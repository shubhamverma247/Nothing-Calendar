package com.dotfield.dotcal.data.nlp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class QuickAddParserTest {

    private val now = LocalDateTime.of(2026, 3, 10, 10, 0)

    @Test
    fun parsesRelativeDates() {
        assertDate("doctor today 1pm", 2026, 3, 10)
        assertDate("lunch tomorrow 1pm", 2026, 3, 11)
        assertDate("lunch kal 1pm", 2026, 3, 11)
        assertDate("movie parso 8pm", 2026, 3, 12)
        assertDate("renew license in 3 days", 2026, 3, 13)
        assertDate("review in 2 weeks", 2026, 3, 24)
    }

    @Test
    fun parsesWeekdayDates() {
        assertDate("gym next monday", 2026, 3, 16)
        assertDate("gym agle somvar", 2026, 3, 16)
        assertDate("market friday", 2026, 3, 13)
        assertDate("family ravivar", 2026, 3, 15)
    }

    @Test
    fun parsesAbsoluteDates() {
        assertDate("mummy ka birthday 14 march", 2026, 3, 14)
        assertDate("mummy ka birthday march 14", 2026, 3, 14)
        assertDate("tax 14/3", 2026, 3, 14)
        assertDate("tax 3/14", 2026, 3, 14)
        assertDate("appointment 14-03-2026", 2026, 3, 14)
        assertDate("rent 1st", 2026, 4, 1)
    }

    @Test
    fun parsesTimesAndRanges() {
        assertTimes("lunch 1pm", LocalTime.of(13, 0), LocalTime.of(14, 0))
        assertTimes("call 13:00", LocalTime.of(13, 0), LocalTime.of(14, 0))
        assertTimes("dentist sham 5 baje", LocalTime.of(17, 0), LocalTime.of(18, 0))
        assertTimes("pooja subah 9", LocalTime.of(9, 0), LocalTime.of(10, 0))
        assertTimes("chai 9 baje", LocalTime.of(9, 0), LocalTime.of(10, 0), LocalDateTime.of(2026, 3, 10, 8, 0))
        assertTimes("work block 2-4pm", LocalTime.of(14, 0), LocalTime.of(16, 0))
        assertTimes("study 2pm to 4pm", LocalTime.of(14, 0), LocalTime.of(16, 0))
        assertTimes("focus sham 5 se 6", LocalTime.of(17, 0), LocalTime.of(18, 0))
    }

    @Test
    fun parsesDurations() {
        assertTimes("deep work 9am for 2 hours", LocalTime.of(9, 0), LocalTime.of(11, 0))
        assertTimes("study 7pm 2 ghante", LocalTime.of(19, 0), LocalTime.of(21, 0))
        assertTimes("walk 6am for 30 minutes", LocalTime.of(6, 0), LocalTime.of(6, 30))
    }

    @Test
    fun parsesRecurrence() {
        assertRule("standup daily 9am", "FREQ=DAILY")
        assertRule("meditation roz 6am", "FREQ=DAILY")
        assertRule("review weekly 10am", "FREQ=WEEKLY")
        assertRule("rent monthly 1st", "FREQ=MONTHLY")
        assertRule("gym every monday 7am", "FREQ=WEEKLY;BYDAY=MO")
        assertRule("gym har somvar 7am", "FREQ=WEEKLY;BYDAY=MO")
        assertRule("training every mon wed fri 7am", "FREQ=WEEKLY;BYDAY=MO,WE,FR")
    }

    @Test
    fun preservesTitleAfterConsumingTokens() {
        assertEquals("Lunch with Rahul", QuickAddParser.parse("Lunch with Rahul kal 1pm", now).title)
        assertEquals("dentist", QuickAddParser.parse("kal sham 5 baje dentist", now).title)
        assertEquals("gym", QuickAddParser.parse("har somvar gym 7am", now).title)
        assertEquals("mummy ka birthday", QuickAddParser.parse("mummy ka birthday 14 march", now).title)
    }

    @Test
    fun detectsAllDayWhenDateHasNoTime() {
        val result = QuickAddParser.parse("mummy ka birthday 14 march", now)
        assertTrue(result.isAllDay)
        assertNull(result.startTime)
        assertNull(result.endTime)
    }

    @Test
    fun rollsPastBareTimeToTomorrow() {
        val result = QuickAddParser.parse("lunch 1pm", LocalDateTime.of(2026, 3, 10, 15, 0))
        assertEquals(LocalDate.of(2026, 3, 11), result.date)
        assertEquals(LocalTime.of(13, 0), result.startTime)
    }

    @Test
    fun degradesToTitleOnlyWithLegacyDefaultTime() {
        val result = QuickAddParser.parse("random text", now)
        assertEquals("random text", result.title)
        assertEquals(LocalDate.of(2026, 3, 10), result.date)
        assertEquals(LocalTime.of(9, 0), result.startTime)
        assertFalse(result.isAllDay)
        assertNull(result.rrule)
    }

    private fun assertDate(text: String, year: Int, month: Int, day: Int) {
        assertEquals(LocalDate.of(year, month, day), QuickAddParser.parse(text, now).date)
    }

    private fun assertTimes(
        text: String,
        start: LocalTime,
        end: LocalTime,
        clock: LocalDateTime = now,
    ) {
        val result = QuickAddParser.parse(text, clock)
        assertEquals(start, result.startTime)
        assertEquals(end, result.endTime)
    }

    private fun assertRule(text: String, expected: String) {
        assertEquals(expected, QuickAddParser.parse(text, now).rrule)
    }
}
