package com.dotfield.dotcal.data.scheduling

import java.time.LocalDate
import java.time.LocalTime

data class DeadTimeResult(
    val days: List<DayAvailability>,
) {
    val slots: List<FreeSlot> = days.flatMap(DayAvailability::freeSlots)
}

object DeadTimeFinder {
    const val DEFAULT_START_HOUR = 8
    const val DEFAULT_END_HOUR = 22
    const val MINIMUM_SLOT_MINUTES = 60
    const val DAYS_TO_SEARCH = 7

    fun find(
        startDate: LocalDate,
        busyPeriods: List<BusyPeriod>,
        startHour: Int = DEFAULT_START_HOUR,
        endHour: Int = DEFAULT_END_HOUR,
    ): DeadTimeResult {
        require(startHour in 0..22) { "Start hour must be between 0 and 22" }
        require(endHour in 1..23) { "End hour must be between 1 and 23" }
        require(endHour > startHour) { "End hour must be after start hour" }

        val request = FreeSlotRequest(
            rangeStart = startDate,
            rangeEnd = startDate.plusDays(DAYS_TO_SEARCH - 1L),
            workingStart = LocalTime.of(startHour, 0),
            workingEnd = LocalTime.of(endHour, 0),
            minimumSlotMinutes = MINIMUM_SLOT_MINUTES,
            blockAllDayEvents = true,
            treatGhostsAsBusy = true,
        )
        return DeadTimeResult(FreeSlotEngine.compute(request, busyPeriods))
    }
}
