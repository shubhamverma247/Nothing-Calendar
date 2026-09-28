package com.dotfield.dotcal.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventLocationNavigationTest {
    @Test
    fun blankLocationDoesNotCreateNavigationIntent() {
        assertNull(eventLocationNavigationUri("   "))
    }

    @Test
    fun locationCreatesGoogleMapsNavigationUri() {
        assertEquals(
            "google.navigation:q=1600%20Amphitheatre%20Parkway%2C%20Mountain%20View",
            eventLocationNavigationUri("  1600 Amphitheatre Parkway, Mountain View  "),
        )
        assertEquals(
            "com.google.android.apps.maps",
            GOOGLE_MAPS_PACKAGE,
        )
    }
}
