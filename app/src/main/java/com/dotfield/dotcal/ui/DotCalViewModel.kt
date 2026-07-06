package com.dotfield.dotcal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dotfield.dotcal.data.BirthdayImportResult
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.RecurringEditScope
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.TaskEditorData
import com.dotfield.dotcal.data.billing.ProManager
import com.dotfield.dotcal.data.privacy.AppLockState
import com.dotfield.dotcal.data.trash.DeletedSnapshot
import com.dotfield.dotcal.data.holiday.HolidayCountry
import com.dotfield.dotcal.data.holiday.HolidayDataSource
import com.dotfield.dotcal.sync.CalendarSyncResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class HolidayCountryUiItem(
    val code: String,
    val name: String,
    val isSelected: Boolean,
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

    val agendaEvents: StateFlow<List<CalendarEvent>> = repository.observeUpcomingAgendaEvents(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<CalendarEvent>> = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accounts: StateFlow<List<CalendarAccount>> = repository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val assignableAccounts: StateFlow<List<CalendarAccount>> = repository.observeAssignableAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _lastSelectedEventAccountId = MutableStateFlow<String?>(null)
    val lastSelectedEventAccountId: StateFlow<String?> = _lastSelectedEventAccountId

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
}
