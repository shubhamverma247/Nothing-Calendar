package com.dotfield.dotcal.data.countdown

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class CountdownPinStoreTest {
    @Test
    fun daysUntilUsesLocalDatesAcrossDst() {
        val zone = ZoneId.of("America/New_York")
        val now = LocalDateTime.of(2026, 3, 7, 23, 30).atZone(zone).toInstant().toEpochMilli()
        val target = LocalDateTime.of(2026, 3, 9, 0, 30).atZone(zone).toInstant().toEpochMilli()

        assertEquals(2, CountdownPinStore.daysUntil(target, zone, now))
    }

    @Test
    fun daysUntilAllDayStyleDateCountsCalendarDays() {
        assertEquals(
            23,
            CountdownPinStore.daysUntil(
                targetDate = LocalDate.of(2026, 8, 6),
                nowDate = LocalDate.of(2026, 7, 14),
            ),
        )
    }

    @Test
    fun daysUntilTodayIsZeroForTimedEvents() {
        val zone = ZoneId.of("Asia/Kolkata")
        val now = LocalDateTime.of(2026, 7, 14, 9, 0).atZone(zone).toInstant().toEpochMilli()
        val target = LocalDateTime.of(2026, 7, 14, 21, 0).atZone(zone).toInstant().toEpochMilli()

        assertEquals(0, CountdownPinStore.daysUntil(target, zone, now))
    }
}
