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
}
