package com.dotfield.dotcal.presentation.datecalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Pure date-math for the Date Calculator Pro feature. No database, no network.
 * Result recalculates automatically as a derived flow whenever any input changes.
 */
class DateCalculatorViewModel : ViewModel() {

    enum class Mode { DAYS_BETWEEN, ADD_SUBTRACT }

    sealed class CalculatorResult {
        data class DaysBetween(
            val totalDays: Int,
            val workingDays: Int,
            val weekends: Int,
        ) : CalculatorResult()

        data class AddSubtractResult(
            val resultDate: LocalDate,
            val formattedDate: String,
        ) : CalculatorResult()
    }

    val mode = MutableStateFlow(Mode.DAYS_BETWEEN)
    val fromDate = MutableStateFlow<LocalDate?>(null)
    val toDate = MutableStateFlow<LocalDate?>(null)
    val startDate = MutableStateFlow<LocalDate?>(null)
    val daysCount = MutableStateFlow(0)
    val isSubtract = MutableStateFlow(false)

    fun setMode(value: Mode) { mode.value = value }
    fun setFromDate(value: LocalDate) { fromDate.value = value }
    fun setToDate(value: LocalDate) { toDate.value = value }
    fun setStartDate(value: LocalDate) { startDate.value = value }
    fun setDaysCount(value: Int) { daysCount.value = value.coerceAtLeast(0) }
    fun setSubtract(value: Boolean) { isSubtract.value = value }

    private val daysBetweenInputs = combine(fromDate, toDate) { from, to -> from to to }
    private val addSubtractInputs = combine(startDate, daysCount, isSubtract) { start, days, subtract ->
        Triple(start, days, subtract)
    }

    val result: StateFlow<CalculatorResult?> =
        combine(mode, daysBetweenInputs, addSubtractInputs) { currentMode, between, addSub ->
            compute(
                mode = currentMode,
                from = between.first,
                to = between.second,
                start = addSub.first,
                days = addSub.second,
                subtract = addSub.third,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun compute(
        mode: Mode,
        from: LocalDate?,
        to: LocalDate?,
        start: LocalDate?,
        days: Int,
        subtract: Boolean,
    ): CalculatorResult? = when (mode) {
        Mode.DAYS_BETWEEN -> {
            if (from == null || to == null) {
                null
            } else {
                val earlier = minOf(from, to)
                val later = maxOf(from, to)
                val total = ChronoUnit.DAYS.between(earlier, later).toInt()
                var weekends = 0
                var cursor = earlier
                repeat(total) {
                    val dow = cursor.dayOfWeek
                    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) weekends++
                    cursor = cursor.plusDays(1)
                }
                CalculatorResult.DaysBetween(
                    totalDays = total,
                    workingDays = total - weekends,
                    weekends = weekends,
                )
            }
        }
        Mode.ADD_SUBTRACT -> {
            if (start == null) {
                null
            } else {
                val resultDate = if (subtract) {
                    start.minusDays(days.toLong())
                } else {
                    start.plusDays(days.toLong())
                }
                CalculatorResult.AddSubtractResult(
                    resultDate = resultDate,
                    formattedDate = resultDate.format(RESULT_FORMATTER).uppercase(Locale.getDefault()),
                )
            }
        }
    }

    private companion object {
        private val RESULT_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.getDefault())
    }
}
