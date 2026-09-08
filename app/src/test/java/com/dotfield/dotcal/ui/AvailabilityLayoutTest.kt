package com.dotfield.dotcal.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityLayoutTest {
    @Test
    fun suggestionCardStaysMountedWhileAvailabilityLoads() {
        assertTrue(shouldRenderAvailabilitySuggestions(isLoading = true, error = null))
    }

    @Test
    fun suggestionCardHidesOnlyWhenAvailabilityHasError() {
        assertTrue(shouldRenderAvailabilitySuggestions(isLoading = false, error = null))
        assertFalse(shouldRenderAvailabilitySuggestions(isLoading = true, error = "failed"))
    }
}
