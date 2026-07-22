package com.dotfield.dotcal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.dotfield.dotcal.data.billing.ProManager
import com.dotfield.dotcal.data.countdown.CountdownPinResult
import com.dotfield.dotcal.data.privacy.AppLockState
import com.dotfield.dotcal.data.profiles.FocusProfile
import com.dotfield.dotcal.data.scheduling.AvailabilityTextFormatter
import com.dotfield.dotcal.data.scheduling.FreeSlot
import com.dotfield.dotcal.data.scheduling.FreeSlotRequest
import com.dotfield.dotcal.data.shifts.ShiftApplyResult
import com.dotfield.dotcal.data.shifts.ShiftPattern
import com.dotfield.dotcal.data.shifts.ShiftType
import com.dotfield.dotcal.data.templates.EventTemplate
import com.dotfield.dotcal.data.trash.DeletedSnapshot
import com.dotfield.dotcal.data.holiday.HolidayCountry
import com.dotfield.dotcal.data.holiday.HolidayDataSource
import com.dotfield.dotcal.data.insights.OnThisDayMemory
import com.dotfield.dotcal.data.punchcard.PunchCardStreak
import com.dotfield.dotcal.sync.CalendarSyncResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    // ----- Pro / Billing -----
    val productDetails = proManager.productDetails
    val purchaseResult = proManager.purchaseResultFlow

    fun purchasePro(activity: android.app.Activity) {
        viewModelScope.launch {
            val result = proManager.launchPurchaseFlow(activity)
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
            }
            onComplete()
        }
    }

    fun closeEventDetail() {
        _detailEvent.value = null
    }

    fun refreshConflictWarnings(
        existing: CalendarEvent?,
        startDate: LocalDate,
        endDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        isAllDay: Boolean,
    ) {
        conflictWarningJob?.cancel()
        if (isAllDay || !endDate.atTime(endTime).isAfter(startDate.atTime(startTime))) {
            _conflictWarnings.value = emptyList()
            return
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
        _conflictWarnings.value = emptyList()
    }

    fun refreshAvailability(request: FreeSlotRequest, use24HourFormat: Boolean) {
        availabilityJob?.cancel()
        _availabilityState.value = _availabilityState.value.copy(isLoading = true, error = null)
        availabilityJob = viewModelScope.launch {
            runCatching {
                val days = repository.computeAvailability(request)
                AvailabilityTextFormatter.format(days, use24HourFormat)
            }.onSuccess { text ->
                _availabilityState.value = AvailabilityUiState(text = text)
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
        onSaved: () -> Unit = {},
    ) {
        viewModelScope.launch {
            repository.saveLocalEvent(
                existing = existing,
                data = data,
                recurringEditScope = recurringEditScope,
            )
            if (existing == null) {
                _lastSelectedEventAccountId.value = data.accountId
            }
            onSaved()
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

    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchItems(query)
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
            .atZone(java.time.ZoneId.of(timeZone))
            .toLocalDate()
    }

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
