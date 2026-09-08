package com.dotfield.dotcal.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class QrEventShareTest {

    @Test
    fun nullTitleUsesFallback() {
        assertEquals("Untitled", normalizeQrShareText(null, "Untitled"))
    }

    @Test
    fun blankTitleUsesFallback() {
        assertEquals("Untitled", normalizeQrShareText("   ", "Untitled"))
    }

    @Test
    fun nonBlankTitleIsPreserved() {
        assertEquals("Team sync", normalizeQrShareText("Team sync", "Untitled"))
    }
}
