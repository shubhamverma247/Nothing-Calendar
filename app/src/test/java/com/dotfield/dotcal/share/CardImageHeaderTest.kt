package com.dotfield.dotcal.share

import org.junit.Assert.assertEquals
import org.junit.Test

class CardImageHeaderTest {
    @Test
    fun headerTitleHasNoSpacesAroundSlash() {
        assertEquals("DOTCAL/MONTH", CardImageExporter.shareHeaderTitle("month"))
    }

    @Test
    fun periodLabelStaysRightAligned() {
        assertEquals(858f, CardImageExporter.sharePeriodLabelX(150f), 0f)
    }

    @Test
    fun monthCardHeightGrowsForAdditionalEvents() {
        assertEquals(
            true,
            CardImageExporter.monthShareCardHeight(10) > CardImageExporter.monthShareCardHeight(1),
        )
    }
}
