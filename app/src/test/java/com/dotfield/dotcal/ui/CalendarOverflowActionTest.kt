package com.dotfield.dotcal.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarOverflowActionTest {
    @Test
    fun blankStorageMeansNoHiddenActions() {
        assertTrue(CalendarOverflowAction.hiddenFromStorage(null).isEmpty())
        assertTrue(CalendarOverflowAction.hiddenFromStorage("").isEmpty())
    }

    @Test
    fun hiddenStorageIgnoresUnknownActions() {
        val hidden = CalendarOverflowAction.hiddenFromStorage("search,missing,quick_add")

        assertEquals(setOf(CalendarOverflowAction.Search, CalendarOverflowAction.QuickAdd), hidden)
    }

    @Test
    fun hiddenStorageRoundTripUsesStableKeys() {
        val hidden = setOf(CalendarOverflowAction.Templates, CalendarOverflowAction.AddShift)
        val stored = CalendarOverflowAction.hiddenToStorage(hidden)

        assertEquals("add_shift,templates", stored)
        assertEquals(hidden, CalendarOverflowAction.hiddenFromStorage(stored))
    }
}
