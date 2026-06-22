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
import com.dotfield.dotcal.data.holiday.HolidayCountry
import com.dotfield.dotcal.data.holiday.HolidayDataSource
import com.dotfield.dotcal.sync.CalendarSyncResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
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
class DotCalViewModel(private val repository: DotCalRepository) : ViewModel() {
    private val currentMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val selectedDate = MutableStateFlow(LocalDate.now())

    val month: StateFlow<LocalDate> = currentMonth
    val events: StateFlow<List<CalendarEvent>> = currentMonth
        .flatMapLatest(repository::observeEventsForMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<CalendarEvent>> = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accounts: StateFlow<List<CalendarAccount>> = repository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    private fun CalendarEvent.startDate(): LocalDate {
        return java.time.Instant.ofEpochMilli(startTimeMs)
            .atZone(java.time.ZoneId.of(timeZone))
            .toLocalDate()
    }
}
