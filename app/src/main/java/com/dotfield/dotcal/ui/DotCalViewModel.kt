package com.dotfield.dotcal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.RecurringEditScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

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

    val reminders: StateFlow<List<EventReminder>> = repository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun saveEvent(
        existing: CalendarEvent?,
        data: EventEditorData,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
    ) {
        viewModelScope.launch {
            repository.saveLocalEvent(
                existing = existing,
                data = data,
                recurringEditScope = recurringEditScope,
            )
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
}
