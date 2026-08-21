package com.dotfield.dotcal.data.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
}
