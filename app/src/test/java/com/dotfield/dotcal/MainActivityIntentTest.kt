package com.dotfield.dotcal

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MainActivityIntentTest {
    @Test
    fun calendarViewIntentExposesContentUri() {
        assertEquals(
            "content://files/events.ics",
            externalCalendarUri(Intent.ACTION_VIEW, "text/calendar", "content://files/events.ics"),
        )
    }

    @Test
    fun calendarViewIntentAcceptsMimeParameters() {
        assertEquals(
            "content://files/events.ics",
            externalCalendarUri(Intent.ACTION_VIEW, "text/calendar; charset=utf-8", "content://files/events.ics"),
        )
    }

    @Test
    fun nonCalendarIntentIsIgnored() {
        assertNull(externalCalendarUri(Intent.ACTION_SEND, "text/calendar", "content://files/events.ics"))
        assertNull(externalCalendarUri(Intent.ACTION_VIEW, "text/plain", "content://files/events.ics"))
        assertNull(externalCalendarUri(Intent.ACTION_VIEW, "text/calendar", null))
    }
}
