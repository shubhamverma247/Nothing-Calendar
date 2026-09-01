package com.dotfield.dotcal.data

import android.content.Context
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.preferences.core.edit
import com.dotfield.dotcal.data.backup.BackupData
import com.dotfield.dotcal.data.backup.BackupFileAttachment
import com.dotfield.dotcal.data.backup.BackupSerializer
import com.dotfield.dotcal.data.attachments.EventFileAttachment
import com.dotfield.dotcal.data.attachments.encodeEventFileAttachments
import com.dotfield.dotcal.data.attachments.eventAttachmentDirectory
import com.dotfield.dotcal.data.attachments.eventAttachmentFile
import com.dotfield.dotcal.data.attachments.parseEventFileAttachments
import com.dotfield.dotcal.data.holiday.HolidayCountry
import com.dotfield.dotcal.data.holiday.HolidayDataSource
import com.dotfield.dotcal.data.countdown.CountdownPinResult
import com.dotfield.dotcal.data.countdown.CountdownPinStore
import com.dotfield.dotcal.data.provider.CalendarProviderDataSource
import com.dotfield.dotcal.data.provider.ContactsProviderDataSource
import com.dotfield.dotcal.data.provider.ProviderMeetingMetadata
import com.dotfield.dotcal.data.provider.decodeProviderMeetingMetadata
import com.dotfield.dotcal.data.privacy.AppLockState
import com.dotfield.dotcal.data.privacy.AppPrivacyManager
import com.dotfield.dotcal.data.punchcard.PunchCardStreak
import com.dotfield.dotcal.data.insights.OnThisDayCandidate
import com.dotfield.dotcal.data.insights.OnThisDayFinder
import com.dotfield.dotcal.data.insights.OnThisDayMemory
import com.dotfield.dotcal.data.provider.providerCalendarId
import com.dotfield.dotcal.data.provider.providerRdateStartTimes
import com.dotfield.dotcal.data.provider.providerReminderAlarmRequestCode
import com.dotfield.dotcal.data.provider.ContactsProviderDataSource.Companion.BIRTHDAY_BASE_YEAR
import com.dotfield.dotcal.data.recurrence.RecurrenceRule
import com.dotfield.dotcal.data.recurrence.planNextReminder
import com.dotfield.dotcal.data.profiles.FocusProfile
import com.dotfield.dotcal.data.profiles.FocusProfileStore
import com.dotfield.dotcal.data.scheduling.BusyPeriod
import com.dotfield.dotcal.data.scheduling.DayAvailability
import com.dotfield.dotcal.data.scheduling.DeadTimeFinder
import com.dotfield.dotcal.data.scheduling.DeadTimeResult
import com.dotfield.dotcal.data.scheduling.EventDragMath
import com.dotfield.dotcal.data.scheduling.EventTimeRange
import com.dotfield.dotcal.data.scheduling.FreeSlotEngine
import com.dotfield.dotcal.data.scheduling.FreeSlotRequest
import com.dotfield.dotcal.data.shifts.GeneratedShiftOccurrence
import com.dotfield.dotcal.data.shifts.ShiftApplyResult
import com.dotfield.dotcal.data.shifts.ShiftEventGeneratedBy
import com.dotfield.dotcal.data.shifts.ShiftEventMetadata
import com.dotfield.dotcal.data.shifts.ShiftGenerationRecord
import com.dotfield.dotcal.data.shifts.ShiftPattern
import com.dotfield.dotcal.data.shifts.ShiftPatternStore
import com.dotfield.dotcal.data.shifts.ShiftType
import com.dotfield.dotcal.data.shifts.encode
import com.dotfield.dotcal.data.shifts.buildShiftEventDraft
import com.dotfield.dotcal.data.shifts.buildShiftPlanShareEvents
import com.dotfield.dotcal.data.shifts.expandShiftPattern
import com.dotfield.dotcal.data.shifts.parseShiftEventMetadata
import com.dotfield.dotcal.data.shifts.shiftMetadataFor
import com.dotfield.dotcal.data.sidestore.SharedSideStore
import com.dotfield.dotcal.data.sidestore.EventSideStoreNamespaces
import com.dotfield.dotcal.data.templates.EventTemplate
import com.dotfield.dotcal.data.templates.EventTemplateStore
import com.dotfield.dotcal.data.trash.DeletedSnapshot
import com.dotfield.dotcal.data.trash.RecentlyDeletedStore
import com.dotfield.dotcal.reminders.ReminderScheduler
import com.dotfield.dotcal.sync.CalendarSyncRepository
import com.dotfield.dotcal.sync.CalendarSyncResult
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.widget.WidgetUpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.io.File
import java.util.Base64
import java.util.Locale
import java.util.UUID
import kotlin.math.absoluteValue

