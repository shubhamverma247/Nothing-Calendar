package com.dotfield.dotcal.data.countdown

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object CountdownPinStore {
    const val Namespace = "countdown_pins"

    fun daysUntil(startTimeMs: Long, zoneId: ZoneId, nowMs: Long = System.currentTimeMillis()): Long {
        val today = Instant.ofEpochMilli(nowMs).atZone(zoneId).toLocalDate()
        val target = Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDate()
        return ChronoUnit.DAYS.between(today, target).coerceAtLeast(0)
    }

    fun daysUntil(targetDate: LocalDate, nowDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(nowDate, targetDate).coerceAtLeast(0)
    }
}

sealed interface CountdownPinResult {
    data object Pinned : CountdownPinResult
    data class FreeLimitReached(val activeEventId: String) : CountdownPinResult
}
