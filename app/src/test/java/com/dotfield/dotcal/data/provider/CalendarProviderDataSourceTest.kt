package com.dotfield.dotcal.data.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class CalendarProviderDataSourceTest {
    @Test
    fun providerCalendarIdParsesProviderAccountIds() {
        assertEquals(42L, providerCalendarId(providerAccountId(42L)))
    }

    @Test
    fun providerCalendarIdRejectsLocalAndMalformedIds() {
        assertNull(providerCalendarId("local"))
        assertNull(providerCalendarId("provider-calendar-"))
        assertNull(providerCalendarId("provider-calendar-not-a-number"))
    }

    @Test
    fun providerDurationMillisParsesAllDayMultiDayDuration() {
        assertEquals(2L * 24L * 60L * 60L * 1000L, providerDurationMillis("P2D"))
    }

    @Test
    fun providerDurationMillisParsesTimedDuration() {
        assertEquals(90L * 60L * 1000L, providerDurationMillis("PT1H30M"))
    }

    @Test
    fun providerDurationMillisRejectsInvalidOrEmptyDuration() {
        assertNull(providerDurationMillis(null))
        assertNull(providerDurationMillis(""))
        assertNull(providerDurationMillis("P"))
        assertNull(providerDurationMillis("not-a-duration"))
    }

    @Test
    fun providerEventEndTimeUsesDurationForRecurringEvents() {
        val start = 1_000L
        val lastDate = start + 30L * 24L * 60L * 60L * 1000L

        assertEquals(
            start + 60L * 60L * 1000L,
            providerEventEndTimeMs(
                startTimeMs = start,
                dtEndMs = null,
                duration = "PT1H",
                lastDateMs = lastDate,
                rrule = "FREQ=DAILY;COUNT=30",
            ),
        )
    }

    @Test
    fun providerEventEndTimeDoesNotTreatRecurringLastDateAsOccurrenceEnd() {
        val start = 1_000L
        val lastDate = start + 30L * 24L * 60L * 60L * 1000L

        assertEquals(
            start + 60L * 60L * 1000L,
            providerEventEndTimeMs(
                startTimeMs = start,
                dtEndMs = null,
                duration = null,
                lastDateMs = lastDate,
                rrule = "FREQ=DAILY;COUNT=30",
            ),
        )
    }

    @Test
    fun providerEventEndTimeKeepsLastDateFallbackForNonRecurringEvents() {
        val start = 1_000L
        val lastDate = start + 2L * 60L * 60L * 1000L

        assertEquals(
            lastDate,
            providerEventEndTimeMs(
                startTimeMs = start,
                dtEndMs = null,
                duration = null,
                lastDateMs = lastDate,
                rrule = null,
            ),
        )
    }

    @Test
    fun normalizedProviderReminderMinutesDeduplicatesSortsAndRejectsNegativeValues() {
        assertEquals(listOf(0, 10, 30), listOf(30, -1, 10, 30, 0).normalizedProviderReminderMinutes())
    }

    @Test
    fun providerAvailabilityMapsFreeToNonBlockingOnly() {
        assertEquals(true, providerAvailabilityIsNonBlocking(1))
        assertEquals(false, providerAvailabilityIsNonBlocking(0))
        assertEquals(false, providerAvailabilityIsNonBlocking(2))
        assertEquals(false, providerAvailabilityIsNonBlocking(null))
    }

    @Test
    fun normalizedProviderRdateTrimsAndDropsBlankValues() {
        assertEquals("20260822T093000Z", normalizedProviderRdate(" 20260822T093000Z "))
        assertNull(normalizedProviderRdate(""))
        assertNull(normalizedProviderRdate(null))
    }

    @Test
    fun providerRdateParsesTimedUtcAndLocalValues() {
        val zone = ZoneId.of("America/New_York")
        val utc = LocalDateTime.of(2026, 8, 22, 9, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
        val local = LocalDateTime.of(2026, 8, 23, 10, 0).atZone(zone).toInstant().toEpochMilli()

        assertEquals(
            listOf(utc, local),
            providerRdateStartTimes("20260822T093000Z,20260823T100000", 0, zone.id),
        )
    }

    @Test
    fun providerRdateParsesAllDayValuesAsCalendarDates() {
        val zone = ZoneId.of("Asia/Kolkata")
        val first = LocalDate.of(2026, 8, 22).atStartOfDay(zone).toInstant().toEpochMilli()
        val second = LocalDate.of(2026, 8, 23).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(
            listOf(first, second),
            providerRdateStartTimes("20260822,20260823", 1, zone.id),
        )
    }

    @Test
    fun allDayBoundaryConversionPreservesCalendarDateAcrossTimezones() {
        val utcBoundary = LocalDate.of(2026, 8, 22).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val localBoundary = convertAllDayBoundary(utcBoundary, ZoneOffset.UTC, ZoneId.of("America/Los_Angeles"))

        assertEquals(
            LocalDate.of(2026, 8, 22).atStartOfDay(ZoneId.of("America/Los_Angeles")).toInstant().toEpochMilli(),
            localBoundary,
        )
        assertEquals(
            utcBoundary,
            convertAllDayBoundary(localBoundary, ZoneId.of("America/Los_Angeles"), ZoneOffset.UTC),
        )
    }

    @Test
    fun providerEventTimeZoneUsesUtcForAllDayEvents() {
        assertEquals("UTC", providerEventTimeZone(1, "Asia/Kolkata"))
        assertEquals("Asia/Kolkata", providerEventTimeZone(0, "Asia/Kolkata"))
    }

    @Test
    fun providerDurationFormatsAllDayRecurringEventsAsDays() {
        val zone = ZoneId.of("Asia/Kolkata")
        val start = LocalDate.of(2026, 8, 22).atStartOfDay(zone).toInstant().toEpochMilli()
        val event = providerDurationEvent(
            startTimeMs = start,
            endTimeMs = start + 2L * 24L * 60L * 60L * 1000L,
            isAllDay = 1,
            timeZone = zone.id,
        )

        assertEquals("P2D", event.providerDuration())
    }

    @Test
    fun providerDurationFormatsTimedRecurringEventsAsSeconds() {
        val start = LocalDateTime.of(2026, 8, 22, 9, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
        val event = providerDurationEvent(
            startTimeMs = start,
            endTimeMs = start + 90L * 60L * 1000L,
            isAllDay = 0,
            timeZone = "UTC",
        )

        assertEquals("PT5400S", event.providerDuration())
    }

    @Test
    fun providerReminderAlarmRequestCodeIsStableForEventAndMinutes() {
        assertEquals(
            providerReminderAlarmRequestCode("event-1", 15),
            providerReminderAlarmRequestCode("event-1", 15),
        )
    }

    @Test
    fun providerExdateFromExceptionDatesFormatsTimedExceptionsAsUtc() {
        val ms = LocalDateTime.of(2026, 8, 22, 9, 30).toInstant(ZoneOffset.UTC).toEpochMilli()

        assertEquals("20260822T093000Z", providerExdateFromExceptionDates("[$ms]", 0, "UTC"))
    }

    @Test
    fun exceptionDatesFromProviderExdateParsesTimedUtcExceptions() {
        val ms = LocalDateTime.of(2026, 8, 22, 9, 30).toInstant(ZoneOffset.UTC).toEpochMilli()

        assertEquals("[$ms]", exceptionDatesFromProviderExdate("20260822T093000Z", 0, "UTC"))
    }

    @Test
    fun providerExdateRoundTripsAllDayExceptionsAsDateOnly() {
        val ms = LocalDate.of(2026, 8, 22).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals("20260822", providerExdateFromExceptionDates("[$ms]", 1, "UTC"))
        assertEquals("[$ms]", exceptionDatesFromProviderExdate("20260822", 1, "UTC"))
    }

    @Test
    fun providerExdateParsesLocalTimedExceptionsAcrossDstBoundary() {
        val zone = ZoneId.of("America/New_York")
        val beforeDst = LocalDateTime.of(2026, 3, 7, 9, 0).atZone(zone).toInstant().toEpochMilli()
        val afterDst = LocalDateTime.of(2026, 3, 9, 9, 0).atZone(zone).toInstant().toEpochMilli()

        assertEquals(
            "[$beforeDst,$afterDst]",
            exceptionDatesFromProviderExdate("20260307T090000,20260309T090000", 0, zone.id),
        )
    }

    @Test
    fun providerCanceledOccurrenceUsesItsOwnDurationAndProviderTime() {
        val start = LocalDateTime.of(2026, 8, 22, 9, 30).toInstant(ZoneOffset.UTC).toEpochMilli()

        assertEquals("P3600S", providerCanceledOccurrenceDuration(start, start + 60 * 60 * 1000L, 0))
        assertEquals(start, providerCanceledOccurrenceTime(start, 0, "UTC"))
    }

    @Test
    fun providerCanceledAllDayOccurrenceUsesUtcCalendarBoundary() {
        val zone = ZoneId.of("Asia/Kolkata")
        val localStart = LocalDate.of(2026, 8, 22).atStartOfDay(zone).toInstant().toEpochMilli()
        val utcStart = LocalDate.of(2026, 8, 22).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals("P1D", providerCanceledOccurrenceDuration(localStart, localStart + 24 * 60 * 60 * 1000L, 1))
        assertEquals(utcStart, providerCanceledOccurrenceTime(localStart, 1, zone.id))
    }

    private fun providerDurationEvent(
        startTimeMs: Long,
        endTimeMs: Long,
        isAllDay: Int,
        timeZone: String,
    ) = com.dotfield.dotcal.data.CalendarEvent(
        id = "duration-event",
        accountId = "provider-calendar-1",
        title = "Duration",
        startTimeMs = startTimeMs,
        endTimeMs = endTimeMs,
        timeZone = timeZone,
        isAllDay = isAllDay,
        colorHex = null,
        rrule = "FREQ=DAILY;COUNT=2",
        source = "GOOGLE",
        googleEventId = "1",
        googleCalendarId = "1",
        isTask = 0,
        isCompleted = 0,
        completedAtMs = null,
        voiceNotePath = null,
        createdAtMs = startTimeMs,
        updatedAtMs = startTimeMs,
    )
}
