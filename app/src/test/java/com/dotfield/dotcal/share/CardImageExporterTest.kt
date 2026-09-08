package com.dotfield.dotcal.share

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CardImageExporterTest {
    @Test
    fun monthEmptyLabelSitsBelowCalendarGrid() {
        assertEquals(1035f, CardImageExporter.emptyShareLabelY("month"), 0f)
    }

    @Test
    fun weekShareDoesNotDrawEmptyLabel() {
        assertFalse(CardImageExporter.shouldDrawEmptyShareLabel("week"))
        assertTrue(CardImageExporter.shouldDrawEmptyShareLabel("month"))
    }

    @Test
    fun monthSharePeriodLabelUsesYearAndNumericMonth() {
        assertEquals("2026/9", CardImageExporter.sharePeriodLabel("month", LocalDate.of(2026, 9, 6)))
    }

    @Test
    fun nonMonthShareHasNoMonthPeriodLabel() {
        assertEquals(null, CardImageExporter.sharePeriodLabel("week", LocalDate.of(2026, 9, 6)))
    }
}
