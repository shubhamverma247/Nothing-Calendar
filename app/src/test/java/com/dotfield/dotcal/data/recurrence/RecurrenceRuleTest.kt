package com.dotfield.dotcal.data.recurrence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecurrenceRuleTest {
    @Test
    fun weeklyByDayExpandsRequestedWeekdaysInOrder() {
        val rule = RecurrenceRule.parse("FREQ=WEEKLY;BYDAY=MO,WE,FR")!!
        val firstDate = LocalDate.of(2026, 8, 24)

        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 8, 28),
            ),
            rule.datesForBlock(firstDate, 0),
        )
    }

    @Test
    fun monthlyOrdinalByDayExpandsLastWeekday() {
        val rule = RecurrenceRule.parse("FREQ=MONTHLY;BYDAY=-1FR")!!
        val firstDate = LocalDate.of(2026, 8, 28)

        assertEquals(listOf(LocalDate.of(2026, 8, 28)), rule.datesForBlock(firstDate, 0))
        assertEquals(listOf(LocalDate.of(2026, 9, 25)), rule.datesForBlock(firstDate, 1))
    }

    @Test
    fun yearlyLeapDaySkipsNonLeapYears() {
        val rule = RecurrenceRule.parse("FREQ=YEARLY")!!
        val firstDate = LocalDate.of(2024, 2, 29)

        assertEquals(listOf(LocalDate.of(2024, 2, 29)), rule.datesForBlock(firstDate, 0))
        assertTrue(rule.datesForBlock(firstDate, 1).isEmpty())
        assertTrue(rule.datesForBlock(firstDate, 2).isEmpty())
        assertTrue(rule.datesForBlock(firstDate, 3).isEmpty())
        assertEquals(listOf(LocalDate.of(2028, 2, 29)), rule.datesForBlock(firstDate, 4))
    }

    @Test
    fun dailyIntervalFastForwardsNearRange() {
        val rule = RecurrenceRule.parse("FREQ=DAILY;INTERVAL=3")!!
        val firstDate = LocalDate.of(2026, 8, 1)

        val block = rule.fastForwardBlock(firstDate, LocalDate.of(2026, 8, 20))

        assertEquals(5, block)
        assertEquals(listOf(LocalDate.of(2026, 8, 16)), rule.datesForBlock(firstDate, block))
        assertEquals(listOf(LocalDate.of(2026, 8, 19)), rule.datesForBlock(firstDate, block + 1))
    }
}
