package com.dotfield.dotcal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.dotfield.dotcal.data.BirthdayImportResult
import com.dotfield.dotcal.data.BulkEditResult
import com.dotfield.dotcal.data.BulkEditUndoToken
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.RecurringEditScope
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.TaskEditorData
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.attachments.EventFileAttachment
import com.dotfield.dotcal.data.billing.ProManager
import com.dotfield.dotcal.data.countdown.CountdownPinResult
import com.dotfield.dotcal.data.provider.ProviderMeetingMetadata
import com.dotfield.dotcal.data.privacy.AppLockState
import com.dotfield.dotcal.data.profiles.FocusProfile
import com.dotfield.dotcal.data.scheduling.AvailabilityTextFormatter
import com.dotfield.dotcal.data.scheduling.DayAvailability
import com.dotfield.dotcal.data.scheduling.FreeSlot
import com.dotfield.dotcal.data.scheduling.FreeSlotRequest
import com.dotfield.dotcal.data.shifts.ShiftApplyResult
import com.dotfield.dotcal.data.shifts.ShiftEventMetadata
import com.dotfield.dotcal.data.shifts.ShiftPattern
import com.dotfield.dotcal.data.shifts.ShiftType
import com.dotfield.dotcal.data.templates.EventTemplate
import com.dotfield.dotcal.data.trash.DeletedSnapshot
import com.dotfield.dotcal.data.holiday.HolidayCountry
import com.dotfield.dotcal.data.holiday.HolidayDataSource
import com.dotfield.dotcal.data.insights.OnThisDayMemory
import com.dotfield.dotcal.data.nlp.SmartQuickAddCommand
import com.dotfield.dotcal.data.nlp.SmartQuickAddMatcher
import com.dotfield.dotcal.data.punchcard.PunchCardStreak
import com.dotfield.dotcal.sync.CalendarSyncResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.Instant

data class HolidayCountryUiItem(
    val code: String,
    val name: String,
    val isSelected: Boolean,
)

data class DayDensityForecastItem(
    val date: LocalDate,
    val scheduledMinutes: Int,
    val intensity: Int,
)

data class PunchCardUiState(
    val punchedDays: Set<LocalDate> = emptySet(),
) {
    fun isPunched(date: LocalDate): Boolean = date in punchedDays
    fun streakEndingAt(date: LocalDate): Int = PunchCardStreak.compute(punchedDays, date)
}

data class AvailabilityUiState(
    val isLoading: Boolean = false,
    val text: String = "",
    val days: List<DayAvailability> = emptyList(),
    val error: String? = null,
)

