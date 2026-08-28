package com.dotfield.dotcal.data.nlp

import com.dotfield.dotcal.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class SmartQuickAddParserTest {
    private val now = LocalDateTime.of(2026, 3, 10, 10, 0)

    @Test
    fun parsesMoveWithDateAndPreservesOptionalTime() {
        val command = SmartQuickAddParser.parse("Move my 2pm to tomorrow", now)
        assertTrue(command is SmartQuickAddCommand.Move)
        command as SmartQuickAddCommand.Move
        assertEquals("2pm", command.eventQuery)
        assertEquals(LocalDate.of(2026, 3, 11), command.targetDate)
        assertEquals(null, command.targetTime)
    }

    @Test
    fun parsesDeleteWithPossessiveDate() {
        val command = SmartQuickAddParser.parse("delete tomorrow's gym", now)
        assertTrue(command is SmartQuickAddCommand.Delete)
        command as SmartQuickAddCommand.Delete
        assertEquals("gym", command.eventQuery)
        assertEquals(LocalDate.of(2026, 3, 11), command.date)
    }

    @Test
    fun parsesQueryDate() {
        val command = SmartQuickAddParser.parse("what do I have tomorrow", now)
        assertTrue(command is SmartQuickAddCommand.Query)
        assertEquals(LocalDate.of(2026, 3, 11), (command as SmartQuickAddCommand.Query).date)
    }

    @Test
    fun parsesWeekdayQueryDate() {
        val command = SmartQuickAddParser.parse("show me friday", now)
        assertTrue(command is SmartQuickAddCommand.Query)
        assertEquals(LocalDate.of(2026, 3, 13), (command as SmartQuickAddCommand.Query).date)
    }

    @Test
    fun parsesPrepReferenceAndDirection() {
        val command = SmartQuickAddParser.parse("add 30 min prep before it", now)
        assertEquals(
            SmartQuickAddCommand.AddPrep("it", 30, before = true),
            command,
        )
        assertEquals(LocalTime.of(10, 0), now.toLocalTime())
    }

    @Test
    fun parsesTextAndReminderEdits() {
        assertEquals(
            SmartQuickAddCommand.Rename("gym", "strength training"),
            SmartQuickAddParser.parse("rename gym to strength training", now),
        )
        assertEquals(
            SmartQuickAddCommand.SetLocation("gym", "Studio 2"),
            SmartQuickAddParser.parse("set location of gym to Studio 2", now),
        )
        assertEquals(
            SmartQuickAddCommand.SetReminder("gym", 15),
            SmartQuickAddParser.parse("add reminder for gym 15 minutes before", now),
        )
        assertEquals(
            SmartQuickAddCommand.SetReminder("gym", null),
            SmartQuickAddParser.parse("delete reminder from gym", now),
        )
    }

    @Test
    fun parsesDurationInHours() {
        assertEquals(
            SmartQuickAddCommand.SetDuration("deep work", 120),
            SmartQuickAddParser.parse("set deep work duration to 2 hours", now),
        )
    }

    @Test
    fun matchesEventByTimeQuery() {
        val event = event("Gym", LocalDateTime.of(2026, 3, 10, 14, 0))
        val command = SmartQuickAddParser.parse("move my 2pm to tomorrow", now)!!
        assertEquals(listOf(event), SmartQuickAddMatcher.findCandidates(command, listOf(event), now))
    }

    @Test
    fun resolvesItToConversationContext() {
        val event = event("Gym", LocalDateTime.of(2026, 3, 10, 14, 0))
        val command = SmartQuickAddParser.parse("add 30 min prep before it", now)!!
        assertEquals(
            listOf(event),
            SmartQuickAddMatcher.findCandidates(command, emptyList(), now, contextEvent = event),
        )
    }

    private fun event(title: String, start: LocalDateTime): CalendarEvent {
        val zone = ZoneId.systemDefault()
        return CalendarEvent(
            id = title,
            accountId = "LOCAL",
            title = title,
            startTimeMs = start.atZone(zone).toInstant().toEpochMilli(),
            endTimeMs = start.plusHours(1).atZone(zone).toInstant().toEpochMilli(),
            timeZone = zone.id,
            isAllDay = 0,
            colorHex = null,
            rrule = null,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = 0,
            updatedAtMs = 0,
        )
    }
}
