package com.dotfield.dotcal.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dotfield.dotcal.data.holiday.HolidayCountry
import com.dotfield.dotcal.data.holiday.HolidayDataSource
import com.dotfield.dotcal.data.provider.CalendarProviderDataSource
import com.dotfield.dotcal.data.provider.ContactsProviderDataSource
import com.dotfield.dotcal.reminders.ReminderScheduler
import com.dotfield.dotcal.sync.CalendarSyncRepository
import com.dotfield.dotcal.sync.CalendarSyncResult
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.widget.WidgetUpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.absoluteValue

data class EventEditorData(
    val eventId: String? = null,
    val title: String,
    val description: String,
    val location: String,
    val date: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAllDay: Boolean,
    val reminderMinutes: Int?,
    val rrule: String?,
    val imageUris: String = "[]",
    val voiceNotePath: String? = null,
)

data class TaskEditorData(
    val title: String,
    val date: LocalDate?,
    val time: LocalTime?,
    val reminderMinutes: Int?,
    val rrule: String? = null,
)

data class BirthdayImportResult(
    val importedCount: Int,
    val permissionDenied: Boolean = false,
)

enum class RecurringEditScope {
    ThisEvent,
    WholeSeries,
}

class DotCalRepository(
    private val dao: CalendarDao,
    private val context: Context,
) {
    private val reminderScheduler = ReminderScheduler(context)
    private val contactsProviderDataSource = ContactsProviderDataSource(context.applicationContext)
    private val holidayDataSource = HolidayDataSource(context.applicationContext)
    private val syncRepository = CalendarSyncRepository(
        dao = dao,
        providerDataSource = CalendarProviderDataSource(context.applicationContext),
    )

    fun observeAccounts(): Flow<List<CalendarAccount>> = dao.observeAccounts()

    fun observeSelectedHolidayCountries(): Flow<List<String>> = dao.observeHolidayAccountIds()
        .map { ids -> ids.mapNotNull { it.removePrefix(HOLIDAY_ACCOUNT_PREFIX).takeIf(String::isNotBlank) } }

    fun observeSyncMetadata(): Flow<List<SyncMetadata>> = dao.observeSyncMetadata()

    fun observeEventsForMonth(month: LocalDate): Flow<List<CalendarEvent>> {
        val start = month.withDayOfMonth(1)
        val end = start.plusMonths(1)
        return dao.observeEvents(start.atStartMs(), end.atStartMs())
            .map { events ->
                withContext(Dispatchers.Default) { expandRecurringEvents(events, start, end) }
            }
    }

    fun observeTasks(): Flow<List<CalendarEvent>> = dao.observeTasks()
        .map { tasks ->
            withContext(Dispatchers.Default) { expandRecurringTasks(tasks) }
        }

    fun observeTodayTasks(day: LocalDate): Flow<List<CalendarEvent>> {
        val start = day.atStartMs()
        return dao.observeTodayTasks(start, day.plusDays(1).atStartMs() - 1)
    }

    fun observeUpcomingTasks(nowMs: Long = System.currentTimeMillis()): Flow<List<CalendarEvent>> = dao.observeUpcomingTasks(nowMs)

    fun observeCompletedTasks(): Flow<List<CalendarEvent>> = dao.observeCompletedTasks()

    fun observeReminders(): Flow<List<EventReminder>> = dao.observeReminders()

    suspend fun getEvent(eventId: String): CalendarEvent? = dao.getEvent(eventId)

    suspend fun getReminderByRequestCode(alarmRequestCode: Int): EventReminder? = dao.getReminderByRequestCode(alarmRequestCode)

    suspend fun markReminderDelivered(alarmRequestCode: Int) {
        dao.markReminderDelivered(alarmRequestCode)
    }

    suspend fun rescheduleFutureReminders() {
        dao.getFutureUndeliveredReminders(System.currentTimeMillis()).forEach { reminder ->
            dao.getEvent(reminder.eventId)?.let { event ->
                reminderScheduler.scheduleReminder(reminder, event)
            }
        }
    }

    suspend fun ensureLocalAccount() {
        dao.insertAccountIfAbsent(
            CalendarAccount(
                id = LOCAL_ACCOUNT_ID,
                accountName = "LOCAL",
                displayName = "Personal",
                accountType = "LOCAL",
                color = "#FF0000",
                isVisible = 1,
                isPrimary = 1,
                sortOrder = 0,
            ),
        )
    }

    suspend fun setAccountVisible(accountId: String, visible: Boolean) {
        if (accountId == LOCAL_ACCOUNT_ID) return
        dao.updateAccountVisibility(accountId, if (visible) 1 else 0)
        updateWidgets()
    }

    suspend fun syncNow(): CalendarSyncResult = withContext(Dispatchers.IO) {
        val result = syncRepository.sync()
        if (!result.permissionDenied) {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_LAST_SYNC_MS] = System.currentTimeMillis()
            }
            updateWidgets()
        }
        result
    }

    suspend fun setBirthdayCalendarEnabled(enabled: Boolean): BirthdayImportResult = withContext(Dispatchers.IO) {
        if (!enabled) {
            disableBirthdayCalendar()
            return@withContext BirthdayImportResult(importedCount = 0)
        }
        if (!contactsProviderDataSource.hasContactsReadPermission()) {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_BIRTHDAY_ENABLED] = false
            }
            return@withContext BirthdayImportResult(importedCount = 0, permissionDenied = true)
        }
        val events = contactsProviderDataSource.getBirthdays(BIRTHDAY_ACCOUNT_ID)
        val reminders = events.mapNotNull(::birthdayReminderFor)
        dao.getBirthdayReminders().forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        dao.replaceBirthdayCalendar(
            account = CalendarAccount(
                id = BIRTHDAY_ACCOUNT_ID,
                accountName = "BIRTHDAY",
                displayName = "Birthdays",
                accountType = "DEVICE",
                color = BIRTHDAY_COLOR,
                isVisible = 1,
                isPrimary = 0,
                sortOrder = BIRTHDAY_SORT_ORDER,
            ),
            events = events,
            reminders = reminders,
        )
        reminders.forEach { reminder ->
            events.firstOrNull { it.id == reminder.eventId }?.let { event ->
                reminderScheduler.scheduleReminder(reminder, event)
            }
        }
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_BIRTHDAY_ENABLED] = true
        }
        BirthdayImportResult(importedCount = events.size)
    }

    suspend fun addHolidayCountry(country: HolidayCountry) = withContext(Dispatchers.IO) {
        val accountId = holidayAccountId(country.code)
        val account = CalendarAccount(
            id = accountId,
            accountName = country.name,
            displayName = "Holidays - ${country.name}",
            accountType = "DEVICE",
            color = DEFAULT_EVENT_COLOR,
            isVisible = 1,
            isPrimary = 0,
            sortOrder = dao.getAccount(accountId)?.sortOrder ?: ((dao.getMaxAccountSortOrder() ?: 0) + 1),
        )
        dao.upsertHolidayCalendar(
            account = account,
            events = holidayDataSource.loadBundledHolidays(country.code, accountId),
        )
        updateWidgets()
    }

    suspend fun removeHolidayCountry(countryCode: String) = withContext(Dispatchers.IO) {
        dao.deleteAccount(holidayAccountId(countryCode))
        updateWidgets()
    }

    suspend fun refreshBirthdayCalendarIfEnabled() = withContext(Dispatchers.IO) {
        val enabled = context.calendarPreferencesDataStore.data
            .map { preferences -> preferences[CalendarPreferences.KEY_BIRTHDAY_ENABLED] ?: false }
            .first()
        if (enabled && contactsProviderDataSource.hasContactsReadPermission()) {
            setBirthdayCalendarEnabled(true)
        }
    }

    suspend fun saveLocalEvent(
        existing: CalendarEvent?,
        data: EventEditorData,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
    ) {
        require(data.title.isNotBlank()) { "TITLE REQUIRED" }
        ensureLocalAccount()
        val zoneId = ZoneId.systemDefault()
        if (existing?.isRecurrenceOccurrence() == true && recurringEditScope == RecurringEditScope.ThisEvent) {
            saveDetachedOccurrence(existing, data, zoneId)
            return
        }
        val eventId = existing?.baseEventId() ?: data.eventId ?: UUID.randomUUID().toString()
        val existingMaster = if (existing?.isRecurrenceOccurrence() == true) {
            dao.getEvent(eventId) ?: existing.copy(id = eventId)
        } else {
            existing
        }
        val startDate = if (existing?.isRecurrenceOccurrence() == true && existingMaster?.rrule != null) {
            existingMaster.startDate()
        } else {
            data.date
        }
        val endDate = if (existing?.isRecurrenceOccurrence() == true && existingMaster?.rrule != null) {
            val selectedSpanDays = ChronoUnit.DAYS.between(data.date, data.endDate).coerceAtLeast(0)
            startDate.plusDays(selectedSpanDays)
        } else {
            data.endDate
        }
        val start = if (data.isAllDay) {
            startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            startDate.atTime(data.startTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        val end = if (data.isAllDay) {
            endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            endDate.atTime(data.endTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        require(end > start) { "END MUST BE AFTER START" }
        val now = System.currentTimeMillis()
        val event = existingMaster?.copy(
            id = eventId,
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            rrule = data.rrule,
            imageUris = data.imageUris,
            voiceNotePath = data.voiceNotePath,
            updatedAtMs = now,
        ) ?: CalendarEvent(
                id = eventId,
                accountId = LOCAL_ACCOUNT_ID,
                title = data.title.trim(),
                description = data.description.trim(),
                location = data.location.trim(),
                startTimeMs = start,
                endTimeMs = end,
                timeZone = zoneId.id,
                isAllDay = if (data.isAllDay) 1 else 0,
                colorHex = null,
                rrule = data.rrule,
                imageUris = data.imageUris,
                source = "LOCAL",
                googleEventId = null,
                googleCalendarId = null,
                completedAtMs = null,
                voiceNotePath = data.voiceNotePath,
                createdAtMs = now,
                updatedAtMs = now,
            )
        dao.getRemindersForEvent(eventId).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        dao.upsertEvent(event)
        dao.deleteRemindersForEvent(eventId)
        data.reminderMinutes?.let { minutes ->
            val reminder = EventReminder(
                eventId = eventId,
                minutesBefore = minutes,
                triggerAtMs = start - minutes * 60_000L,
                alarmRequestCode = "$eventId-$minutes".hashCode().absoluteValue,
            )
            dao.insertReminders(listOf(reminder))
            reminderScheduler.scheduleReminder(reminder, event)
        }
        updateWidgets()
    }

    suspend fun deleteLocalEvent(
        event: CalendarEvent,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
    ) {
        if (event.isRecurrenceOccurrence() && recurringEditScope == RecurringEditScope.ThisEvent) {
            excludeOccurrence(event)
            return
        }
        val eventId = event.baseEventId()
        dao.getRemindersForEvent(eventId).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        event.googleEventId?.let { googleEventId ->
            dao.insertDeletedEventLog(
                DeletedEventLog(
                    googleEventId = googleEventId,
                    deletedAtMs = System.currentTimeMillis(),
                ),
            )
        }
        dao.deleteRemindersForEvent(eventId)
        dao.deleteEvent(eventId)
        updateWidgets()
    }

    suspend fun addLocalEvent(title: String, date: LocalDate, startTime: LocalTime = LocalTime.of(9, 0)) {
        saveLocalEvent(
            existing = null,
            data = EventEditorData(
                title = title,
                description = "",
                location = "",
                date = date,
                endDate = date,
                startTime = startTime,
                endTime = startTime.plusHours(1),
                isAllDay = false,
                reminderMinutes = null,
                rrule = null,
                imageUris = "[]",
                voiceNotePath = null,
            ),
        )
    }

    suspend fun saveLocalTask(existing: CalendarEvent?, data: TaskEditorData) {
        require(data.title.isNotBlank()) { "TITLE REQUIRED" }
        ensureLocalAccount()
        val now = System.currentTimeMillis()
        val zoneId = ZoneId.systemDefault()
        val taskId = existing?.baseEventId() ?: UUID.randomUUID().toString()
        val hasDueDate = data.date != null
        val start = when {
            data.date == null -> 0L
            data.time == null -> data.date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            else -> data.date.atTime(data.time).atZone(zoneId).toInstant().toEpochMilli()
        }
        val end = when {
            data.date == null -> 0L
            data.time == null -> data.date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            else -> start + 30 * 60_000L
        }
        val existingMaster = existing?.baseEventId()?.let { dao.getEvent(it) } ?: existing
        val task = existingMaster?.copy(
            id = taskId,
            title = data.title.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.time == null) 1 else 0,
            rrule = if (hasDueDate) data.rrule else null,
            isTask = 1,
            updatedAtMs = now,
        ) ?: CalendarEvent(
                id = taskId,
                accountId = LOCAL_ACCOUNT_ID,
                title = data.title.trim(),
                description = "",
                location = "",
                startTimeMs = start,
                endTimeMs = end,
                timeZone = zoneId.id,
                isAllDay = if (data.time == null) 1 else 0,
                colorHex = null,
                rrule = if (hasDueDate) data.rrule else null,
                source = "LOCAL",
                googleEventId = null,
                googleCalendarId = null,
                isTask = 1,
                isCompleted = 0,
                completedAtMs = null,
                imageUris = "[]",
                voiceNotePath = null,
                createdAtMs = now,
                updatedAtMs = now,
            )
        dao.getRemindersForEvent(taskId).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        dao.upsertEvent(task)
        dao.deleteRemindersForEvent(taskId)
        if (hasDueDate) {
            data.reminderMinutes?.let { minutes ->
                val reminder = EventReminder(
                    eventId = taskId,
                    minutesBefore = minutes,
                    triggerAtMs = start - minutes * 60_000L,
                    alarmRequestCode = "$taskId-$minutes".hashCode().absoluteValue,
                )
                dao.insertReminders(listOf(reminder))
                reminderScheduler.scheduleReminder(reminder, task)
            }
        }
        updateWidgets()
    }

    suspend fun setTaskCompleted(task: CalendarEvent, completed: Boolean) {
        val now = System.currentTimeMillis()
        dao.updateTaskCompletion(
            eventId = task.baseEventId(),
            isCompleted = if (completed) 1 else 0,
            completedAtMs = if (completed) now else null,
            updatedAtMs = now,
        )
        updateWidgets()
    }

    suspend fun deleteTask(task: CalendarEvent) {
        val taskId = task.baseEventId()
        dao.getRemindersForEvent(taskId).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        dao.deleteRemindersForEvent(taskId)
        dao.deleteEvent(taskId)
        updateWidgets()
    }

    private fun LocalDate.atStartMs(): Long {
        return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private suspend fun saveDetachedOccurrence(existing: CalendarEvent, data: EventEditorData, zoneId: ZoneId) {
        val master = excludeOccurrence(existing) ?: return
        val start = if (data.isAllDay) {
            data.date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            data.date.atTime(data.startTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        val end = if (data.isAllDay) {
            data.endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            data.endDate.atTime(data.endTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        require(end > start) { "END MUST BE AFTER START" }
        val now = System.currentTimeMillis()
        val detachedId = data.eventId ?: UUID.randomUUID().toString()
        val detachedEvent = master.copy(
            id = detachedId,
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            rrule = null,
            exceptionDates = "[]",
            imageUris = data.imageUris,
            voiceNotePath = data.voiceNotePath,
            googleEventId = null,
            googleCalendarId = null,
            syncVersion = 0,
            createdAtMs = now,
            updatedAtMs = now,
        )
        dao.upsertEvent(detachedEvent)
        data.reminderMinutes?.let { minutes ->
            val reminder = EventReminder(
                eventId = detachedId,
                minutesBefore = minutes,
                triggerAtMs = start - minutes * 60_000L,
                alarmRequestCode = "$detachedId-$minutes".hashCode().absoluteValue,
            )
            dao.insertReminders(listOf(reminder))
            reminderScheduler.scheduleReminder(reminder, detachedEvent)
        }
        updateWidgets()
    }

    private suspend fun excludeOccurrence(event: CalendarEvent): CalendarEvent? {
        val occurrenceStartMs = event.occurrenceStartMs() ?: return null
        val masterId = event.baseEventId()
        val master = dao.getEvent(masterId) ?: return null
        val exceptions = master.exceptionStartTimes() + occurrenceStartMs
        val updatedMaster = master.copy(
            exceptionDates = exceptions.sorted().joinToString(prefix = "[", postfix = "]"),
            updatedAtMs = System.currentTimeMillis(),
        )
        dao.upsertEvent(updatedMaster)
        updateWidgets()
        return updatedMaster
    }

    private fun updateWidgets() {
        WidgetUpdateWorker.enqueue(context)
    }

    private fun expandRecurringEvents(events: List<CalendarEvent>, rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val expanded = events.flatMap { event ->
            when (event.rrule?.trim()) {
                "FREQ=DAILY" -> event.expandDaily(rangeStart, rangeEndExclusive)
                "FREQ=WEEKLY" -> event.expandWeekly(rangeStart, rangeEndExclusive)
                "FREQ=MONTHLY" -> event.expandMonthly(rangeStart, rangeEndExclusive)
                "FREQ=YEARLY" -> event.expandYearly(rangeStart, rangeEndExclusive)
                else -> listOf(event)
            }
        }
        return expanded.sortedWith(compareBy<CalendarEvent> { it.startTimeMs }.thenBy { it.title })
    }

    private fun expandRecurringTasks(tasks: List<CalendarEvent>): List<CalendarEvent> {
        val today = LocalDate.now()
        val rangeEnd = today.plusYears(1)
        val expanded = tasks.flatMap { task ->
            when (task.rrule?.trim()) {
                "FREQ=DAILY" -> task.expandDaily(today, rangeEnd)
                "FREQ=WEEKLY" -> task.expandWeekly(today, rangeEnd)
                "FREQ=MONTHLY" -> task.expandMonthly(today, rangeEnd)
                "FREQ=YEARLY" -> task.expandYearly(today, rangeEnd)
                else -> listOf(task)
            }
        }
        return expanded.sortedWith(compareBy<CalendarEvent> { it.isCompleted }.thenBy { if (it.startTimeMs > 0L) it.startTimeMs else Long.MAX_VALUE }.thenBy { it.title })
    }

    private fun CalendarEvent.expandDaily(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        val firstVisibleDate = maxOf(firstDate, rangeStart)
        return generateOccurrences(firstVisibleDate, rangeEndExclusive) { it.plusDays(1) }
    }

    private fun CalendarEvent.expandWeekly(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        val daysToRange = ChronoUnit.DAYS.between(firstDate, rangeStart).coerceAtLeast(0)
        val weeksToRange = daysToRange / 7
        val firstVisibleDate = firstDate.plusWeeks(weeksToRange).let { candidate ->
            if (candidate < rangeStart) candidate.plusWeeks(1) else candidate
        }
        return generateOccurrences(firstVisibleDate, rangeEndExclusive) { it.plusWeeks(1) }
    }

    private fun CalendarEvent.expandMonthly(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        val monthsToRange = ChronoUnit.MONTHS.between(firstDate.withDayOfMonth(1), rangeStart.withDayOfMonth(1)).coerceAtLeast(0)
        var cursorMonth = firstDate.withDayOfMonth(1).plusMonths(monthsToRange)
        val occurrences = mutableListOf<CalendarEvent>()
        while (cursorMonth < rangeEndExclusive.withDayOfMonth(1).plusMonths(1)) {
            val date = cursorMonth.dayOrNull(firstDate.dayOfMonth)
            if (date != null && date >= firstDate && date >= rangeStart && date < rangeEndExclusive) {
                occurrenceOn(date)?.let { occurrences += it }
            }
            cursorMonth = cursorMonth.plusMonths(1)
        }
        return occurrences
    }

    private fun CalendarEvent.expandYearly(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        var cursorYear = maxOf(firstDate.year, rangeStart.year)
        val occurrences = mutableListOf<CalendarEvent>()
        while (cursorYear <= rangeEndExclusive.year) {
            val date = LocalDate.of(cursorYear, 1, 1).monthDayOrNull(firstDate.monthValue, firstDate.dayOfMonth)
            if (date != null && date >= firstDate && date >= rangeStart && date < rangeEndExclusive) {
                occurrenceOn(date)?.let { occurrences += it }
            }
            cursorYear += 1
        }
        return occurrences
    }

    private fun CalendarEvent.generateOccurrences(
        firstVisibleDate: LocalDate,
        rangeEndExclusive: LocalDate,
        nextDate: (LocalDate) -> LocalDate,
    ): List<CalendarEvent> {
        val firstDate = startDate()
        var cursor = firstVisibleDate
        val occurrences = mutableListOf<CalendarEvent>()
        while (cursor < rangeEndExclusive) {
            if (cursor >= firstDate) occurrenceOn(cursor)?.let { occurrences += it }
            cursor = nextDate(cursor)
        }
        return occurrences
    }

    private fun CalendarEvent.occurrenceOn(date: LocalDate): CalendarEvent? {
        val zoneId = ZoneId.of(timeZone)
        val startDateTime = java.time.Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDateTime()
        val durationMs = endTimeMs - startTimeMs
        val occurrenceStart = date.atTime(startDateTime.toLocalTime()).atZone(zoneId).toInstant().toEpochMilli()
        if (occurrenceStart in exceptionStartTimes()) return null
        return copy(
            id = recurrenceOccurrenceId(id, occurrenceStart),
            startTimeMs = occurrenceStart,
            endTimeMs = occurrenceStart + durationMs,
        )
    }

    private fun CalendarEvent.startDate(): LocalDate {
        return java.time.Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.of(timeZone)).toLocalDate()
    }

    private fun LocalDate.dayOrNull(day: Int): LocalDate? {
        val yearMonth = YearMonth.from(this)
        return if (day <= yearMonth.lengthOfMonth()) withDayOfMonth(day) else null
    }

    private fun LocalDate.monthDayOrNull(month: Int, day: Int): LocalDate? {
        return runCatching { withMonth(month).dayOrNull(day) }.getOrNull()
    }

    private suspend fun disableBirthdayCalendar() {
        dao.getBirthdayReminders().forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        dao.deleteBirthdayEvents()
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_BIRTHDAY_ENABLED] = false
        }
    }

    private fun birthdayReminderFor(event: CalendarEvent): EventReminder? {
        val nextStart = nextBirthdayStartMs(event) ?: return null
        val triggerAtMs = nextStart - BIRTHDAY_REMINDER_MINUTES * 60_000L
        if (triggerAtMs <= System.currentTimeMillis()) return null
        return EventReminder(
            eventId = event.id,
            minutesBefore = BIRTHDAY_REMINDER_MINUTES,
            triggerAtMs = triggerAtMs,
            alarmRequestCode = "${event.id}-$BIRTHDAY_REMINDER_MINUTES".hashCode().absoluteValue,
        )
    }

    private fun nextBirthdayStartMs(event: CalendarEvent): Long? {
        val zoneId = ZoneId.of(event.timeZone)
        val birthday = java.time.Instant.ofEpochMilli(event.startTimeMs).atZone(zoneId).toLocalDate()
        val today = LocalDate.now(zoneId)
        var year = today.year
        repeat(8) {
            val candidate = runCatching { LocalDate.of(year, birthday.monthValue, birthday.dayOfMonth) }.getOrNull()
            if (candidate != null && candidate >= today) {
                return candidate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            }
            year += 1
        }
        return null
    }

    companion object {
        const val LOCAL_ACCOUNT_ID = "local-primary"
        private const val HOLIDAY_ACCOUNT_PREFIX = "holiday-"
        private const val DEFAULT_EVENT_COLOR = "#FF0000"
        private const val BIRTHDAY_ACCOUNT_ID = ContactsProviderDataSource.BIRTHDAY_ACCOUNT_ID
        private const val BIRTHDAY_COLOR = ContactsProviderDataSource.BIRTHDAY_COLOR
        private const val BIRTHDAY_REMINDER_MINUTES = 1440
        private const val BIRTHDAY_SORT_ORDER = 10

        fun holidayAccountId(countryCode: String): String = "$HOLIDAY_ACCOUNT_PREFIX$countryCode"
    }
}

private const val RECURRENCE_OCCURRENCE_SEPARATOR = "::occurrence::"

fun recurrenceOccurrenceId(eventId: String, occurrenceStartMs: Long): String {
    return "$eventId$RECURRENCE_OCCURRENCE_SEPARATOR$occurrenceStartMs"
}

fun CalendarEvent.baseEventId(): String {
    return id.substringBefore(RECURRENCE_OCCURRENCE_SEPARATOR)
}

fun CalendarEvent.isRecurrenceOccurrence(): Boolean {
    return id.contains(RECURRENCE_OCCURRENCE_SEPARATOR)
}

fun CalendarEvent.occurrenceStartMs(): Long? {
    return id.substringAfter(RECURRENCE_OCCURRENCE_SEPARATOR, "").toLongOrNull()
}

private fun CalendarEvent.exceptionStartTimes(): Set<Long> {
    return exceptionDates
        .removePrefix("[")
        .removeSuffix("]")
        .split(',')
        .mapNotNull { it.trim().toLongOrNull() }
        .toSet()
}