data class DeadTimeUiState(
    val isLoading: Boolean = false,
    val slots: List<FreeSlot> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DotCalViewModel(
    private val repository: DotCalRepository,
    val proManager: ProManager,
) : ViewModel() {
    private val currentMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val selectedDate = MutableStateFlow(LocalDate.now())

    val isPro: StateFlow<Boolean> = proManager.isPro
    val billingState: StateFlow<ProManager.BillingConnectionState> = proManager.billingState
    val appLockState: StateFlow<AppLockState> = repository.observeAppLockState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLockState(enabled = false, hasPin = false))
    val privateVaultIds: StateFlow<Set<String>> = repository.observePrivateVaultIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val month: StateFlow<LocalDate> = currentMonth
    val events: StateFlow<List<CalendarEvent>> = currentMonth
        .flatMapLatest(repository::observeEventsForMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val yearEvents: StateFlow<List<CalendarEvent>> = selectedDate
        .map { it.year }
        .distinctUntilChanged()
        .flatMapLatest(repository::observeEventsForYear)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val agendaEvents: StateFlow<List<CalendarEvent>> = repository.observeUpcomingAgendaEvents(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val onThisDayMemories: StateFlow<List<OnThisDayMemory>> = selectedDate
        .flatMapLatest(repository::observeOnThisDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dayDensityForecast: StateFlow<List<DayDensityForecastItem>> = agendaEvents
        .map(::buildDayDensityForecast)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildDayDensityForecast(emptyList()))

    private val _punchCardState = MutableStateFlow(PunchCardUiState())
    val punchCardState: StateFlow<PunchCardUiState> = _punchCardState
    private val _countdownPins = MutableStateFlow<Set<String>>(emptySet())
    val countdownPins: StateFlow<Set<String>> = _countdownPins
    private val _availabilityState = MutableStateFlow(AvailabilityUiState())
    val availabilityState: StateFlow<AvailabilityUiState> = _availabilityState
    private var availabilityJob: Job? = null
    private val _deadTimeState = MutableStateFlow(DeadTimeUiState())
    val deadTimeState: StateFlow<DeadTimeUiState> = _deadTimeState
    private var deadTimeJob: Job? = null

    val tasks: StateFlow<List<CalendarEvent>> = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accounts: StateFlow<List<CalendarAccount>> = repository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val assignableAccounts: StateFlow<List<CalendarAccount>> = repository.observeAssignableAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _lastSelectedEventAccountId = MutableStateFlow<String?>(null)
    val lastSelectedEventAccountId: StateFlow<String?> = _lastSelectedEventAccountId

    private var conflictWarningJob: Job? = null
    private var conflictWarningSessionKey: String? = null
    private val _conflictWarnings = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val conflictWarnings: StateFlow<List<CalendarEvent>> = _conflictWarnings

    val syncMetadata: StateFlow<List<SyncMetadata>> = repository.observeSyncMetadata()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val holidayCountries: StateFlow<List<HolidayCountryUiItem>> = repository.observeSelectedHolidayCountries()
        .map { selectedCodes ->
            val selected = selectedCodes.toSet()
            HolidayDataSource.Countries.map { country ->
                HolidayCountryUiItem(
                    code = country.code,
                    name = country.name,
                    isSelected = country.code in selected,
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HolidayDataSource.Countries.map { country ->
                HolidayCountryUiItem(country.code, country.name, isSelected = false)
            },
        )

    val reminders: StateFlow<List<EventReminder>> = repository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _detailEvent = MutableStateFlow<CalendarEvent?>(null)
    val detailEvent: StateFlow<CalendarEvent?> = _detailEvent
    val shiftEventMetadata: StateFlow<Map<String, ShiftEventMetadata>> = combine(
        events,
        agendaEvents,
        detailEvent,
    ) { monthEvents, agendaItems, detail ->
        val ids = (monthEvents + agendaItems + listOfNotNull(detail)).map { it.baseEventId() }.toSet()
        repository.listShiftEventMetadata(ids)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
    private val _eventFileAttachments = MutableStateFlow<Map<String, List<EventFileAttachment>>>(emptyMap())
    val eventFileAttachments: StateFlow<Map<String, List<EventFileAttachment>>> = _eventFileAttachments
    private val _providerMeetingMetadata = MutableStateFlow<Map<String, ProviderMeetingMetadata>>(emptyMap())
    val providerMeetingMetadata: StateFlow<Map<String, ProviderMeetingMetadata>> = _providerMeetingMetadata

    // ----- Pro / Billing -----
    val productDetails = proManager.productDetails
    val purchaseOffers = proManager.purchaseOffers
    val hasActiveSubscription = proManager.hasActiveSubscription
    val purchaseResult = proManager.purchaseResultFlow

    fun purchasePro(activity: android.app.Activity, selectedOfferKey: String? = null) {
        viewModelScope.launch {
            val result = proManager.launchPurchaseFlow(activity, selectedOfferKey)
            // Pre-flight failures surface immediately; the real purchase outcome
            // (Success/Cancelled) arrives later through purchaseResult.
            if (result is ProManager.PurchaseResult.Error) {
                proManager.pushPurchaseResult(result)
            }
        }
    }

    fun restorePro(onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(proManager.restorePurchases()) }
    }

    fun clearPurchaseResult() = proManager.clearPurchaseResult()

    init {
        viewModelScope.launch { repository.ensureLocalAccount() }
        refreshPunchCard()
        refreshCountdownPins()
    }

    fun previousMonth() {
        currentMonth.value = currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        currentMonth.value = currentMonth.value.plusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        currentMonth.value = date.withDayOfMonth(1)
    }

    fun addQuickEvent(title: String, date: LocalDate, startTime: LocalTime = LocalTime.of(9, 0)) {
        viewModelScope.launch { repository.addLocalEvent(title = title, date = date, startTime = startTime) }
    }

    fun openEventDetail(event: CalendarEvent) {
        _detailEvent.value = event
        refreshProviderMeetingMetadata(event.baseEventId())
    }

    fun dismissOnThisDay(date: LocalDate) {
        viewModelScope.launch { repository.dismissOnThisDay(date) }
    }

    fun openMemoryById(eventId: String) {
        openEventDetailById(eventId)
    }

    fun openEventDetailById(eventId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (eventId.substringBefore("::occurrence::") in repository.observePrivateVaultIds().first()) {
                onComplete()
                return@launch
            }
            val event = repository.getEvent(eventId)
            if (event == null) {
                onComplete()
                return@launch
            }
            event.let {
                selectDate(event.startDate())
                _detailEvent.value = event
                refreshProviderMeetingMetadata(event.baseEventId())
            }
            onComplete()
        }
    }

    fun closeEventDetail() {
        _detailEvent.value = null
    }

    fun refreshEventFileAttachments(eventId: String) {
        viewModelScope.launch {
            _eventFileAttachments.value = _eventFileAttachments.value + (
                eventId to repository.readEventFileAttachments(eventId)
            )
        }
    }

    private fun refreshProviderMeetingMetadata(eventId: String) {
        viewModelScope.launch {
            val metadata = repository.readProviderMeetingMetadata(eventId)
            _providerMeetingMetadata.value = if (metadata == null) {
                _providerMeetingMetadata.value - eventId
            } else {
                _providerMeetingMetadata.value + (eventId to metadata)
            }
        }
    }

    fun importEventFileAttachment(
        eventId: String,
        uri: Uri,
        currentAttachments: List<EventFileAttachment>,
        onDone: (Result<EventFileAttachment>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = runCatching { repository.importEventFileAttachment(eventId, uri, currentAttachments) }
            result.getOrNull()?.let { attachment ->
                _eventFileAttachments.value = _eventFileAttachments.value + (
                    eventId to (currentAttachments + attachment).take(5)
                )
            }
            onDone(result)
        }
    }

    fun discardEventFileAttachment(attachment: EventFileAttachment) {
        viewModelScope.launch { repository.discardEventFileAttachment(attachment) }
    }

    fun refreshConflictWarnings(
        sessionKey: String,
        existing: CalendarEvent?,
        startDate: LocalDate,
        endDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        isAllDay: Boolean,
    ) {
        conflictWarningJob?.cancel()
        // `conflictWarnings` is shared ViewModel state, so an opening editor would otherwise render
        // the *previous* session's hits, hold them through the debounce below, then swap them out —
        // which read as a half-drawn warning flashing at the bottom of the screen and vanishing.
        // Only a new editor session clears: within one session the last result is held across the
        // debounce window, so nudging the start/end time of an event that really does overlap never
        // blinks its warning out and back.
        val isNewSession = sessionKey != conflictWarningSessionKey
        conflictWarningSessionKey = sessionKey
        if (isAllDay || !endDate.atTime(endTime).isAfter(startDate.atTime(startTime))) {
            // No lookup follows this branch, so nothing downstream would replace a stale list.
            _conflictWarnings.value = emptyList()
            return
        }
        if (isNewSession) {
            _conflictWarnings.value = emptyList()
        }
        conflictWarningJob = viewModelScope.launch {
            delay(300)
            _conflictWarnings.value = repository.findConflictWarnings(
                startDate = startDate,
                endDate = endDate,
                startTime = startTime,
                endTime = endTime,
                excludedEventId = existing?.baseEventId(),
            )
        }
    }

    fun clearConflictWarnings() {
        conflictWarningJob?.cancel()
        conflictWarningSessionKey = null
        _conflictWarnings.value = emptyList()
    }

    fun refreshAvailability(request: FreeSlotRequest, use24HourFormat: Boolean) {
        availabilityJob?.cancel()
        _availabilityState.value = _availabilityState.value.copy(isLoading = true, error = null)
        availabilityJob = viewModelScope.launch {
            runCatching {
                val days = repository.computeAvailability(request)
                days to AvailabilityTextFormatter.format(days, use24HourFormat)
            }.onSuccess { (days, text) ->
                _availabilityState.value = AvailabilityUiState(days = days, text = text)
            }.onFailure {
                _availabilityState.value = AvailabilityUiState(error = "Couldn't calculate availability")
            }
        }
    }

    fun clearAvailability() {
        availabilityJob?.cancel()
        _availabilityState.value = AvailabilityUiState()
    }

    fun refreshDeadTime(
        startDate: LocalDate,
        startHour: Int,
        endHour: Int,
    ) {
        deadTimeJob?.cancel()
        _deadTimeState.value = _deadTimeState.value.copy(isLoading = true, error = null)
        deadTimeJob = viewModelScope.launch {
            runCatching {
                repository.computeDeadTime(startDate, startHour, endHour).slots
            }.onSuccess { slots ->
                _deadTimeState.value = DeadTimeUiState(slots = slots)
            }.onFailure {
                _deadTimeState.value = DeadTimeUiState(error = "Couldn't find free time")
            }
        }
    }

    fun saveEvent(
        existing: CalendarEvent?,
        data: EventEditorData,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
        onSaved: (String?) -> Unit = {},
    ) {
        viewModelScope.launch {
            val savedEvent = repository.saveLocalEvent(
                existing = existing,
                data = data,
                recurringEditScope = recurringEditScope,
            )
            if (existing == null) {
                _lastSelectedEventAccountId.value = data.accountId
            }
            (savedEvent?.baseEventId() ?: existing?.baseEventId() ?: data.eventId)?.let { refreshEventFileAttachments(it) }
            onSaved(savedEvent?.id ?: savedEvent?.baseEventId())
        }
    }

    fun deleteEvent(
        event: CalendarEvent,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
    ) {
        viewModelScope.launch {
            repository.deleteLocalEvent(
                event = event,
                recurringEditScope = recurringEditScope,
            )
        }
    }

    fun bulkShiftEvents(eventIds: Set<String>, days: Long, hours: Long, onDone: (Result<BulkEditResult>) -> Unit = {}) {
        viewModelScope.launch { onDone(runCatching { repository.bulkShiftEvents(eventIds, days, hours) }) }
    }

    fun checkDragConflicts(
        event: CalendarEvent,
        targetStart: LocalDateTime,
        targetEnd: LocalDateTime,
        onDone: (List<CalendarEvent>) -> Unit,
    ) {
        viewModelScope.launch {
            onDone(
                repository.findConflictWarnings(
                    startDate = targetStart.toLocalDate(),
                    endDate = targetEnd.toLocalDate(),
                    startTime = targetStart.toLocalTime(),
                    endTime = targetEnd.toLocalTime(),
                    excludedEventId = event.baseEventId(),
                ),
            )
        }
    }

    fun rescheduleEvent(
        event: CalendarEvent,
        targetStart: LocalDateTime,
        targetEnd: LocalDateTime,
        recurringEditScope: RecurringEditScope,
        onDone: (Result<BulkEditResult>) -> Unit,
    ) {
        viewModelScope.launch {
            onDone(
                runCatching {
                    repository.rescheduleEvent(event, targetStart, targetEnd, recurringEditScope)
                },
            )
        }
    }

    fun bulkMoveToDate(eventIds: Set<String>, targetDate: LocalDate, onDone: (Result<BulkEditResult>) -> Unit = {}) {
        viewModelScope.launch { onDone(runCatching { repository.bulkMoveToDate(eventIds, targetDate) }) }
    }

    fun bulkCopyToDate(eventIds: Set<String>, targetDate: LocalDate, onDone: (Result<BulkEditResult>) -> Unit = {}) {
        viewModelScope.launch { onDone(runCatching { repository.bulkCopyToDate(eventIds, targetDate) }) }
    }

    fun bulkChangeCalendar(eventIds: Set<String>, accountId: String, onDone: (Result<BulkEditResult>) -> Unit = {}) {
        viewModelScope.launch { onDone(runCatching { repository.bulkChangeCalendar(eventIds, accountId) }) }
    }

    fun bulkChangeColor(eventIds: Set<String>, colorHex: String?, onDone: (Result<BulkEditResult>) -> Unit = {}) {
        viewModelScope.launch { onDone(runCatching { repository.bulkChangeColor(eventIds, colorHex) }) }
    }

    fun bulkDeleteEvents(eventIds: Set<String>, onDone: (Result<BulkEditResult>) -> Unit = {}) {
        viewModelScope.launch { onDone(runCatching { repository.bulkDeleteEvents(eventIds) }) }
    }

    fun bulkToggleGhost(eventIds: Set<String>, onDone: (Result<BulkEditResult>) -> Unit = {}) {
        viewModelScope.launch { onDone(runCatching { repository.bulkToggleGhost(eventIds) }) }
    }

    fun undoBulkEdit(token: BulkEditUndoToken, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.undoBulkEdit(token)
            onDone()
        }
    }

    fun saveTask(existing: CalendarEvent?, data: TaskEditorData, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveLocalTask(existing, data)
            onSaved()
        }
    }

    fun completeTask(task: CalendarEvent) {
        viewModelScope.launch {
            repository.setTaskCompleted(task, completed = true)
        }
    }

    fun reopenTask(task: CalendarEvent) {
        viewModelScope.launch {
            repository.setTaskCompleted(task, completed = false)
        }
    }

    fun deleteTask(task: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun punchDay(date: LocalDate) {
        viewModelScope.launch {
            repository.setDayPunched(date, punched = true)
            _punchCardState.value = PunchCardUiState(repository.readPunchedDays())
        }
    }

    fun clearDayPunch(date: LocalDate) {
        viewModelScope.launch {
            repository.setDayPunched(date, punched = false)
            _punchCardState.value = PunchCardUiState(repository.readPunchedDays())
        }
    }

    private fun refreshPunchCard() {
        viewModelScope.launch {
            _punchCardState.value = PunchCardUiState(repository.readPunchedDays())
        }
    }

    fun pinCountdown(event: CalendarEvent, isPro: Boolean, onResult: (CountdownPinResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.pinCountdown(event.baseEventId(), isPro)
            _countdownPins.value = repository.readCountdownPins()
            onResult(result)
        }
    }

    fun swapCountdownPin(activeEventId: String, newEvent: CalendarEvent, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.swapCountdownPin(activeEventId, newEvent.baseEventId())
            _countdownPins.value = repository.readCountdownPins()
            onDone()
        }
    }

    fun unpinCountdown(event: CalendarEvent, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.unpinCountdown(event.baseEventId())
            _countdownPins.value = repository.readCountdownPins()
            onDone()
        }
    }

    private fun refreshCountdownPins() {
        viewModelScope.launch {
            _countdownPins.value = repository.readCountdownPins()
        }
    }

    // ----- Recently Deleted (file-based trash) -----
    private val _recentlyDeleted = MutableStateFlow<List<DeletedSnapshot>>(emptyList())
    val recentlyDeleted: StateFlow<List<DeletedSnapshot>> = _recentlyDeleted

    private val _privateVaultEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val privateVaultEvents: StateFlow<List<CalendarEvent>> = _privateVaultEvents

    // ----- Global Search (FREE) -----
    private val _searchResults = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val searchResults: StateFlow<List<CalendarEvent>> = _searchResults
    private var smartQuickAddResolveJob: Job? = null

    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchItems(query)
        }
    }

    fun resolveSmartQuickAddCandidates(
        command: SmartQuickAddCommand?,
        contextEventId: String?,
        onResolved: (List<CalendarEvent>) -> Unit,
    ) {
        smartQuickAddResolveJob?.cancel()
        if (command == null) {
            onResolved(emptyList())
            return
        }
        smartQuickAddResolveJob = viewModelScope.launch {
            val now = LocalDateTime.now()
            val date = (command as? SmartQuickAddCommand.Query)?.date
            val rangeStart = date ?: now.toLocalDate()
            val rangeEnd = date ?: rangeStart.plusMonths(6)
            val query = when (command) {
                is SmartQuickAddCommand.Move -> command.eventQuery
                is SmartQuickAddCommand.Delete -> command.eventQuery
                is SmartQuickAddCommand.AddPrep -> command.eventQuery
                is SmartQuickAddCommand.Rename -> command.eventQuery
                is SmartQuickAddCommand.SetDuration -> command.eventQuery
                is SmartQuickAddCommand.SetLocation -> command.eventQuery
                is SmartQuickAddCommand.SetReminder -> command.eventQuery
                is SmartQuickAddCommand.Query -> ""
            }
            val context = contextEventId?.let { repository.getEvent(it) }
            val source = repository.findSmartQuickAddCandidates(query, rangeStart, rangeEnd) + listOfNotNull(context)
            onResolved(SmartQuickAddMatcher.findCandidates(command, source, now, context))
        }
    }

    fun applySmartQuickAddEdit(
        event: CalendarEvent,
        command: SmartQuickAddCommand,
        onDone: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            runCatching {
                val current = repository.getEvent(event.baseEventId()) ?: event
                require(current.isTask == 0 && current.source != "BIRTHDAY") { "EVENT CANNOT BE EDITED" }
                require(
                    command is SmartQuickAddCommand.Rename ||
                        command is SmartQuickAddCommand.SetDuration ||
                        command is SmartQuickAddCommand.SetLocation ||
                        command is SmartQuickAddCommand.SetReminder,
                ) { "UNSUPPORTED QUICK EDIT" }
                val zone = runCatching { ZoneId.of(current.timeZone) }.getOrDefault(ZoneId.systemDefault())
                val start = Instant.ofEpochMilli(current.startTimeMs).atZone(zone).toLocalDateTime()
                val currentEnd = Instant.ofEpochMilli(current.endTimeMs).atZone(zone).toLocalDateTime()
                val reminders = repository.getRemindersForEvent(current.baseEventId()).map { it.minutesBefore }
                val edit = when (command) {
                    is SmartQuickAddCommand.Rename -> current.title to command.newTitle.trim()
                    is SmartQuickAddCommand.SetLocation -> current.location to command.location.trim()
                    else -> current.title to current.title
                }
                val targetEnd = when (command) {
                    is SmartQuickAddCommand.SetDuration -> start.plusMinutes(command.minutes.also {
                        require(current.isAllDay == 0) { "ALL-DAY EVENT HAS NO DURATION" }
                        require(it in 1..(7L * 24L * 60L)) { "DURATION OUT OF RANGE" }
                    })
                    else -> currentEnd
                }
                val targetReminders = when (command) {
                    is SmartQuickAddCommand.SetReminder -> command.minutesBefore?.let(::listOf).orEmpty()
                    else -> reminders
                }.distinct().sorted()
                repository.saveLocalEvent(
                    existing = current,
                    data = EventEditorData(
                        eventId = current.baseEventId(),
                        accountId = current.accountId,
                        title = edit.second.ifBlank { current.title },
                        description = current.description,
                        location = if (command is SmartQuickAddCommand.SetLocation) edit.second else current.location,
                        date = start.toLocalDate(),
                        endDate = if (current.isAllDay == 1) {
                            currentEnd.toLocalDate().minusDays(1)
                        } else {
                            targetEnd.toLocalDate()
                        },
                        startTime = start.toLocalTime(),
                        endTime = targetEnd.toLocalTime(),
                        isAllDay = current.isAllDay == 1,
                        reminderMinutes = targetReminders.firstOrNull(),
                        reminderMinutesList = targetReminders.takeIf { it.size > 1 },
                        rrule = current.rrule,
                        imageUris = current.imageUris,
                        voiceNotePath = current.voiceNotePath,
                        colorHex = current.colorHex,
                        isGhost = current.isGhost,
                    ),
                )
            }.onSuccess { onDone(true) }.onFailure { onDone(false) }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun refreshRecentlyDeleted() {
        viewModelScope.launch {
            _recentlyDeleted.value = repository.listRecentlyDeleted()
        }
    }

    fun restoreDeleted(eventId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.restoreDeleted(eventId)
            _recentlyDeleted.value = repository.listRecentlyDeleted()
            onDone()
        }
    }

    fun purgeDeleted(eventId: String) {
        viewModelScope.launch {
            repository.purgeDeleted(eventId)
            _recentlyDeleted.value = repository.listRecentlyDeleted()
        }
    }

    fun emptyRecentlyDeleted() {
        viewModelScope.launch {
            repository.emptyRecentlyDeleted()
            _recentlyDeleted.value = emptyList()
        }
    }

    // ----- Event/Task Templates (file-based, Pro) -----
    private val _templates = MutableStateFlow<List<EventTemplate>>(emptyList())
    val templates: StateFlow<List<EventTemplate>> = _templates

    fun refreshTemplates() {
        viewModelScope.launch {
            _templates.value = repository.listTemplates()
        }
    }

    fun saveTemplate(template: EventTemplate, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveTemplate(template)
            _templates.value = repository.listTemplates()
            onDone()
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
            _templates.value = repository.listTemplates()
        }
    }

    fun applyTemplateToDates(
        templateId: String,
        dates: List<LocalDate>,
        accountId: String?,
        onDone: (Int) -> Unit = {},
    ) {
        viewModelScope.launch {
            onDone(repository.applyTemplateToDates(templateId, dates, accountId))
        }
    }

    // ----- Calendar Sets / Focus Profiles (file-based, Pro) -----
    private val _focusProfiles = MutableStateFlow<List<FocusProfile>>(emptyList())
    val focusProfiles: StateFlow<List<FocusProfile>> = _focusProfiles

    fun refreshFocusProfiles() {
        viewModelScope.launch {
            _focusProfiles.value = repository.listFocusProfiles()
        }
    }

    fun saveFocusProfile(profile: FocusProfile, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveFocusProfile(profile)
            _focusProfiles.value = repository.listFocusProfiles()
            onDone()
        }
    }

    fun deleteFocusProfile(id: String) {
        viewModelScope.launch {
            repository.deleteFocusProfile(id)
            _focusProfiles.value = repository.listFocusProfiles()
        }
    }

    fun applyFocusProfile(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.applyFocusProfile(id)
            onDone()
        }
    }

    // ----- Shift Patterns (file-based, Pro) -----
    private val _shiftTypes = MutableStateFlow<List<ShiftType>>(emptyList())
    val shiftTypes: StateFlow<List<ShiftType>> = _shiftTypes

    private val _shiftPatterns = MutableStateFlow<List<ShiftPattern>>(emptyList())
    val shiftPatterns: StateFlow<List<ShiftPattern>> = _shiftPatterns

    fun refreshShiftPatterns() {
        viewModelScope.launch {
            _shiftTypes.value = repository.listShiftTypes()
            _shiftPatterns.value = repository.listShiftPatterns()
        }
    }

    fun saveShiftType(type: ShiftType, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveShiftType(type)
            _shiftTypes.value = repository.listShiftTypes()
            onDone()
        }
    }

    fun deleteShiftType(id: String) {
        viewModelScope.launch {
            repository.deleteShiftType(id)
            _shiftTypes.value = repository.listShiftTypes()
        }
    }

    fun saveShiftPattern(pattern: ShiftPattern, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveShiftPattern(pattern)
            _shiftPatterns.value = repository.listShiftPatterns()
            onDone()
        }
    }

    fun deleteShiftPattern(id: String, removeGeneratedEvents: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteShiftPattern(id, removeGeneratedEvents)
            _shiftPatterns.value = repository.listShiftPatterns()
            onDone()
        }
    }

    fun applyShiftPattern(
        patternId: String,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        accountId: String?,
        onDone: (ShiftApplyResult) -> Unit = {},
    ) {
        viewModelScope.launch {
            onDone(repository.applyShiftPattern(patternId, rangeStart, rangeEnd, accountId))
        }
    }

    fun addShiftOnDate(
        shiftTypeId: String,
        date: LocalDate,
        accountId: String?,
        onDone: (Boolean) -> Unit = {},
    ) {
        viewModelScope.launch {
            onDone(repository.addShiftOnDate(shiftTypeId, date, accountId))
        }
    }

    fun buildShiftPlanShareEvents(
        patternId: String,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        onDone: (List<CalendarEvent>) -> Unit,
    ) {
        viewModelScope.launch {
            onDone(repository.buildShiftPlanShareEvents(patternId, rangeStart, rangeEnd))
        }
    }

    fun setAppLockPin(pin: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onResult(runCatching {
                repository.setAppLockPin(pin)
                Unit
            })
        }
    }

    fun verifyAppLockPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repository.verifyAppLockPin(pin))
        }
    }

    fun disableAppLock(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.disableAppLock()
            onDone()
        }
    }

    fun setAppLockEnabled(enabled: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.setAppLockEnabled(enabled)
            onDone()
        }
    }

    fun clearAppLockPin(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAppLockPin()
            onDone()
        }
    }

    fun refreshPrivateVault() {
        viewModelScope.launch {
            _privateVaultEvents.value = repository.listPrivateVaultEvents()
        }
    }

    fun moveToPrivateVault(event: CalendarEvent, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.moveToPrivateVault(event)
            _privateVaultEvents.value = repository.listPrivateVaultEvents()
            onDone()
        }
    }

    fun restoreFromPrivateVault(eventId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.restoreFromPrivateVault(eventId)
            _privateVaultEvents.value = repository.listPrivateVaultEvents()
            onDone()
        }
    }

    fun setAccountVisible(accountId: String, visible: Boolean) {
        viewModelScope.launch {
            repository.setAccountVisible(accountId, visible)
        }
    }

    fun syncNow(onComplete: (Result<CalendarSyncResult>) -> Unit = {}) {
        viewModelScope.launch {
            val result = runCatching { repository.syncNow() }
            onComplete(result)
        }
    }

    fun setBirthdayCalendarEnabled(enabled: Boolean, onComplete: (Result<BirthdayImportResult>) -> Unit = {}) {
        viewModelScope.launch {
            val result = runCatching { repository.setBirthdayCalendarEnabled(enabled) }
            onComplete(result)
        }
    }

    fun addHolidayCountry(item: HolidayCountryUiItem, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = runCatching {
                repository.addHolidayCountry(HolidayCountry(item.code, item.name))
            }
            onComplete(result)
        }
    }

    fun removeHolidayCountry(item: HolidayCountryUiItem, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = runCatching { repository.removeHolidayCountry(item.code) }
            onComplete(result)
        }
    }

    fun refreshBirthdayCalendarIfEnabled() {
        viewModelScope.launch {
            repository.refreshBirthdayCalendarIfEnabled()
        }
    }

    // ----- ICS import / export (Pro) -----

    /** Produces the iCalendar text off the main thread, then hands it to [onReady] for file IO. */
    fun exportIcs(onReady: (Result<String>) -> Unit) {
        viewModelScope.launch {
            onReady(runCatching { repository.exportIcs() })
        }
    }

    /** Parses and upserts the supplied iCalendar text, reporting a summary via [onResult]. */
    fun importIcs(icsText: String, onResult: (Result<DotCalRepository.IcsImportResult>) -> Unit) {
        viewModelScope.launch {
            onResult(runCatching { repository.importIcs(icsText) })
        }
    }

    // ----- Backup / restore (Pro) -----

    /** Produces the backup JSON off the main thread, then hands it to [onReady] for file IO. */
    fun exportBackup(onReady: (Result<String>) -> Unit) {
        viewModelScope.launch {
            onReady(runCatching { repository.exportBackup() })
        }
    }

    /** Restores the supplied backup JSON (non-destructive merge), reporting a summary via [onResult]. */
    fun importBackup(json: String, onResult: (Result<DotCalRepository.BackupImportResult>) -> Unit) {
        viewModelScope.launch {
            onResult(runCatching { repository.importBackup(json) })
        }
    }

    private fun CalendarEvent.startDate(): LocalDate {
        return java.time.Instant.ofEpochMilli(startTimeMs)
            .atZone(safeZoneId(timeZone))
            .toLocalDate()
    }

    private fun safeZoneId(id: String): ZoneId = runCatching { ZoneId.of(id) }.getOrDefault(ZoneId.systemDefault())

    private fun buildDayDensityForecast(events: List<CalendarEvent>): List<DayDensityForecastItem> {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        return List(7) { index ->
            val date = today.plusDays(index.toLong())
            val dayStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val minutes = events
                .asSequence()
                .filter { it.isTask == 0 && it.isAllDay == 0 && it.source != "BIRTHDAY" }
                .map { event ->
                    val start = event.startTimeMs.coerceAtLeast(dayStart)
                    val end = event.endTimeMs.coerceAtMost(dayEnd)
                    ((end - start).coerceAtLeast(0L) / 60_000L).toInt()
                }
                .sum()
            DayDensityForecastItem(
                date = date,
                scheduledMinutes = minutes,
                intensity = when {
                    minutes == 0 -> 0
                    minutes <= 120 -> 1
                    minutes <= 300 -> 2
                    else -> 3
                },
            )
        }
    }
}