data class EventEditorData(
    val eventId: String? = null,
    val accountId: String? = null,
    val title: String,
    val description: String,
    val location: String,
    val date: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAllDay: Boolean,
    val reminderMinutes: Int?,
    val reminderMinutesList: List<Int>? = null,
    val rrule: String?,
    val imageUris: String = "[]",
    val voiceNotePath: String? = null,
    val colorHex: String? = null,
    val isGhost: Boolean = false,
    val fileAttachments: List<EventFileAttachment> = emptyList(),
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

data class BulkEditUndoToken(
    val previousEvents: List<CalendarEvent> = emptyList(),
    val previousReminders: List<EventReminder> = emptyList(),
    val insertedEventIds: List<String> = emptyList(),
    val deletedSnapshotIds: List<String> = emptyList(),
    val previousGhostFlags: Map<String, Boolean> = emptyMap(),
)

data class BulkEditResult(
    val changedCount: Int,
    val skippedCount: Int = 0,
    val undoToken: BulkEditUndoToken,
)

enum class RecurringEditScope {
    ThisEvent,
    ThisAndFollowing,
    WholeSeries,
}

class DotCalRepository(
    private val dao: CalendarDao,
    private val context: Context,
) {
    private val reminderScheduler = ReminderScheduler(context)
    private val privacyManager = AppPrivacyManager(context.applicationContext)
    private val recentlyDeletedStore = RecentlyDeletedStore(context)
    private val eventTemplateStore = EventTemplateStore(context)
    private val focusProfileStore = FocusProfileStore(context)
    private val shiftPatternStore = ShiftPatternStore(context)
    private val sideStore = SharedSideStore(context)
    private val contactsProviderDataSource = ContactsProviderDataSource(context.applicationContext)
    private val calendarProviderDataSource = CalendarProviderDataSource(context.applicationContext)
    private val holidayDataSource = HolidayDataSource(context.applicationContext)
    private val syncRepository = CalendarSyncRepository(
        dao = dao,
        providerDataSource = calendarProviderDataSource,
        reminderScheduler = reminderScheduler,
        sideStore = sideStore,
    )

    fun observeIsPro(): Flow<Boolean> =
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_IS_PRO] ?: false
        }

    suspend fun readIsPro(): Boolean = withContext(Dispatchers.IO) {
        context.calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_IS_PRO] ?: false
    }

    suspend fun setIsPro(isPro: Boolean) = withContext(Dispatchers.IO) {
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_IS_PRO] = isPro
        }
    }

    suspend fun readPunchedDays(): Set<LocalDate> = withContext(Dispatchers.IO) {
        sideStore.readNamespace(PunchCardStreak.Namespace)
            .filterValues { it == "true" }
            .keys
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .toSet()
    }

    suspend fun setDayPunched(date: LocalDate, punched: Boolean) {
        if (punched) {
            sideStore.write(PunchCardStreak.Namespace, date.toString(), "true")
        } else {
            sideStore.remove(PunchCardStreak.Namespace, date.toString())
        }
    }

    suspend fun readCountdownPins(): Set<String> = withContext(Dispatchers.IO) {
        sideStore.readNamespace(CountdownPinStore.Namespace)
            .filterValues { it == "true" }
            .keys
            .toSet()
    }

    suspend fun pinCountdown(eventId: String, isPro: Boolean): CountdownPinResult = withContext(Dispatchers.IO) {
        val currentPins = readCountdownPins()
        if (!isPro && eventId !in currentPins && currentPins.isNotEmpty()) {
            return@withContext CountdownPinResult.FreeLimitReached(currentPins.first())
        }
        sideStore.write(CountdownPinStore.Namespace, eventId, "true")
        updateWidgets()
        CountdownPinResult.Pinned
    }

    suspend fun swapCountdownPin(activeEventId: String, newEventId: String) = withContext(Dispatchers.IO) {
        sideStore.remove(CountdownPinStore.Namespace, activeEventId)
        sideStore.write(CountdownPinStore.Namespace, newEventId, "true")
        updateWidgets()
    }

    suspend fun unpinCountdown(eventId: String) = withContext(Dispatchers.IO) {
        sideStore.remove(CountdownPinStore.Namespace, eventId)
        updateWidgets()
    }

    suspend fun readEventFileAttachments(eventId: String): List<EventFileAttachment> = withContext(Dispatchers.IO) {
        sideStore.read(EVENT_FILE_ATTACHMENTS_NAMESPACE, eventId)
            ?.let(::parseEventFileAttachments)
            .orEmpty()
    }

    suspend fun readProviderMeetingMetadata(eventId: String): ProviderMeetingMetadata? = withContext(Dispatchers.IO) {
        sideStore.read(EventSideStoreNamespaces.ProviderMeetingMetadata, eventId)
            ?.let(::decodeProviderMeetingMetadata)
    }

    suspend fun importEventFileAttachment(
        eventId: String,
        sourceUri: Uri,
        currentAttachments: List<EventFileAttachment>,
    ): EventFileAttachment = withContext(Dispatchers.IO) {
        require(currentAttachments.size < MAX_EVENT_FILE_ATTACHMENTS) { "FILE LIMIT REACHED" }
        val resolver = context.contentResolver
        val mimeType = resolver.getType(sourceUri) ?: EVENT_FILE_PDF_MIME
        val displayName = resolver.queryDisplayName(sourceUri)
        require(mimeType == EVENT_FILE_PDF_MIME || displayName.endsWith(".pdf", ignoreCase = true)) {
            "ONLY PDF ATTACHMENTS ARE SUPPORTED"
        }
        val attachmentId = UUID.randomUUID().toString()
        val output = eventAttachmentFile(context.filesDir, eventId, attachmentId)
        output.parentFile?.mkdirs()
        var copiedBytes = 0L
        resolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "FILE UNAVAILABLE" }
            output.outputStream().use { out ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    copiedBytes += read
                    if (copiedBytes > MAX_EVENT_FILE_ATTACHMENT_BYTES) {
                        runCatching { output.delete() }
                        error("FILE TOO LARGE")
                    }
                    out.write(buffer, 0, read)
                }
            }
        }
        EventFileAttachment(
            id = attachmentId,
            displayName = displayName,
            mimeType = EVENT_FILE_PDF_MIME,
            sizeBytes = copiedBytes,
            localPath = output.absolutePath,
            addedAtMs = System.currentTimeMillis(),
        )
    }

    suspend fun discardEventFileAttachment(attachment: EventFileAttachment) = withContext(Dispatchers.IO) {
        runCatching { File(attachment.localPath).delete() }
    }

    fun observeAppLockState(): Flow<AppLockState> = privacyManager.observeAppLockState()

    fun observePrivateVaultIds(): Flow<Set<String>> = privacyManager.observePrivateVaultIds()

    suspend fun setAppLockPin(pin: String) = privacyManager.setPin(pin)

    suspend fun verifyAppLockPin(pin: String): Boolean = privacyManager.verifyPin(pin)

    suspend fun setAppLockEnabled(enabled: Boolean) = privacyManager.setAppLockEnabled(enabled)

    suspend fun disableAppLock() = privacyManager.disableAppLock()

    suspend fun clearAppLockPin() = privacyManager.clearPin()

    suspend fun listPrivateVaultEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        privacyManager.observePrivateVaultIds().first()
            .mapNotNull { eventId -> dao.getEvent(eventId) }
            .sortedWith(compareBy<CalendarEvent> { it.isTask }.thenBy { it.startTimeMs }.thenBy { it.title })
    }

    suspend fun moveToPrivateVault(event: CalendarEvent) = withContext(Dispatchers.IO) {
        val eventId = event.baseEventId()
        dao.getRemindersForEvent(eventId).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        privacyManager.addPrivateEvent(eventId)
        updateWidgets()
    }

    suspend fun restoreFromPrivateVault(eventId: String) = withContext(Dispatchers.IO) {
        val baseId = eventId.substringBefore(RECURRENCE_OCCURRENCE_SEPARATOR)
        val event = dao.getEvent(baseId)
        privacyManager.removePrivateEvent(baseId)
        if (event != null) {
            val now = System.currentTimeMillis()
            dao.getRemindersForEvent(baseId)
                .filter { it.triggerAtMs > now && it.isDelivered == 0 }
                .forEach { reminderScheduler.scheduleReminder(it, event) }
        }
        updateWidgets()
    }

    fun observeAccounts(): Flow<List<CalendarAccount>> = dao.observeAccounts()

    fun observeAssignableAccounts(): Flow<List<CalendarAccount>> {
        return dao.observeAccounts().map { accounts ->
            accounts
                .filterNot { it.isReadOnlyGeneratedAccount() }
                .sortedWith(compareBy<CalendarAccount> { it.sortOrder }.thenBy { it.displayName })
        }
    }

    /** Result summary of an ICS import. */
    data class IcsImportResult(
        val inserted: Int,
        val updated: Int,
        val skipped: Int,
    )

    /** Serializes all user-owned events + tasks to an RFC 5545 iCalendar document. */
    suspend fun exportIcs(): String = withContext(Dispatchers.IO) {
        val events = dao.getAllUserEventsForExport()
        val remindersByEventId = events.associate { event ->
            event.id to dao.getRemindersForEvent(event.id)
        }
        com.dotfield.dotcal.data.ics.IcsExporter.export(events, remindersByEventId)
    }

    /** Number of rows that would be exported; used to disable export when there is nothing to save. */
    suspend fun countExportableEvents(): Int = withContext(Dispatchers.IO) {
        dao.getAllUserEventsForExport().size
    }

    /**
     * Parses [icsText] and upserts events/tasks into the local calendar. Existing rows are matched
     * by UID (stored as the event id) and updated in place; unknown UIDs are inserted with a fresh
     * id. VALARM reminders are imported when present; missing alarms preserve existing reminders.
     * Recurrence exceptions are preserved.
     */
    suspend fun importIcs(icsText: String): IcsImportResult = withContext(Dispatchers.IO) {
        ensureLocalAccount()
        val items = com.dotfield.dotcal.data.ics.IcsParser.parse(icsText)
        var inserted = 0
        var updated = 0
        var skipped = 0
        val now = System.currentTimeMillis()
        items.forEach { item ->
            if (item.title.isBlank()) { skipped++; return@forEach }
            // Only trust a UID as an existing local id when the row actually exists locally.
            val existing = item.uid?.let { dao.getEvent(it) }
            val targetId = existing?.id ?: item.uid?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val exceptionDates = if (item.exceptionMs.isEmpty()) {
                existing?.exceptionDates ?: "[]"
            } else {
                item.exceptionMs.sorted().joinToString(prefix = "[", postfix = "]")
            }
            val row = CalendarEvent(
                id = targetId,
                accountId = existing?.accountId ?: LOCAL_ACCOUNT_ID,
                title = item.title.trim(),
                description = item.description.trim(),
                location = item.location.trim(),
                startTimeMs = item.startTimeMs,
                endTimeMs = item.endTimeMs,
                timeZone = item.timeZone,
                isAllDay = if (item.isAllDay) 1 else 0,
                colorHex = existing?.colorHex,
                rrule = item.rrule,
                exceptionDates = exceptionDates,
                source = existing?.source ?: "LOCAL",
                googleEventId = existing?.googleEventId,
                googleCalendarId = existing?.googleCalendarId,
                syncVersion = existing?.syncVersion ?: 0,
                isTask = if (item.isTask) 1 else 0,
                isCompleted = if (item.isCompleted) 1 else 0,
                completedAtMs = item.completedAtMs ?: existing?.completedAtMs,
                imageUris = existing?.imageUris ?: "[]",
                voiceNotePath = existing?.voiceNotePath,
                createdAtMs = existing?.createdAtMs ?: now,
                updatedAtMs = now,
            )
            // Guard against invalid spans for timed events (tasks may legitimately be 0/0).
            if (!item.isTask && row.endTimeMs <= row.startTimeMs) { skipped++; return@forEach }
            dao.upsertEvent(row)
            if (item.reminderMinutes.isNotEmpty() && row.startTimeMs > 0L) {
                replaceImportedReminders(row, item.reminderMinutes)
            }
            if (existing != null) updated++ else inserted++
        }
        if (inserted > 0 || updated > 0) updateWidgets()
        IcsImportResult(inserted = inserted, updated = updated, skipped = skipped)
    }

    private suspend fun replaceImportedReminders(event: CalendarEvent, minutesBefore: List<Int>) {
        dao.getRemindersForEvent(event.id).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        dao.deleteRemindersForEvent(event.id)
        val reminders = minutesBefore.distinct().sorted().map { minutes ->
            EventReminder(
                eventId = event.id,
                minutesBefore = minutes,
                triggerAtMs = event.startTimeMs - minutes * 60_000L,
                alarmRequestCode = "${event.id}-$minutes".hashCode().absoluteValue,
            )
        }
        dao.insertReminders(reminders)
        reminders.forEach { reminder -> reminderScheduler.scheduleReminder(reminder, event) }
    }

    /** Result summary of a Backup restore. */
    data class BackupImportResult(
        val accountsAdded: Int,
        val eventsInserted: Int,
        val eventsUpdated: Int,
        val remindersRestored: Int,
        val fileAttachmentsRestored: Int = 0,
    )

    /**
     * Serializes a full-fidelity snapshot of the user's calendar (accounts that own the events,
     * the user-owned events/tasks, and their reminders) to a JSON document. Excludes generated
     * (birthday/holiday) and Google-synced rows, which regenerate from their sources.
     */
    suspend fun exportBackup(): String = withContext(Dispatchers.IO) {
        val events = dao.getAllUserEventsForExport()
        val accounts = events.map { it.accountId }.toSet().mapNotNull { dao.getAccount(it) }
        val reminders = events.flatMap { dao.getRemindersForEvent(it.id) }
        val fileAttachments = exportBackupFileAttachments(events)
        BackupSerializer.encode(
            BackupData(
                accounts = accounts,
                events = events,
                reminders = reminders,
                fileAttachments = fileAttachments,
                createdAtMs = System.currentTimeMillis(),
            ),
        )
    }

    /**
     * Restores a backup produced by [exportBackup]. Non-destructive merge: absent accounts are
     * added (existing account config is preserved), events are upserted by id (existing rows
     * updated in place, unknown ids inserted), and an event's reminders are replaced + rescheduled
     * only when the backup carries reminders for it (matching ICS import semantics). Live data not
     * present in the backup is never deleted. Throws on a foreign/unparseable file.
     */
    suspend fun importBackup(json: String): BackupImportResult = withContext(Dispatchers.IO) {
        val data = BackupSerializer.decode(json)
        ensureLocalAccount()
        var accountsAdded = 0
        data.accounts.forEach { account ->
            if (account.id != LOCAL_ACCOUNT_ID && dao.getAccount(account.id) == null) {
                dao.insertAccountIfAbsent(account)
                accountsAdded++
            }
        }
        val remindersByEventId = data.reminders.groupBy { it.eventId }
        var eventsInserted = 0
        var eventsUpdated = 0
        var remindersRestored = 0
        var fileAttachmentsRestored = 0
        val now = System.currentTimeMillis()
        data.events.forEach { event ->
            val existing = dao.getEvent(event.id)
            // The event's account may be missing on this device; fall back to local so the FK holds.
            val accountId = if (dao.getAccount(event.accountId) != null) event.accountId else LOCAL_ACCOUNT_ID
            val row = event.copy(accountId = accountId)
            dao.upsertEvent(row)
            val reminders = remindersByEventId[event.id].orEmpty()
            if (reminders.isNotEmpty()) {
                dao.getRemindersForEvent(row.id).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
                dao.deleteRemindersForEvent(row.id)
                dao.insertReminders(reminders)
                reminders
                    .filter { it.triggerAtMs > now && it.isDelivered == 0 }
                    .forEach { reminderScheduler.scheduleReminder(it, row) }
                remindersRestored += reminders.size
            }
            if (existing != null) eventsUpdated++ else eventsInserted++
        }
        val restoredEventIds = data.events.map { it.id }.toSet()
        val fileAttachmentsByEventId = data.fileAttachments
            .filter { fileAttachment -> fileAttachment.eventId in restoredEventIds }
            .groupBy { it.eventId }
        if (data.hasFileAttachmentsSection) {
            restoredEventIds.forEach { eventId ->
                val restored = restoreBackupFileAttachments(eventId, fileAttachmentsByEventId[eventId].orEmpty())
                replaceEventFileAttachments(eventId, restored)
                fileAttachmentsRestored += restored.size
            }
        } else {
            fileAttachmentsByEventId.forEach { (eventId, attachments) ->
                val restored = restoreBackupFileAttachments(eventId, attachments)
                replaceEventFileAttachments(eventId, restored)
                fileAttachmentsRestored += restored.size
            }
        }
        if (eventsInserted > 0 || eventsUpdated > 0 || accountsAdded > 0) updateWidgets()
        BackupImportResult(
            accountsAdded = accountsAdded,
            eventsInserted = eventsInserted,
            eventsUpdated = eventsUpdated,
            remindersRestored = remindersRestored,
            fileAttachmentsRestored = fileAttachmentsRestored,
        )
    }

    private suspend fun exportBackupFileAttachments(events: List<CalendarEvent>): List<BackupFileAttachment> {
        return events.flatMap { event ->
            readEventFileAttachments(event.id).mapNotNull { attachment ->
                val file = File(attachment.localPath)
                if (!file.exists() || file.length() > MAX_EVENT_FILE_ATTACHMENT_BYTES) return@mapNotNull null
                BackupFileAttachment(
                    eventId = event.id,
                    attachment = attachment,
                    base64Content = Base64.getEncoder().encodeToString(file.readBytes()),
                )
            }
        }
    }

    private fun restoreBackupFileAttachments(
        eventId: String,
        attachments: List<BackupFileAttachment>,
    ): List<EventFileAttachment> {
        return attachments.take(MAX_EVENT_FILE_ATTACHMENTS).mapNotNull { backupAttachment ->
            runCatching {
                val bytes = Base64.getDecoder().decode(backupAttachment.base64Content)
                require(bytes.size <= MAX_EVENT_FILE_ATTACHMENT_BYTES) { "FILE TOO LARGE" }
                val attachment = backupAttachment.attachment
                val output = eventAttachmentFile(context.filesDir, eventId, attachment.id)
                output.parentFile?.mkdirs()
                output.writeBytes(bytes)
                attachment.copy(
                    mimeType = attachment.mimeType.ifBlank { EVENT_FILE_PDF_MIME },
                    sizeBytes = bytes.size.toLong(),
                    localPath = output.absolutePath,
                )
            }.getOrNull()
        }
    }

    fun observeSelectedHolidayCountries(): Flow<List<String>> = dao.observeHolidayAccountIds()
        .map { ids -> ids.mapNotNull { it.removePrefix(HOLIDAY_ACCOUNT_PREFIX).takeIf(String::isNotBlank) } }

    fun observeSyncMetadata(): Flow<List<SyncMetadata>> = dao.observeSyncMetadata()

    fun observeEventsForMonth(month: LocalDate): Flow<List<CalendarEvent>> {
        val monthStart = month.withDayOfMonth(1)
        // Load a rolling window of the previous, current, and next month so that
        // Week/Day/3-day views straddling a month boundary always have their
        // neighbouring-month events, and paging across months reuses already-loaded
        // data instead of showing a gap while a narrower query reloads.
        val start = monthStart.minusMonths(1)
        val end = monthStart.plusMonths(2)
        return dao.observeEvents(start.atStartMs(), end.atStartMs())
            .retryOnDatabaseLocked()
            .combine(privacyManager.observePrivateVaultIds()) { events, privateIds -> events.filterOutPrivate(privateIds) }
            .map { events ->
                val providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates)
                val expanded = withContext(Dispatchers.Default) { expandRecurringEvents(events, start, end, providerRdates) }
                expanded.withGhostFlags()
            }
    }

    fun observeEventsForYear(year: Int): Flow<List<CalendarEvent>> {
        val start = LocalDate.of(year, 1, 1)
        val end = start.plusYears(1)
        return dao.observeEvents(start.atStartMs(), end.atStartMs())
            .retryOnDatabaseLocked()
            .combine(privacyManager.observePrivateVaultIds()) { events, privateIds -> events.filterOutPrivate(privateIds) }
            .map { events ->
                val providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates)
                val expanded = withContext(Dispatchers.Default) { expandRecurringEvents(events, start, end, providerRdates) }
                expanded.withGhostFlags()
            }
    }

    suspend fun computeAvailability(request: FreeSlotRequest): List<DayAvailability> {
        val busyPeriods = loadBusyPeriods(request.rangeStart, request.rangeEnd)
        val preferences = context.calendarPreferencesDataStore.data.first()
        val bufferedRequest = request.copy(
            bufferBeforeMinutes = (preferences[CalendarPreferences.KEY_AUTO_BUFFER_BEFORE_MINUTES] ?: 0)
                .coerceIn(0, 240),
            bufferAfterMinutes = (preferences[CalendarPreferences.KEY_AUTO_BUFFER_AFTER_MINUTES] ?: 0)
                .coerceIn(0, 240),
        )
        return withContext(Dispatchers.Default) {
            FreeSlotEngine.compute(bufferedRequest, busyPeriods)
        }
    }

    suspend fun computeDeadTime(
        startDate: LocalDate,
        startHour: Int,
        endHour: Int,
    ): DeadTimeResult {
        val rangeEnd = startDate.plusDays(DeadTimeFinder.DAYS_TO_SEARCH - 1L)
        val busyPeriods = loadBusyPeriods(startDate, rangeEnd)
        return withContext(Dispatchers.Default) {
            DeadTimeFinder.find(startDate, busyPeriods, startHour, endHour)
        }
    }

    private suspend fun loadBusyPeriods(
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
    ): List<BusyPeriod> {
        val rangeEndExclusive = rangeEnd.plusDays(1)
        val events = dao.observeEvents(rangeStart.atStartMs(), rangeEndExclusive.atStartMs())
            .retryOnDatabaseLocked()
            .first()
        val privateIds = privacyManager.observePrivateVaultIds().first()
        val ghostIds = sideStore.readNamespace(GHOST_FLAGS_NAMESPACE)
            .filterValues { it == "1" }
            .keys
        val providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates)
        return expandRecurringEvents(
            events.filterOutPrivate(privateIds),
            rangeStart,
            rangeEndExclusive,
            providerRdates,
        ).map { event ->
            val zone = safeZoneId(event.timeZone)
            BusyPeriod(
                start = Instant.ofEpochMilli(event.startTimeMs).atZone(zone).toLocalDateTime(),
                end = Instant.ofEpochMilli(event.endTimeMs).atZone(zone).toLocalDateTime(),
                isAllDay = event.isAllDay == 1,
                isGhost = event.baseEventId() in ghostIds,
            )
        }
    }

    fun observeAgendaEvents(rangeStart: LocalDate, rangeEnd: LocalDate): Flow<List<CalendarEvent>> {
        val start = rangeStart
        val end = rangeEnd
        val startMs = start.atStartMs()
        return dao.observeEvents(startMs, end.atStartMs())
            .retryOnDatabaseLocked()
            .combine(privacyManager.observePrivateVaultIds()) { events, privateIds -> events.filterOutPrivate(privateIds) }
            .map { events ->
                val providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates)
                val expanded = withContext(Dispatchers.Default) {
                    expandRecurringEvents(events, start, end, providerRdates)
                        .filter { event -> event.endTimeMs >= startMs }
                }
                expanded.withGhostFlags()
            }
    }

    fun observeUpcomingAgendaEvents(startDate: LocalDate): Flow<List<CalendarEvent>> {
        return observeAgendaEvents(startDate, startDate.plusMonths(6))
    }

    /**
     * "On This Day" (FREE): master events from prior years sharing the target's month/day.
     * Reacts to the event list, Private Vault changes, and the per-day dismissal flag. Returns
     * empty once the user dismisses the card for [targetDate]. Matching/anniversary math live in
     * the pure [OnThisDayFinder]; no schema change (dismissal is a single DataStore string).
     */
    fun observeOnThisDay(targetDate: LocalDate): Flow<List<OnThisDayMemory>> {
        val zoneId = ZoneId.systemDefault()
        val targetDayStartMs = targetDate.atStartMs()
        val dismissedFlow = context.calendarPreferencesDataStore.data
            .map { it[CalendarPreferences.KEY_ON_THIS_DAY_DISMISSED_DATE] }
        return combine(
            dao.observeOnThisDayCandidates(targetDayStartMs),
            privacyManager.observePrivateVaultIds(),
            dismissedFlow,
        ) { events, privateIds, dismissedDate ->
            if (dismissedDate == targetDate.toString()) {
                emptyList()
            } else {
                val candidates = events
                    .filterOutPrivate(privateIds)
                    .map { event ->
                        val originalDate = Instant.ofEpochMilli(event.startTimeMs)
                            .atZone(zoneId)
                            .toLocalDate()
                        val isBirthday = event.source == "BIRTHDAY"
                        OnThisDayCandidate(
                            eventId = event.baseEventId(),
                            title = event.title,
                            originalDate = originalDate,
                            isBirthday = isBirthday,
                            // Contact birthdays without a real year are stored at the base year;
                            // only compute an age when the birth year is genuine.
                            birthYearKnown = isBirthday && originalDate.year != BIRTHDAY_BASE_YEAR,
                        )
                    }
                withContext(Dispatchers.Default) { OnThisDayFinder.find(targetDate, candidates) }
            }
        }
    }

    suspend fun dismissOnThisDay(date: LocalDate) {
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_ON_THIS_DAY_DISMISSED_DATE] = date.toString()
        }
    }

    suspend fun findConflictWarnings(
        startDate: LocalDate,
        endDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        excludedEventId: String?,
    ): List<CalendarEvent> {
        val zoneId = ZoneId.systemDefault()
        val preferences = context.calendarPreferencesDataStore.data.first()
        val bufferBeforeMs = (preferences[CalendarPreferences.KEY_AUTO_BUFFER_BEFORE_MINUTES] ?: 0)
            .coerceIn(0, 240) * 60_000L
        val bufferAfterMs = (preferences[CalendarPreferences.KEY_AUTO_BUFFER_AFTER_MINUTES] ?: 0)
            .coerceIn(0, 240) * 60_000L
        val startMs = startDate.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli() - bufferBeforeMs
        val endMs = endDate.atTime(endTime).atZone(zoneId).toInstant().toEpochMilli() + bufferAfterMs
        if (endMs <= startMs) return emptyList()
        val queryStartDate = startDate.minusDays(1)
        val queryEndDate = endDate.plusDays(2)
        return withContext(Dispatchers.IO) {
            val privateIds = privacyManager.observePrivateVaultIds().first()
            val providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates)
            val rawEvents = dao.getVisibleTimedEventsForConflictWarning(startMs, endMs)
                .filterOutPrivate(privateIds)
            withContext(Dispatchers.Default) {
                expandRecurringEvents(rawEvents, queryStartDate, queryEndDate, providerRdates)
                    .asSequence()
                    .filter { event -> excludedEventId == null || event.baseEventId() != excludedEventId }
                    .filter { event -> event.isAllDay == 0 && event.isCompleted == 0 && event.source != "BIRTHDAY" }
                    .filter { event ->
                        event.startTimeMs - bufferBeforeMs < endMs &&
                            event.conflictEndTimeMs() + bufferAfterMs > startMs
                    }
                    .sortedBy { event -> event.startTimeMs }
                    .toList()
            }.withGhostFlags()
        }
    }

    fun observeTasks(): Flow<List<CalendarEvent>> = dao.observeTasks()
        .combine(privacyManager.observePrivateVaultIds()) { tasks, privateIds -> tasks.filterOutPrivate(privateIds) }
        .map { tasks ->
            withContext(Dispatchers.Default) { expandRecurringTasks(tasks) }
        }

    /**
     * Global Search (FREE): one-shot text match over user events + tasks (master rows).
     * Reuses [CalendarDao.searchUserEvents] for the SQL LIKE, then drops Private Vault items
     * via the existing [filterOutPrivate] helper. Facet filtering (calendar/type/date) is
     * applied in-memory by the caller. Blank query returns empty. No schema change.
     */
    suspend fun searchItems(query: String): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext emptyList()
        val privateIds = privacyManager.observePrivateVaultIds().first()
        dao.searchUserEvents(q).filterOutPrivate(privateIds)
    }

    /**
     * Candidate source for local conversational commands. Title/location search is global; the
     * date window adds expanded occurrences so time-only references such as "my 2pm" also work.
     */
    suspend fun findSmartQuickAddCandidates(
        query: String,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val expanded = observeAgendaEvents(rangeStart, rangeEnd.plusDays(1)).first()
        val searched: List<CalendarEvent> = if (query.trim().isBlank()) emptyList() else searchItems(query)
        (expanded + searched)
            .distinctBy { it.baseEventId() }
            .take(200)
    }

    fun observeTodayTasks(day: LocalDate): Flow<List<CalendarEvent>> {
        val start = day.atStartMs()
        return dao.observeTodayTasks(start, day.plusDays(1).atStartMs() - 1)
            .combine(privacyManager.observePrivateVaultIds()) { tasks, privateIds -> tasks.filterOutPrivate(privateIds) }
    }

    fun observeUpcomingTasks(nowMs: Long = System.currentTimeMillis()): Flow<List<CalendarEvent>> =
        dao.observeUpcomingTasks(nowMs)
            .combine(privacyManager.observePrivateVaultIds()) { tasks, privateIds -> tasks.filterOutPrivate(privateIds) }

    /**
     * One-shot snapshot of the next upcoming items whose start is still in the future,
     * soonest first. Reuses the existing recurrence expansion so Glyph surfaces stay
     * consistent with the agenda. [includeTasks] adds tasks to the list (Pro). Returns
     * an empty list when nothing is coming up. No schema or storage change.
     */
    suspend fun getNextUpcomingList(
        includeTasks: Boolean,
        nowMs: Long = System.currentTimeMillis(),
        limit: Int = 5,
        includeActive: Boolean = false,
    ): List<CalendarEvent> {
        val today = Instant.ofEpochMilli(nowMs).atZone(ZoneId.systemDefault()).toLocalDate()
        val events = observeUpcomingAgendaEvents(today).first()
            .filter { event ->
                event.isTask == 0 && (event.startTimeMs > nowMs || (includeActive && event.endTimeMs > nowMs))
            }
        val tasks = if (includeTasks) {
            observeUpcomingTasks(nowMs).first()
                .filter { task ->
                    task.isCompleted == 0 && (task.startTimeMs > nowMs || (includeActive && task.endTimeMs > nowMs))
                }
        } else {
            emptyList()
        }
        return (events + tasks)
            .sortedBy { it.startTimeMs }
            .take(limit.coerceAtLeast(1))
    }

    fun observeCompletedTasks(): Flow<List<CalendarEvent>> =
        dao.observeCompletedTasks()
            .combine(privacyManager.observePrivateVaultIds()) { tasks, privateIds -> tasks.filterOutPrivate(privateIds) }

    fun observeReminders(): Flow<List<EventReminder>> = dao.observeReminders()

    suspend fun getEvent(eventId: String): CalendarEvent? = dao.getEvent(eventId)?.withGhostFlag()

    suspend fun getReminderByRequestCode(alarmRequestCode: Int): EventReminder? = dao.getReminderByRequestCode(alarmRequestCode)

    suspend fun getRemindersForEvent(eventId: String): List<EventReminder> = dao.getRemindersForEvent(eventId)

    suspend fun markReminderDelivered(alarmRequestCode: Int) {
        dao.markReminderDelivered(alarmRequestCode)
    }

    suspend fun rescheduleFutureReminders() {
        val nowMs = System.currentTimeMillis()
        val providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates)
        dao.getUndeliveredReminders().forEach { reminder ->
            dao.getEvent(reminder.eventId)?.let { event ->
                event.providerRdate = providerRdates[event.baseEventId()]
                val plan = planNextReminder(event, reminder.minutesBefore, nowMs)
                if (plan == null) {
                    dao.markReminderDelivered(reminder.alarmRequestCode)
                } else {
                    val updated = reminder.copy(triggerAtMs = plan.triggerAtMs, isDelivered = 0)
                    if (updated.triggerAtMs != reminder.triggerAtMs) {
                        dao.rescheduleReminder(reminder.id, updated.triggerAtMs)
                    }
                    reminderScheduler.scheduleReminder(updated, event, plan.occurrenceStartMs)
                }
            }
        }
    }

    suspend fun rescheduleRecurringReminder(
        reminder: EventReminder,
        event: CalendarEvent,
        currentOccurrenceStartMs: Long,
    ): Boolean {
        event.providerRdate = sideStore.read(EventSideStoreNamespaces.ProviderRdates, event.baseEventId())
        if (event.rrule.isNullOrBlank() && event.providerRdate.isNullOrBlank()) return false
        val plan = planNextReminder(event, reminder.minutesBefore, maxOf(System.currentTimeMillis(), currentOccurrenceStartMs))
            ?: return false
        dao.rescheduleReminder(reminder.id, plan.triggerAtMs)
        reminderScheduler.scheduleReminder(reminder.copy(triggerAtMs = plan.triggerAtMs), event, plan.occurrenceStartMs)
        return true
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
    ): CalendarEvent? {
        require(data.title.isNotBlank()) { "TITLE REQUIRED" }
        ensureLocalAccount()
        val zoneId = ZoneId.systemDefault()
        if (existing?.isRecurrenceOccurrence() == true && recurringEditScope == RecurringEditScope.ThisEvent) {
            return saveDetachedOccurrence(existing, data, zoneId)
        }
        if (existing?.isRecurrenceOccurrence() == true && recurringEditScope == RecurringEditScope.ThisAndFollowing) {
            return saveThisAndFollowingOccurrence(existing, data, zoneId)
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
        val targetAccountId = data.accountId ?: existingMaster?.accountId ?: existing?.accountId ?: LOCAL_ACCOUNT_ID
        val draftEvent = existingMaster?.copy(
            id = eventId,
            accountId = targetAccountId,
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            colorHex = data.colorHex,
            rrule = data.rrule,
            imageUris = data.imageUris,
            voiceNotePath = data.voiceNotePath,
            updatedAtMs = now,
        ) ?: CalendarEvent(
                id = eventId,
                accountId = targetAccountId,
                title = data.title.trim(),
                description = data.description.trim(),
                location = data.location.trim(),
                startTimeMs = start,
                endTimeMs = end,
                timeZone = zoneId.id,
                isAllDay = if (data.isAllDay) 1 else 0,
                colorHex = data.colorHex,
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
        val reminderMinutes = data.reminderMinutesList
            ?.distinct()
            ?.sorted()
            ?: data.reminderMinutes?.let { listOf(it) }
            ?: emptyList()
        val event = syncProviderBackedEvent(existingMaster ?: existing, draftEvent, reminderMinutes)
        if (event.id != eventId) {
            dao.getRemindersForEvent(eventId).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
            dao.deleteRemindersForEvent(eventId)
            dao.deleteEvent(eventId)
            writeGhostFlag(eventId, false)
            sideStore.remove(SHIFT_EVENT_METADATA_NAMESPACE, eventId)
        }
        dao.getRemindersForEvent(event.id).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        dao.upsertEvent(event)
        writeGhostFlag(event.id, data.isGhost)
        replaceEventFileAttachments(event.id, data.fileAttachments, previousEventId = eventId)
        dao.deleteRemindersForEvent(event.id)
        reminderMinutes.forEach { minutes ->
            val reminder = EventReminder(
                eventId = event.id,
                minutesBefore = minutes,
                triggerAtMs = start - minutes * 60_000L,
                alarmRequestCode = providerReminderAlarmRequestCode(event.id, minutes),
            )
            dao.insertReminders(listOf(reminder))
            reminderScheduler.scheduleReminder(reminder, event)
        }
        updateWidgets()
        return event
    }

    suspend fun deleteLocalEvent(
        event: CalendarEvent,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
    ) {
        if (event.isRecurrenceOccurrence() && recurringEditScope == RecurringEditScope.ThisEvent) {
            val providerOccurrenceCanceled = event.googleEventId
                ?.takeIf { event.source == "GOOGLE" }
                ?.let { googleEventId ->
                    withContext(Dispatchers.IO) {
                        calendarProviderDataSource.cancelEventOccurrence(
                            googleEventId = googleEventId,
                            occurrenceStartMs = event.startTimeMs,
                            occurrenceEndMs = event.endTimeMs,
                            isAllDay = event.isAllDay,
                            timeZone = event.timeZone,
                        )
                    }
                } == true
            excludeOccurrence(event, syncProvider = !providerOccurrenceCanceled)
            return
        }
        if (event.isRecurrenceOccurrence() && recurringEditScope == RecurringEditScope.ThisAndFollowing) {
            truncateSeriesBeforeOccurrence(event)
            return
        }
        val eventId = event.baseEventId()
        val reminders = dao.getRemindersForEvent(eventId)
        reminders.forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        val master = dao.getEvent(eventId) ?: event
        recentlyDeletedStore.save(master, reminders, System.currentTimeMillis())
        event.googleEventId?.let { googleEventId ->
            withContext(Dispatchers.IO) {
                calendarProviderDataSource.deleteEvent(googleEventId)
            }
            dao.insertDeletedEventLog(
                DeletedEventLog(
                    googleEventId = googleEventId,
                    deletedAtMs = System.currentTimeMillis(),
                ),
            )
        }
        dao.deleteRemindersForEvent(eventId)
        dao.deleteEvent(eventId)
        sideStore.remove(SHIFT_EVENT_METADATA_NAMESPACE, eventId)
        updateWidgets()
    }

    private suspend fun syncProviderBackedEvent(
        previous: CalendarEvent?,
        event: CalendarEvent,
        reminderMinutes: List<Int> = emptyList(),
    ): CalendarEvent {
        val targetCalendarId = providerCalendarId(event.accountId)
        if (targetCalendarId == null) {
            previous?.googleEventId?.let { googleEventId ->
                withContext(Dispatchers.IO) {
                    calendarProviderDataSource.deleteEvent(googleEventId)
                }
            }
            return event.copy(
                source = "LOCAL",
                googleEventId = null,
                googleCalendarId = null,
                syncVersion = 0,
            )
        }

        val previousCalendarId = previous?.googleCalendarId
        val providerDraft = event.copy(
            source = "GOOGLE",
            googleEventId = previous?.googleEventId.takeIf { previousCalendarId == targetCalendarId.toString() },
            googleCalendarId = targetCalendarId.toString(),
        ).apply {
            val previousBaseId = previous?.baseEventId()
            providerStatus = previousBaseId
                ?.let { sideStore.read(EventSideStoreNamespaces.ProviderStatuses, it)?.toIntOrNull() }
            providerRdate = previousBaseId
                ?.let { sideStore.read(EventSideStoreNamespaces.ProviderRdates, it) }
        }
        val providerEvent = withContext(Dispatchers.IO) {
            calendarProviderDataSource.saveEvent(targetCalendarId, providerDraft, reminderMinutes)
        } ?: return event
        if (previous?.googleEventId != null && previousCalendarId != targetCalendarId.toString()) {
            withContext(Dispatchers.IO) {
                calendarProviderDataSource.deleteEvent(previous.googleEventId)
            }
        }
        return event.withProviderSyncResult(providerEvent)
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
        val reminders = dao.getRemindersForEvent(taskId)
        reminders.forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        val master = dao.getEvent(taskId) ?: task
        recentlyDeletedStore.save(master, reminders, System.currentTimeMillis())
        dao.deleteRemindersForEvent(taskId)
        dao.deleteEvent(taskId)
        updateWidgets()
    }

    suspend fun bulkShiftEvents(eventIds: Set<String>, days: Long, hours: Long): BulkEditResult = withContext(Dispatchers.IO) {
        val deltaMs = days * 86_400_000L + hours * 3_600_000L
        if (deltaMs == 0L) return@withContext BulkEditResult(0, 0, BulkEditUndoToken())
        val candidates = loadBulkEditableMasters(eventIds)
        val editable = candidates.filterNot { it.source == "BIRTHDAY" }
        val reminders = editable.flatMap { dao.getRemindersForEvent(it.id) }
        val now = System.currentTimeMillis()
        val shifted = editable.map { event ->
            event.copy(
                startTimeMs = event.startTimeMs + deltaMs,
                endTimeMs = event.endTimeMs + deltaMs,
                updatedAtMs = now,
            )
        }
        val shiftedReminders = reminders.map { it.copy(triggerAtMs = it.triggerAtMs + deltaMs, isDelivered = 0) }
        applyBulkEventUpdates(shifted, shiftedReminders)
        BulkEditResult(
            changedCount = shifted.size,
            skippedCount = candidates.size - editable.size,
            undoToken = BulkEditUndoToken(previousEvents = editable, previousReminders = reminders),
        )
    }

    suspend fun rescheduleEvent(
        event: CalendarEvent,
        targetStart: LocalDateTime,
        targetEnd: LocalDateTime,
        recurringEditScope: RecurringEditScope,
    ): BulkEditResult = withContext(Dispatchers.IO) {
        require(event.isAllDay == 0 && event.isTask == 0 && event.source != "BIRTHDAY") {
            "EVENT CANNOT BE RESCHEDULED"
        }
        require(targetEnd.isAfter(targetStart)) { "END MUST BE AFTER START" }

        val master = dao.getEvent(event.baseEventId()) ?: event
        val previousReminders = dao.getRemindersForEvent(master.id)
        val detachedId = if (event.isRecurrenceOccurrence() && recurringEditScope == RecurringEditScope.ThisEvent) {
            UUID.randomUUID().toString()
        } else {
            null
        }
        val targetRange = if (event.isRecurrenceOccurrence() && recurringEditScope == RecurringEditScope.WholeSeries) {
            val zoneId = safeZoneId(event.timeZone)
            val occurrenceStart = Instant.ofEpochMilli(event.startTimeMs).atZone(zoneId).toLocalDateTime()
            val occurrenceEnd = Instant.ofEpochMilli(event.endTimeMs).atZone(zoneId).toLocalDateTime()
            val masterStart = Instant.ofEpochMilli(master.startTimeMs).atZone(zoneId).toLocalDateTime()
            val masterEnd = Instant.ofEpochMilli(master.endTimeMs).atZone(zoneId).toLocalDateTime()
            val adjusted = EventDragMath.applyOccurrenceChangeToSeries(
                master = EventTimeRange(masterStart, masterEnd),
                occurrence = EventTimeRange(occurrenceStart, occurrenceEnd),
                target = EventTimeRange(targetStart, targetEnd),
            )
            adjusted.start to adjusted.end
        } else {
            targetStart to targetEnd
        }
        val reminderMinutes = previousReminders.map { it.minutesBefore }.distinct().sorted()
        saveLocalEvent(
            existing = if (
                event.isRecurrenceOccurrence() &&
                recurringEditScope != RecurringEditScope.WholeSeries
            ) {
                event
            } else {
                master
            },
            data = EventEditorData(
                eventId = detachedId ?: master.id,
                accountId = master.accountId,
                title = master.title,
                description = master.description,
                location = master.location,
                date = targetRange.first.toLocalDate(),
                endDate = targetRange.second.toLocalDate(),
                startTime = targetRange.first.toLocalTime(),
                endTime = targetRange.second.toLocalTime(),
                isAllDay = false,
                reminderMinutes = reminderMinutes.firstOrNull(),
                reminderMinutesList = reminderMinutes.takeIf { it.isNotEmpty() },
                rrule = master.rrule,
                imageUris = master.imageUris,
                voiceNotePath = master.voiceNotePath,
                colorHex = master.colorHex,
            ),
            recurringEditScope = recurringEditScope,
        )
        BulkEditResult(
            changedCount = 1,
            undoToken = BulkEditUndoToken(
                previousEvents = listOf(master),
                previousReminders = previousReminders,
                insertedEventIds = listOfNotNull(detachedId),
            ),
        )
    }

    suspend fun bulkMoveToDate(eventIds: Set<String>, targetDate: LocalDate): BulkEditResult = withContext(Dispatchers.IO) {
        val candidates = loadBulkEditableMasters(eventIds)
        val editable = candidates.filterNot { it.source == "BIRTHDAY" }
        val reminders = editable.flatMap { dao.getRemindersForEvent(it.id) }
        val zoneId = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val moved = editable.map { event ->
            val start = Instant.ofEpochMilli(event.startTimeMs).atZone(zoneId)
            val end = Instant.ofEpochMilli(event.endTimeMs).atZone(zoneId)
            val dayDelta = ChronoUnit.DAYS.between(start.toLocalDate(), targetDate)
            event.copy(
                startTimeMs = start.plusDays(dayDelta).toInstant().toEpochMilli(),
                endTimeMs = end.plusDays(dayDelta).toInstant().toEpochMilli(),
                timeZone = zoneId.id,
                updatedAtMs = now,
            )
        }
        val deltaById = editable.zip(moved).associate { (before, after) -> before.id to after.startTimeMs - before.startTimeMs }
        val movedReminders = reminders.map { reminder ->
            reminder.copy(triggerAtMs = reminder.triggerAtMs + (deltaById[reminder.eventId] ?: 0L), isDelivered = 0)
        }
        applyBulkEventUpdates(moved, movedReminders)
        BulkEditResult(
            changedCount = moved.size,
            skippedCount = candidates.size - editable.size,
            undoToken = BulkEditUndoToken(previousEvents = editable, previousReminders = reminders),
        )
    }

    suspend fun bulkCopyToDate(eventIds: Set<String>, targetDate: LocalDate): BulkEditResult = withContext(Dispatchers.IO) {
        ensureLocalAccount()
        val candidates = loadBulkEditableMasters(eventIds)
        val editable = candidates.filterNot { it.source == "BIRTHDAY" }
        val zoneId = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val copies = mutableListOf<CalendarEvent>()
        val copyReminders = mutableListOf<EventReminder>()
        editable.forEach { event ->
            val copyId = UUID.randomUUID().toString()
            val start = Instant.ofEpochMilli(event.startTimeMs).atZone(zoneId)
            val end = Instant.ofEpochMilli(event.endTimeMs).atZone(zoneId)
            val dayDelta = ChronoUnit.DAYS.between(start.toLocalDate(), targetDate)
            val newStart = start.plusDays(dayDelta).toInstant().toEpochMilli()
            val newEnd = end.plusDays(dayDelta).toInstant().toEpochMilli()
            copies += event.copy(
                id = copyId,
                accountId = LOCAL_ACCOUNT_ID,
                startTimeMs = newStart,
                endTimeMs = newEnd,
                timeZone = zoneId.id,
                source = "LOCAL",
                googleEventId = null,
                googleCalendarId = null,
                syncVersion = 0,
                imageUris = "[]",
                voiceNotePath = null,
                createdAtMs = now,
                updatedAtMs = now,
            )
            val deltaMs = newStart - event.startTimeMs
            dao.getRemindersForEvent(event.id).forEach { reminder ->
                copyReminders += reminder.copy(
                    eventId = copyId,
                    triggerAtMs = reminder.triggerAtMs + deltaMs,
                    alarmRequestCode = "$copyId-${reminder.minutesBefore}".hashCode().absoluteValue,
                    isDelivered = 0,
                )
            }
        }
        if (copies.isNotEmpty()) dao.upsertEvents(copies)
        if (copyReminders.isNotEmpty()) dao.insertReminders(copyReminders)
        copies.forEach { event ->
            copyReminders
                .filter { it.eventId == event.id && it.triggerAtMs > now && it.isDelivered == 0 }
                .forEach { reminder -> reminderScheduler.scheduleReminder(reminder, event) }
        }
        updateWidgets()
        BulkEditResult(
            changedCount = copies.size,
            skippedCount = candidates.size - editable.size,
            undoToken = BulkEditUndoToken(insertedEventIds = copies.map { it.id }),
        )
    }

    suspend fun bulkChangeCalendar(eventIds: Set<String>, accountId: String): BulkEditResult = withContext(Dispatchers.IO) {
        val candidates = loadBulkEditableMasters(eventIds)
        val accountExists = dao.getAccount(accountId) != null
        val editable = candidates.filter { accountExists && it.source == "LOCAL" }
        val reminders = editable.flatMap { dao.getRemindersForEvent(it.id) }
        val now = System.currentTimeMillis()
        val moved = editable.map { it.copy(accountId = accountId, updatedAtMs = now) }
        if (moved.isNotEmpty()) dao.upsertEvents(moved)
        updateWidgets()
        BulkEditResult(
            changedCount = moved.size,
            skippedCount = candidates.size - editable.size,
            undoToken = BulkEditUndoToken(previousEvents = editable, previousReminders = reminders),
        )
    }

    suspend fun bulkChangeColor(eventIds: Set<String>, colorHex: String?): BulkEditResult = withContext(Dispatchers.IO) {
        val candidates = loadBulkEditableMasters(eventIds)
        val editable = candidates.filterNot { it.source == "BIRTHDAY" || it.source == "HOLIDAY" }
        val reminders = editable.flatMap { dao.getRemindersForEvent(it.id) }
        val now = System.currentTimeMillis()
        val recolored = editable.map { it.copy(colorHex = colorHex, updatedAtMs = now) }
        if (recolored.isNotEmpty()) dao.upsertEvents(recolored)
        updateWidgets()
        BulkEditResult(
            changedCount = recolored.size,
            skippedCount = candidates.size - editable.size,
            undoToken = BulkEditUndoToken(previousEvents = editable, previousReminders = reminders),
        )
    }

    suspend fun bulkDeleteEvents(eventIds: Set<String>): BulkEditResult = withContext(Dispatchers.IO) {
        val candidates = loadBulkEditableMasters(eventIds)
        val editable = candidates.filterNot { it.source == "BIRTHDAY" }
        val reminders = editable.flatMap { dao.getRemindersForEvent(it.id) }
        editable.forEach { event ->
            val eventReminders = reminders.filter { it.eventId == event.id }
            eventReminders.forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
            recentlyDeletedStore.save(event, eventReminders, System.currentTimeMillis())
            event.googleEventId?.let { googleEventId ->
                dao.insertDeletedEventLog(
                    DeletedEventLog(
                        googleEventId = googleEventId,
                        deletedAtMs = System.currentTimeMillis(),
                    ),
                )
            }
            dao.deleteRemindersForEvent(event.id)
            dao.deleteEvent(event.id)
        }
        updateWidgets()
        BulkEditResult(
            changedCount = editable.size,
            skippedCount = candidates.size - editable.size,
            undoToken = BulkEditUndoToken(
                previousEvents = editable,
                previousReminders = reminders,
                deletedSnapshotIds = editable.map { it.id },
            ),
        )
    }

    suspend fun bulkToggleGhost(eventIds: Set<String>): BulkEditResult = withContext(Dispatchers.IO) {
        val editable = loadBulkEditableMasters(eventIds).filterNot { it.source == "BIRTHDAY" }
        val previousFlags = linkedMapOf<String, Boolean>()
        editable.forEach { event ->
            previousFlags[event.id] = sideStore.read(GHOST_FLAGS_NAMESPACE, event.id) == "1"
        }
        val shouldGhost = editable.any { previousFlags[it.id] != true }
        editable.forEach { event ->
            if (shouldGhost) {
                sideStore.write(GHOST_FLAGS_NAMESPACE, event.id, "1")
            } else {
                sideStore.remove(GHOST_FLAGS_NAMESPACE, event.id)
            }
        }
        if (editable.isNotEmpty()) {
            val now = System.currentTimeMillis()
            dao.upsertEvents(editable.map { it.copy(updatedAtMs = now) })
        }
        updateWidgets()
        BulkEditResult(
            changedCount = editable.size,
            undoToken = BulkEditUndoToken(previousGhostFlags = previousFlags),
        )
    }

    suspend fun undoBulkEdit(token: BulkEditUndoToken) = withContext(Dispatchers.IO) {
        token.insertedEventIds.forEach { eventId ->
            dao.getRemindersForEvent(eventId).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
            dao.deleteRemindersForEvent(eventId)
            dao.deleteEvent(eventId)
        }
        if (token.previousEvents.isNotEmpty()) {
            dao.upsertEvents(token.previousEvents)
            token.previousEvents.forEach { dao.deleteRemindersForEvent(it.id) }
        }
        if (token.previousReminders.isNotEmpty()) {
            dao.insertReminders(token.previousReminders)
            val now = System.currentTimeMillis()
            token.previousReminders
                .filter { it.triggerAtMs > now && it.isDelivered == 0 }
                .forEach { reminder ->
                    token.previousEvents.firstOrNull { it.id == reminder.eventId }?.let { event ->
                        reminderScheduler.scheduleReminder(reminder, event)
                    }
                }
        }
        token.previousGhostFlags.forEach { (eventId, ghosted) ->
            if (ghosted) {
                sideStore.write(GHOST_FLAGS_NAMESPACE, eventId, "1")
            } else {
                sideStore.remove(GHOST_FLAGS_NAMESPACE, eventId)
            }
        }
        token.deletedSnapshotIds.forEach { recentlyDeletedStore.remove(it) }
        updateWidgets()
    }

    private suspend fun loadBulkEditableMasters(eventIds: Set<String>): List<CalendarEvent> {
        return eventIds
            .map { it.substringBefore(RECURRENCE_OCCURRENCE_SEPARATOR) }
            .distinct()
            .mapNotNull { dao.getEvent(it) }
            .filter { it.isTask == 0 }
    }

    private suspend fun applyBulkEventUpdates(events: List<CalendarEvent>, reminders: List<EventReminder>) {
        val now = System.currentTimeMillis()
        events.forEach { event ->
            dao.getRemindersForEvent(event.id).forEach { reminderScheduler.cancelReminder(it.alarmRequestCode) }
        }
        if (events.isNotEmpty()) dao.upsertEvents(events)
        events.forEach { dao.deleteRemindersForEvent(it.id) }
        if (reminders.isNotEmpty()) dao.insertReminders(reminders)
        events.forEach { event ->
            reminders
                .filter { it.eventId == event.id && it.triggerAtMs > now && it.isDelivered == 0 }
                .forEach { reminder -> reminderScheduler.scheduleReminder(reminder, event) }
        }
        updateWidgets()
    }

    // ----- Recently Deleted (file-based trash; no Room/schema change) -----

    /** Snapshots of deleted events/tasks within the 30-day window, newest first. */
    suspend fun listRecentlyDeleted(): List<DeletedSnapshot> = withContext(Dispatchers.IO) {
        recentlyDeletedStore.pruneExpired(System.currentTimeMillis()).forEach { eventId ->
            deleteEventFileAttachments(eventId)
        }
        recentlyDeletedStore.list(System.currentTimeMillis())
    }

    /**
     * Restore a deleted event/task back into the calendar. Re-inserts the row and its
     * reminders, reschedules any still-future reminders, and drops the snapshot.
     * Returns false if the snapshot is gone.
     */
    suspend fun restoreDeleted(eventId: String): Boolean = withContext(Dispatchers.IO) {
        val snapshot = recentlyDeletedStore.get(eventId) ?: return@withContext false
        ensureLocalAccount()
        // The original account may have been removed; fall back to the local account
        // so the FK constraint holds and the event is never lost.
        val accountId = if (dao.getAccount(snapshot.event.accountId) != null) {
            snapshot.event.accountId
        } else {
            LOCAL_ACCOUNT_ID
        }
        val event = snapshot.event.copy(accountId = accountId)
        dao.upsertEvent(event)
        dao.deleteRemindersForEvent(event.id)
        if (snapshot.reminders.isNotEmpty()) {
            dao.insertReminders(snapshot.reminders)
            val now = System.currentTimeMillis()
            snapshot.reminders
                .filter { it.triggerAtMs > now && it.isDelivered == 0 }
                .forEach { reminderScheduler.scheduleReminder(it, event) }
        }
        recentlyDeletedStore.remove(eventId)
        updateWidgets()
        true
    }

    /** Permanently drop one snapshot from the trash. */
    suspend fun purgeDeleted(eventId: String) = withContext(Dispatchers.IO) {
        deleteEventFileAttachments(eventId)
        recentlyDeletedStore.remove(eventId)
    }

    /** Empty the entire trash. */
    suspend fun emptyRecentlyDeleted() = withContext(Dispatchers.IO) {
        recentlyDeletedStore.list(System.currentTimeMillis()).forEach { snapshot ->
            deleteEventFileAttachments(snapshot.event.id)
        }
        recentlyDeletedStore.clear()
    }

    // ----- Event/Task Templates (file-based, Pro) -----

    /** All saved templates, newest first. */
    suspend fun listTemplates(): List<EventTemplate> = withContext(Dispatchers.IO) {
        eventTemplateStore.list()
    }

    /** Insert or overwrite a template. */
    suspend fun saveTemplate(template: EventTemplate) = withContext(Dispatchers.IO) {
        eventTemplateStore.save(template)
    }

    /** Permanently delete a template. */
    suspend fun deleteTemplate(id: String) = withContext(Dispatchers.IO) {
        eventTemplateStore.remove(id)
    }

    suspend fun applyTemplateToDates(
        templateId: String,
        dates: List<LocalDate>,
        accountId: String?,
    ): Int = withContext(Dispatchers.IO) {
        val template = eventTemplateStore.list().firstOrNull { it.id == templateId } ?: return@withContext 0
        val cleanDates = dates.distinct().sorted()
        for (date in cleanDates) {
            val startMinute = template.startMinuteOfDay ?: 0
            val startTime = LocalTime.of(startMinute / 60, startMinute % 60)
            val endDateTime = date.atTime(startTime).plusMinutes(template.durationMinutes.coerceAtLeast(1).toLong())
            saveLocalEvent(
                existing = null,
                data = EventEditorData(
                    accountId = accountId ?: template.accountId,
                    title = template.title.ifBlank { template.name },
                    description = template.description,
                    location = template.location,
                    date = date,
                    endDate = if (template.isAllDay || template.startMinuteOfDay == null) date else endDateTime.toLocalDate(),
                    startTime = startTime,
                    endTime = if (template.isAllDay || template.startMinuteOfDay == null) LocalTime.of(23, 59) else endDateTime.toLocalTime(),
                    isAllDay = template.isAllDay || template.startMinuteOfDay == null,
                    reminderMinutes = template.reminderMinutes,
                    rrule = null,
                ),
            )
        }
        cleanDates.size
    }

    // ----- Calendar Sets / Focus Profiles (file-based, Pro) -----

    /** All saved calendar sets, newest first. */
    suspend fun listFocusProfiles(): List<FocusProfile> = withContext(Dispatchers.IO) {
        focusProfileStore.list()
    }

    /** Insert or overwrite a calendar set. */
    suspend fun saveFocusProfile(profile: FocusProfile) = withContext(Dispatchers.IO) {
        focusProfileStore.save(profile)
    }

    /** Permanently delete a calendar set. */
    suspend fun deleteFocusProfile(id: String) = withContext(Dispatchers.IO) {
        focusProfileStore.remove(id)
    }

    /** Show only the calendars saved in this set; hide every other calendar. LOCAL_ACCOUNT_ID always stays visible. */
    suspend fun applyFocusProfile(id: String) = withContext(Dispatchers.IO) {
        val profile = focusProfileStore.list().firstOrNull { it.id == id } ?: return@withContext
        val accounts = dao.getAccountsForWidgetConfig()
        for (account in accounts) {
            if (account.id == LOCAL_ACCOUNT_ID) continue
            val shouldBeVisible = account.id in profile.accountIds
            if ((account.isVisible == 1) != shouldBeVisible) {
                dao.updateAccountVisibility(account.id, if (shouldBeVisible) 1 else 0)
            }
        }
        updateWidgets()
    }

    // ----- Shift Patterns (file-based, Pro) -----

    suspend fun listShiftTypes(): List<ShiftType> = withContext(Dispatchers.IO) {
        shiftPatternStore.listTypes()
    }

    suspend fun saveShiftType(type: ShiftType) = withContext(Dispatchers.IO) {
        shiftPatternStore.saveType(type)
    }

    suspend fun deleteShiftType(id: String) = withContext(Dispatchers.IO) {
        shiftPatternStore.removeType(id)
    }

    suspend fun listShiftPatterns(): List<ShiftPattern> = withContext(Dispatchers.IO) {
        shiftPatternStore.listPatterns()
    }

    suspend fun saveShiftPattern(pattern: ShiftPattern) = withContext(Dispatchers.IO) {
        shiftPatternStore.savePattern(pattern)
    }

    suspend fun addShiftOnDate(
        shiftTypeId: String,
        date: LocalDate,
        accountId: String?,
    ): Boolean = withContext(Dispatchers.IO) {
        val type = shiftPatternStore.listTypes().firstOrNull { it.id == shiftTypeId } ?: return@withContext false
        saveShiftOccurrence(
            occurrence = GeneratedShiftOccurrence(date, type),
            accountId = accountId,
            metadata = shiftMetadataFor(type, date, ShiftEventGeneratedBy.Manual),
        ) != null
    }

    suspend fun listShiftEventMetadata(eventIds: Collection<String>): Map<String, ShiftEventMetadata> = withContext(Dispatchers.IO) {
        val ids = eventIds.map { it.substringBefore(RECURRENCE_OCCURRENCE_SEPARATOR) }.toSet()
        sideStore.readNamespace(SHIFT_EVENT_METADATA_NAMESPACE)
            .filterKeys { it in ids }
            .mapNotNull { (id, value) -> parseShiftEventMetadata(value)?.let { id to it } }
            .toMap()
    }

    suspend fun deleteShiftPattern(id: String, removeGeneratedEvents: Boolean) = withContext(Dispatchers.IO) {
        if (removeGeneratedEvents) removeGeneratedShiftEvents(id)
        shiftPatternStore.removePattern(id)
        shiftPatternStore.removeGenerationsForPattern(id)
    }

    suspend fun applyShiftPattern(
        patternId: String,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        accountId: String?,
    ): ShiftApplyResult = withContext(Dispatchers.IO) {
        val pattern = shiftPatternStore.listPatterns().firstOrNull { it.id == patternId }
            ?: return@withContext ShiftApplyResult(generatedCount = 0)
        val shiftTypes = shiftPatternStore.listTypes().associateBy { it.id }
        val overlapping = shiftPatternStore.listGenerations()
            .filter { it.patternId == patternId && rangesOverlap(it.rangeStart, it.rangeEnd, rangeStart, rangeEnd) }
        val replaced = overlapping.sumOf { it.eventIds.size }
        overlapping.forEach { record ->
            record.eventIds.forEach { eventId ->
                dao.getEvent(eventId)?.let { deleteLocalEvent(it) }
            }
            shiftPatternStore.removeGeneration(record.id)
        }
        val occurrences = expandShiftPattern(pattern, shiftTypes, rangeStart, rangeEnd)
        val eventIds = saveShiftOccurrences(occurrences, accountId, ShiftEventGeneratedBy.Pattern, patternId)
        shiftPatternStore.saveGeneration(
            ShiftGenerationRecord(
                id = ShiftGenerationRecord.newId(),
                patternId = patternId,
                generatedAtMs = System.currentTimeMillis(),
                eventIds = eventIds,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
            ),
        )
        ShiftApplyResult(generatedCount = eventIds.size, replacedCount = replaced)
    }

    suspend fun buildShiftPlanShareEvents(
        patternId: String,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val pattern = shiftPatternStore.listPatterns().firstOrNull { it.id == patternId }
            ?: return@withContext emptyList()
        val shiftTypes = shiftPatternStore.listTypes().associateBy { it.id }
        buildShiftPlanShareEvents(pattern, shiftTypes, rangeStart, rangeEnd)
    }

    private suspend fun removeGeneratedShiftEvents(patternId: String) {
        shiftPatternStore.listGenerations()
            .filter { it.patternId == patternId }
            .forEach { record ->
                record.eventIds.forEach { eventId ->
                    dao.getEvent(eventId)?.let { deleteLocalEvent(it) }
                }
                shiftPatternStore.removeGeneration(record.id)
            }
    }

    private suspend fun saveShiftOccurrences(
        occurrences: List<GeneratedShiftOccurrence>,
        accountId: String?,
        generatedBy: ShiftEventGeneratedBy,
        patternId: String? = null,
    ): List<String> = occurrences.mapNotNull { occurrence ->
        saveShiftOccurrence(
            occurrence = occurrence,
            accountId = accountId,
            metadata = shiftMetadataFor(occurrence.shiftType, occurrence.date, generatedBy, patternId),
        )?.id
    }

    private suspend fun saveShiftOccurrence(
        occurrence: GeneratedShiftOccurrence,
        accountId: String?,
        metadata: ShiftEventMetadata,
    ): CalendarEvent? {
        ensureLocalAccount()
        val draft = buildShiftEventDraft(occurrence.shiftType, occurrence.date) ?: return null
        val zoneId = ZoneId.systemDefault()
        val start = if (draft.isAllDay) {
            draft.date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            draft.date.atTime(draft.startTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        val end = if (draft.isAllDay) {
            draft.endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            draft.endDate.atTime(draft.endTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        if (end <= start) return null
        val now = System.currentTimeMillis()
        val event = CalendarEvent(
            id = UUID.randomUUID().toString(),
            accountId = accountId ?: LOCAL_ACCOUNT_ID,
            title = draft.title,
            description = "",
            location = "",
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (draft.isAllDay) 1 else 0,
            colorHex = draft.colorHex,
            rrule = null,
            exceptionDates = "[]",
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            syncVersion = 0,
            isTask = 0,
            isCompleted = 0,
            completedAtMs = null,
            imageUris = "[]",
            voiceNotePath = null,
            createdAtMs = now,
            updatedAtMs = now,
        )
        val providerReminderMinutes = draft.reminderMinutes?.let(::listOf).orEmpty()
        val saved = syncProviderBackedEvent(null, event, providerReminderMinutes)
        dao.upsertEvent(saved)
        draft.reminderMinutes?.let { minutes ->
            val reminder = EventReminder(
                eventId = saved.id,
                minutesBefore = minutes,
                triggerAtMs = start - minutes * 60_000L,
                alarmRequestCode = providerReminderAlarmRequestCode(saved.id, minutes),
            )
            dao.insertReminders(listOf(reminder))
            reminderScheduler.scheduleReminder(reminder, saved)
        }
        sideStore.write(SHIFT_EVENT_METADATA_NAMESPACE, saved.id, metadata.encode())
        updateWidgets()
        return saved
    }

    private fun rangesOverlap(aStart: LocalDate, aEnd: LocalDate, bStart: LocalDate, bEnd: LocalDate): Boolean =
        !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart)

    private fun LocalDate.atStartMs(): Long {
        return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun CalendarEvent.conflictEndTimeMs(): Long {
        return endTimeMs.coerceAtLeast(startTimeMs + 15 * 60 * 1000L)
    }

    private suspend fun saveDetachedOccurrence(existing: CalendarEvent, data: EventEditorData, zoneId: ZoneId): CalendarEvent? {
        val master = excludeOccurrence(existing) ?: return null
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
            accountId = data.accountId ?: master.accountId,
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            colorHex = data.colorHex,
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
        val reminderMinutes = data.reminderMinutesList
            ?.distinct()
            ?.sorted()
            ?: data.reminderMinutes?.let(::listOf)
            ?: emptyList()
        val syncedDetachedEvent = syncProviderBackedEvent(null, detachedEvent, reminderMinutes)
        dao.upsertEvent(syncedDetachedEvent)
        writeGhostFlag(syncedDetachedEvent.id, data.isGhost)
        replaceEventFileAttachments(syncedDetachedEvent.id, data.fileAttachments, previousEventId = detachedId)
        reminderMinutes.forEach { minutes ->
            val reminder = EventReminder(
                eventId = syncedDetachedEvent.id,
                minutesBefore = minutes,
                triggerAtMs = start - minutes * 60_000L,
                alarmRequestCode = providerReminderAlarmRequestCode(syncedDetachedEvent.id, minutes),
            )
            dao.insertReminders(listOf(reminder))
            reminderScheduler.scheduleReminder(reminder, syncedDetachedEvent)
        }
        updateWidgets()
        return syncedDetachedEvent
    }

    private suspend fun excludeOccurrence(event: CalendarEvent, syncProvider: Boolean = true): CalendarEvent? {
        val occurrenceStartMs = event.occurrenceStartMs() ?: return null
        val masterId = event.baseEventId()
        val master = dao.getEvent(masterId) ?: return null
        val exceptions = master.exceptionStartTimes() + occurrenceStartMs
        val updatedMaster = master.copy(
            exceptionDates = exceptions.sorted().joinToString(prefix = "[", postfix = "]"),
            updatedAtMs = System.currentTimeMillis(),
        )
        val reminderMinutes = dao.getRemindersForEvent(master.id).map { it.minutesBefore }
        val syncedMaster = if (syncProvider) {
            syncProviderBackedEvent(master, updatedMaster, reminderMinutes)
        } else {
            updatedMaster
        }
        dao.upsertEvent(syncedMaster)
        updateWidgets()
        return syncedMaster
    }

    private suspend fun saveThisAndFollowingOccurrence(existing: CalendarEvent, data: EventEditorData, zoneId: ZoneId): CalendarEvent? {
        val occurrenceStartMs = existing.occurrenceStartMs() ?: return null
        val masterId = existing.baseEventId()
        val master = dao.getEvent(masterId) ?: existing.copy(id = masterId)
        if (master.rrule.isNullOrBlank()) {
            return saveLocalEvent(master, data, RecurringEditScope.WholeSeries)
        }
        val splitDate = Instant.ofEpochMilli(occurrenceStartMs).atZone(safeZoneId(master.timeZone)).toLocalDate()
        if (splitDate <= master.startDate()) {
            return saveLocalEvent(master, data, RecurringEditScope.WholeSeries)
        }
        val oldReminders = dao.getRemindersForEvent(master.id)
        val oldReminderMinutes = oldReminders.map { it.minutesBefore }.distinct().sorted()
        val oldEvent = master.copy(
            rrule = truncateRRuleBefore(master.rrule, splitDate),
            exceptionDates = master.exceptionStartTimes()
                .filter { it < occurrenceStartMs }
                .joinToString(prefix = "[", postfix = "]"),
            updatedAtMs = System.currentTimeMillis(),
        )
        val syncedOldEvent = syncProviderBackedEvent(master, oldEvent, oldReminderMinutes)
        dao.upsertEvent(syncedOldEvent)

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
        val newEventId = data.eventId?.takeIf { it != master.id } ?: UUID.randomUUID().toString()
        val newEvent = master.copy(
            id = newEventId,
            accountId = data.accountId ?: master.accountId,
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            colorHex = data.colorHex,
            rrule = data.rrule,
            exceptionDates = "[]",
            googleEventId = null,
            googleCalendarId = null,
            syncVersion = 0,
            imageUris = data.imageUris,
            voiceNotePath = data.voiceNotePath,
            createdAtMs = now,
            updatedAtMs = now,
        )
        val reminderMinutes = data.reminderMinutesList
            ?.distinct()
            ?.sorted()
            ?: data.reminderMinutes?.let(::listOf)
            ?: emptyList()
        val syncedNewEvent = syncProviderBackedEvent(null, newEvent, reminderMinutes)
        dao.upsertEvent(syncedNewEvent)
        writeGhostFlag(syncedNewEvent.id, data.isGhost)
        replaceEventFileAttachments(syncedNewEvent.id, data.fileAttachments, previousEventId = data.eventId)
        reminderMinutes.forEach { minutes ->
            val reminder = EventReminder(
                eventId = syncedNewEvent.id,
                minutesBefore = minutes,
                triggerAtMs = start - minutes * 60_000L,
                alarmRequestCode = providerReminderAlarmRequestCode(syncedNewEvent.id, minutes),
            )
            dao.insertReminders(listOf(reminder))
            reminderScheduler.scheduleReminder(reminder, syncedNewEvent)
        }
        updateWidgets()
        return syncedNewEvent
    }

    private suspend fun truncateSeriesBeforeOccurrence(event: CalendarEvent): CalendarEvent? {
        val occurrenceStartMs = event.occurrenceStartMs() ?: return null
        val masterId = event.baseEventId()
        val master = dao.getEvent(masterId) ?: return null
        val splitDate = Instant.ofEpochMilli(occurrenceStartMs).atZone(safeZoneId(master.timeZone)).toLocalDate()
        if (master.rrule.isNullOrBlank() || splitDate <= master.startDate()) {
            deleteLocalEvent(master, RecurringEditScope.WholeSeries)
            return null
        }
        val reminders = dao.getRemindersForEvent(master.id)
        val updatedMaster = master.copy(
            rrule = truncateRRuleBefore(master.rrule, splitDate),
            exceptionDates = master.exceptionStartTimes()
                .filter { it < occurrenceStartMs }
                .joinToString(prefix = "[", postfix = "]"),
            updatedAtMs = System.currentTimeMillis(),
        )
        val syncedMaster = syncProviderBackedEvent(master, updatedMaster, reminders.map { it.minutesBefore })
        dao.upsertEvent(syncedMaster)
        updateWidgets()
        return syncedMaster
    }

    private fun truncateRRuleBefore(rrule: String?, splitDate: LocalDate): String? {
        val rule = RecurrenceRule.parse(rrule) ?: return rrule
        return rule.copy(count = null, until = splitDate.minusDays(1)).toRRule()
    }

    private fun updateWidgets() {
        WidgetUpdateWorker.enqueue(context)
    }

    private suspend fun replaceEventFileAttachments(
        eventId: String,
        attachments: List<EventFileAttachment>,
        previousEventId: String? = null,
    ) {
        val materialized = materializeEventFileAttachments(eventId, attachments)
        val previous = readEventFileAttachments(eventId) +
            previousEventId?.takeIf { it != eventId }?.let { readEventFileAttachments(it) }.orEmpty()
        val keptPaths = materialized.map { it.localPath }.toSet()
        previous.filterNot { it.localPath in keptPaths }.forEach { discardEventFileAttachment(it) }
        if (materialized.isEmpty()) {
            sideStore.remove(EVENT_FILE_ATTACHMENTS_NAMESPACE, eventId)
        } else {
            sideStore.write(EVENT_FILE_ATTACHMENTS_NAMESPACE, eventId, materialized.encodeEventFileAttachments())
        }
        previousEventId?.takeIf { it != eventId }?.let {
            sideStore.remove(EVENT_FILE_ATTACHMENTS_NAMESPACE, it)
        }
    }

    private fun materializeEventFileAttachments(
        eventId: String,
        attachments: List<EventFileAttachment>,
    ): List<EventFileAttachment> {
        return attachments.take(MAX_EVENT_FILE_ATTACHMENTS).mapNotNull { attachment ->
            runCatching {
                val source = File(attachment.localPath)
                if (!source.exists()) return@runCatching null
                val target = eventAttachmentFile(context.filesDir, eventId, attachment.id)
                if (source.canonicalPath != target.canonicalPath) {
                    target.parentFile?.mkdirs()
                    source.copyTo(target, overwrite = true)
                }
                attachment.copy(
                    mimeType = attachment.mimeType.ifBlank { EVENT_FILE_PDF_MIME },
                    sizeBytes = target.length(),
                    localPath = target.absolutePath,
                )
            }.getOrNull()
        }
    }

    private suspend fun deleteEventFileAttachments(eventId: String) {
        readEventFileAttachments(eventId).forEach { discardEventFileAttachment(it) }
        sideStore.remove(EVENT_FILE_ATTACHMENTS_NAMESPACE, eventId)
        runCatching { eventAttachmentDirectory(context.filesDir, eventId).deleteRecursively() }
    }

    private fun android.content.ContentResolver.queryDisplayName(uri: Uri): String {
        return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }?.takeIf { it.isNotBlank() } ?: "Attachment.pdf"
    }

    private fun List<CalendarEvent>.filterOutPrivate(privateIds: Set<String>): List<CalendarEvent> {
        if (privateIds.isEmpty()) return this
        return filterNot { it.baseEventId() in privateIds }
    }

    private suspend fun CalendarEvent.withGhostFlag(): CalendarEvent {
        return also {
            val baseId = baseEventId()
            it.isGhost = sideStore.read(GHOST_FLAGS_NAMESPACE, baseId) == "1"
            it.providerRdate = sideStore.read(EventSideStoreNamespaces.ProviderRdates, baseId)
        }
    }

    private suspend fun List<CalendarEvent>.withGhostFlags(): List<CalendarEvent> {
        val ghostIds = sideStore.readNamespace(GHOST_FLAGS_NAMESPACE)
            .filterValues { it == "1" }
            .keys
        return onEach { event -> event.isGhost = event.baseEventId() in ghostIds }
    }

    private suspend fun writeGhostFlag(eventId: String, isGhost: Boolean) {
        if (isGhost) {
            sideStore.write(GHOST_FLAGS_NAMESPACE, eventId, "1")
        } else {
            sideStore.remove(GHOST_FLAGS_NAMESPACE, eventId)
        }
    }

    private fun expandRecurringEvents(
        events: List<CalendarEvent>,
        rangeStart: LocalDate,
        rangeEndExclusive: LocalDate,
        providerRdates: Map<String, String> = emptyMap(),
    ): List<CalendarEvent> {
        val rangeStartMs = rangeStart.atStartMs()
        val rangeEndMs = rangeEndExclusive.atStartMs()
        val expanded = events.flatMap { event ->
            val rule = RecurrenceRule.parse(event.rrule)
            val ruleEvents = if (rule == null) listOf(event) else event.expandRule(rule, rangeStart, rangeEndExclusive)
            val rdateEvents = event.expandProviderRdates(providerRdates[event.baseEventId()], rangeStartMs, rangeEndMs)
            (ruleEvents + rdateEvents).distinctBy { it.id }
        }
        return expanded.sortedWith(compareBy<CalendarEvent> { it.startTimeMs }.thenBy { it.title })
    }

    private fun expandRecurringTasks(tasks: List<CalendarEvent>): List<CalendarEvent> {
        val today = LocalDate.now()
        val rangeEnd = today.plusYears(1)
        val expanded = tasks.flatMap { task ->
            val rule = RecurrenceRule.parse(task.rrule)
            if (rule == null) listOf(task) else task.expandRule(rule, today, rangeEnd)
        }
        return expanded.sortedWith(compareBy<CalendarEvent> { it.isCompleted }.thenBy { if (it.startTimeMs > 0L) it.startTimeMs else Long.MAX_VALUE }.thenBy { it.title })
    }

    /**
     * Expand a structured [RecurrenceRule] into visible occurrences within [rangeStart, rangeEndExclusive).
     * Honors INTERVAL, weekly/monthly BYDAY, COUNT and UNTIL. COUNT is evaluated from the series anchor
     * (so counts stay stable across scroll/views); the unbounded/UNTIL-only case fast-forwards to the range
     * for performance. EXDATE handling and per-occurrence timing are delegated to [occurrenceOn].
     */
    private fun CalendarEvent.expandRule(
        rule: RecurrenceRule,
        rangeStart: LocalDate,
        rangeEndExclusive: LocalDate,
    ): List<CalendarEvent> {
        val firstDate = startDate()
        val anchorCount = rule.count != null
        var block = if (anchorCount) 0 else rule.fastForwardBlock(firstDate, rangeStart)
        val out = mutableListOf<CalendarEvent>()
        var emittedCount = 0
        var iterations = 0
        while (iterations < MAX_RECURRENCE_BLOCKS) {
            iterations++
            for (date in rule.datesForBlock(firstDate, block)) {
                if (rule.until != null && date > rule.until) return out
                if (anchorCount && emittedCount >= rule.count!!) return out
                emittedCount++
                if (date >= rangeStart && date < rangeEndExclusive) {
                    occurrenceOn(date)?.let { out += it }
                }
            }
            if (anchorCount && emittedCount >= rule.count!!) break
            val anchor = rule.blockAnchorDate(firstDate, block)
            if (rule.until != null && anchor > rule.until) break
            if (!anchorCount && anchor >= rangeEndExclusive) break
            block++
        }
        return out
    }

    private fun CalendarEvent.occurrenceOn(date: LocalDate): CalendarEvent? {
        val zoneId = safeZoneId(timeZone)
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

    private fun CalendarEvent.expandProviderRdates(rdate: String?, rangeStartMs: Long, rangeEndMs: Long): List<CalendarEvent> {
        if (rdate.isNullOrBlank()) return emptyList()
        val durationMs = endTimeMs - startTimeMs
        val exceptions = exceptionStartTimes()
        return providerRdateStartTimes(rdate, isAllDay, timeZone)
            .filterNot { it in exceptions }
            .map { occurrenceStart ->
                copy(
                    id = recurrenceOccurrenceId(id, occurrenceStart),
                    startTimeMs = occurrenceStart,
                    endTimeMs = occurrenceStart + durationMs,
                )
            }
            .filter { occurrence -> occurrence.startTimeMs < rangeEndMs && occurrence.endTimeMs >= rangeStartMs }
    }

    private fun CalendarEvent.startDate(): LocalDate {
        return java.time.Instant.ofEpochMilli(startTimeMs).atZone(safeZoneId(timeZone)).toLocalDate()
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
        val zoneId = safeZoneId(event.timeZone)
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

    private fun safeZoneId(id: String): ZoneId = runCatching { ZoneId.of(id) }.getOrDefault(ZoneId.systemDefault())

    companion object {
        const val LOCAL_ACCOUNT_ID = "local-primary"
        private const val HOLIDAY_ACCOUNT_PREFIX = "holiday-"
        private const val DEFAULT_EVENT_COLOR = "#FF0000"
        private const val GHOST_FLAGS_NAMESPACE = EventSideStoreNamespaces.GhostFlags
        private const val SHIFT_EVENT_METADATA_NAMESPACE = "shift_event_metadata"
        private const val EVENT_FILE_ATTACHMENTS_NAMESPACE = "event_file_attachments"
        private const val EVENT_FILE_PDF_MIME = "application/pdf"
        private const val MAX_EVENT_FILE_ATTACHMENTS = 5
        private const val MAX_EVENT_FILE_ATTACHMENT_BYTES = 20L * 1024L * 1024L
        private const val BIRTHDAY_ACCOUNT_ID = ContactsProviderDataSource.BIRTHDAY_ACCOUNT_ID
        private const val BIRTHDAY_COLOR = ContactsProviderDataSource.BIRTHDAY_COLOR
        private const val BIRTHDAY_REMINDER_MINUTES = 1440
        private const val BIRTHDAY_SORT_ORDER = 10

        /** Safety cap on recurrence-block iteration; ~27 years of a daily rule. Guards pathological rules. */
        private const val MAX_RECURRENCE_BLOCKS = 10_000

        fun holidayAccountId(countryCode: String): String = "$HOLIDAY_ACCOUNT_PREFIX$countryCode"
    }
}

private fun CalendarAccount.isReadOnlyGeneratedAccount(): Boolean {
    val raw = listOf(id, accountName, displayName, accountType).joinToString(" ").lowercase(Locale.US)
    return id == ContactsProviderDataSource.BIRTHDAY_ACCOUNT_ID ||
        id.startsWith("holiday-") ||
        raw.contains("birthday") ||
        raw.contains("#holiday@") ||
        raw.contains("holiday@group.v.calendar.google.com") ||
        raw.contains("#contacts@") ||
        raw.contains("contacts@group.v.calendar.google.com") ||
        raw.contains("addressbook#contacts") ||
        accountName.equals("BIRTHDAY", ignoreCase = true) ||
        displayName.startsWith("Holidays -", ignoreCase = true)
}

private const val RECURRENCE_OCCURRENCE_SEPARATOR = "::occurrence::"
private const val DATABASE_LOCK_RETRY_LIMIT = 4L
private const val DATABASE_LOCK_RETRY_BASE_DELAY_MS = 120L

private fun Flow<List<CalendarEvent>>.retryOnDatabaseLocked(): Flow<List<CalendarEvent>> =
    retryWhen { cause, attempt ->
        if (attempt >= DATABASE_LOCK_RETRY_LIMIT || !cause.hasDatabaseLockedCause()) {
            false
        } else {
            delay(DATABASE_LOCK_RETRY_BASE_DELAY_MS * (attempt + 1))
            true
        }
    }

private fun Throwable.hasDatabaseLockedCause(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is SQLiteDatabaseLockedException) return true
        if (current is SQLiteException && current.message?.contains("database is locked", ignoreCase = true) == true) return true
        current = current.cause
    }
    return false
}

internal fun CalendarEvent.withProviderSyncResult(providerEvent: CalendarEvent): CalendarEvent {
    return copy(
        id = providerEvent.id,
        accountId = providerEvent.accountId,
        source = providerEvent.source,
        googleEventId = providerEvent.googleEventId,
        googleCalendarId = providerEvent.googleCalendarId,
        syncVersion = providerEvent.syncVersion,
        colorHex = providerEvent.colorHex,
    )
}

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
