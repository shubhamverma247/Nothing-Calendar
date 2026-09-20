package com.dotfield.dotcal.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderCenterItemTest {
    private val item = ReminderCenterItem(1, "event", "Focus", 2_000L, 0, 15, 1_000L, 42, 0)

    @Test
    fun pendingReminderIsOverdueAtOrAfterTrigger() {
        assertTrue(item.isOverdue(1_000L))
        assertTrue(item.isOverdue(1_001L))
    }

    @Test
    fun pendingReminderIsNotOverdueBeforeTrigger() {
        assertFalse(item.isOverdue(999L))
    }
}
