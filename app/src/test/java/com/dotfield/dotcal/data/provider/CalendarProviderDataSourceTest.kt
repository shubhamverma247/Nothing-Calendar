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
}
