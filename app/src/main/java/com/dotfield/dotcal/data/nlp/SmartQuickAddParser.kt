package com.dotfield.dotcal.data.nlp

import com.dotfield.dotcal.data.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

/**
 * Small, offline command layer on top of [QuickAddParser]. It deliberately returns intent-like
 * values instead of mutating calendar data so the normal UI confirmation and save paths remain in
 * charge of side effects.
 */
sealed interface SmartQuickAddCommand {
    data class Move(
        val eventQuery: String,
        val targetDate: LocalDate,
        val targetTime: LocalTime?,
    ) : SmartQuickAddCommand

    data class Delete(
        val eventQuery: String,
        val date: LocalDate?,
    ) : SmartQuickAddCommand

    data class Query(val date: LocalDate?) : SmartQuickAddCommand

    data class Rename(val eventQuery: String, val newTitle: String) : SmartQuickAddCommand

    data class SetDuration(val eventQuery: String, val minutes: Long) : SmartQuickAddCommand

    data class SetLocation(val eventQuery: String, val location: String) : SmartQuickAddCommand

    data class SetReminder(val eventQuery: String, val minutesBefore: Int?) : SmartQuickAddCommand

    data class AddPrep(
        val eventQuery: String,
        val minutes: Long,
        val before: Boolean,
    ) : SmartQuickAddCommand
}

object SmartQuickAddParser {
    private val DATE_WORDS = Regex("""(?i)today|tomorrow|tmrw|tmr|tom""")
    private val WEEKDAY_WORDS = Regex(
        """(?i)(?:(?:next|agle|agli)\s+)?(?:mon(?:day)?s?|tue(?:sday)?s?|wed(?:nesday)?s?|thu(?:rsday)?s?|fri(?:day)?s?|sat(?:urday)?s?|sun(?:day)?s?)""",
    )

    fun parse(input: String, now: LocalDateTime = LocalDateTime.now()): SmartQuickAddCommand? {
        val value = input.trim()
        if (value.isBlank()) return null

        val move = Regex(
            """(?i)^(?:please\s+)?(?:move|reschedule|shift)\s+(?:my\s+)?(.+?)\s+(?:to|on)\s+(.+)$""",
        ).matchEntire(value)
        if (move != null) {
            val eventQuery = cleanQuery(move.groupValues[1])
            val destination = QuickAddParser.parse("event ${move.groupValues[2]}", now)
            return SmartQuickAddCommand.Move(eventQuery, destination.date, destination.startTime.takeUnless { destination.isAllDay })
        }

        val prep = Regex(
            """(?i)^(?:please\s+)?(?:add|put)\s+(\d+)\s*(?:min|mins|minutes)\s+(?:of\s+)?prep\s+(before|after)\s*(.*)$""",
        ).matchEntire(value)
        if (prep != null) {
            val query = cleanQuery(prep.groupValues[3]).ifBlank { "it" }
            return SmartQuickAddCommand.AddPrep(
                eventQuery = query,
                minutes = prep.groupValues[1].toLongOrNull() ?: return null,
                before = prep.groupValues[2].equals("before", ignoreCase = true),
            )
        }

        val rename = Regex(
            """(?i)^(?:please\s+)?(?:rename|retitle)\s+(?:my\s+)?(.+?)\s+(?:to|as)\s+(.+)$""",
        ).matchEntire(value)
        if (rename != null) {
            return SmartQuickAddCommand.Rename(cleanQuery(rename.groupValues[1]), rename.groupValues[2].trim())
        }

        val setDuration = Regex(
            """(?i)^(?:please\s+)?set\s+(?:the\s+)?(.+?)\s+duration\s+to\s+(\d+)\s*(hours?|hrs?|hr|minutes?|mins?|min|ghante|ghanta)\s*(?:long)?$""",
        ).matchEntire(value)
        val makeDuration = Regex(
            """(?i)^(?:please\s+)?make\s+(?:my\s+)?(.+?)\s+(\d+)\s*(hours?|hrs?|hr|minutes?|mins?|min|ghante|ghanta)\s*(?:long)?$""",
        ).matchEntire(value)
        val duration = setDuration ?: makeDuration
        if (duration != null) {
            val query = cleanQuery(duration.groupValues[1])
            val amount = duration.groupValues[2].toLongOrNull() ?: return null
            val unit = duration.groupValues[3].lowercase(Locale.US)
            val minutes = if (unit.startsWith("h") || unit.startsWith("g")) amount * 60 else amount
            return SmartQuickAddCommand.SetDuration(query, minutes)
        }

        val location = Regex(
            """(?i)^(?:please\s+)?(?:set|change|update)\s+(?:the\s+)?location\s+(?:of\s+)?(.+?)\s+(?:to|as)\s+(.+)$""",
        ).matchEntire(value)
        if (location != null) {
            return SmartQuickAddCommand.SetLocation(cleanQuery(location.groupValues[1]), location.groupValues[2].trim())
        }

        val reminder = Regex(
            """(?i)^(?:please\s+)?(?:add|set)\s+(?:a\s+)?reminder\s+(?:for|to)\s+(.+?)\s+(\d+)\s*(?:min|mins|minutes)\s+before$""",
        ).matchEntire(value)
        if (reminder != null) {
            return SmartQuickAddCommand.SetReminder(
                cleanQuery(reminder.groupValues[1]),
                reminder.groupValues[2].toIntOrNull()?.takeIf { it >= 0 },
            )
        }

        val removeReminder = Regex(
            """(?i)^(?:please\s+)?(?:remove|clear|delete)\s+(?:the\s+)?reminder\s+(?:from|for)\s+(.+)$""",
        ).matchEntire(value)
        if (removeReminder != null) {
            return SmartQuickAddCommand.SetReminder(cleanQuery(removeReminder.groupValues[1]), null)
        }

        val possessiveDelete = Regex(
            """(?i)^(?:please\s+)?(?:delete|remove|cancel)\s+($DATE_WORDS)(?:['’]s)\s+(.+)$""",
        ).matchEntire(value)
        if (possessiveDelete != null) {
            val date = QuickAddParser.parse("event ${possessiveDelete.groupValues[1]}", now).date
            return SmartQuickAddCommand.Delete(cleanQuery(possessiveDelete.groupValues[2]), date)
        }

        val delete = Regex(
            """(?i)^(?:please\s+)?(?:delete|remove|cancel)\s+(?:my\s+)?(.+?)(?:\s+((?:today|tomorrow|tmrw|tmr|tom|kal|parso)))?$""",
        ).matchEntire(value)
        if (delete != null) {
            val dateText = delete.groupValues[2]
            val date = dateText.takeIf { it.isNotBlank() }?.let {
                QuickAddParser.parse("event $it", now).date
            }
            return SmartQuickAddCommand.Delete(cleanQuery(delete.groupValues[1]), date)
        }

        if (Regex("""(?i)^(?:what(?:'s| is)|what\s+do\s+i\s+have|show|list)\b.*""").matches(value)) {
            val dateText = DATE_WORDS.find(value)?.value ?: WEEKDAY_WORDS.find(value)?.value
            val date = dateText?.let { QuickAddParser.parse("event $it", now).date }
            return SmartQuickAddCommand.Query(date)
        }
        return null
    }

    private fun cleanQuery(value: String): String = value
        .trim()
        .replace(Regex("""(?i)\b(?:my|the|event|appointment)\b"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim(' ', ',', '.')
}

/** Resolves command references against the items already exposed by the current calendar view. */
object SmartQuickAddMatcher {
    fun findCandidates(
        command: SmartQuickAddCommand,
        events: List<CalendarEvent>,
        now: LocalDateTime = LocalDateTime.now(),
        contextEvent: CalendarEvent? = null,
    ): List<CalendarEvent> {
        if (command is SmartQuickAddCommand.Query) {
            return events
                .asSequence()
                .filter { it.isCompleted == 0 && it.isTask == 0 }
                .filter { command.date == null || it.localDate() == command.date }
                .sortedBy { it.startTimeMs }
                .distinctBy { it.id.substringBefore("::occurrence::") }
                .take(8)
                .toList()
        }

        val query = when (command) {
            is SmartQuickAddCommand.Move -> command.eventQuery
            is SmartQuickAddCommand.Delete -> command.eventQuery
            is SmartQuickAddCommand.AddPrep -> command.eventQuery
            is SmartQuickAddCommand.Rename -> command.eventQuery
            is SmartQuickAddCommand.SetDuration -> command.eventQuery
            is SmartQuickAddCommand.SetLocation -> command.eventQuery
            is SmartQuickAddCommand.SetReminder -> command.eventQuery
            is SmartQuickAddCommand.Query -> ""
        }
        if (query.equals("it", ignoreCase = true) && contextEvent != null) return listOf(contextEvent)

        val parsedQuery = QuickAddParser.parse(query, now)
        val queryWords = query.lowercase(Locale.US)
            .split(Regex("""\s+"""))
            .filter { it.length > 1 && it !in setOf("at", "on", "for") }
        val dateFilter = when (command) {
            is SmartQuickAddCommand.Delete -> command.date
            else -> null
        }
        return events
            .asSequence()
            .filter { it.isCompleted == 0 && it.isTask == 0 }
            .filter { dateFilter == null || it.localDate() == dateFilter }
            .map { event ->
                val title = event.title.lowercase(Locale.US)
                val eventTime = event.localStartTime()
                val timeMatch = queryWords.isNotEmpty() && parsedQuery.startTime != null &&
                    event.localDate() == parsedQuery.date && eventTime == parsedQuery.startTime
                val wordScore = queryWords.count { word -> title.contains(word) }
                val exactScore = if (title == query.lowercase(Locale.US)) 10 else 0
                event to (exactScore + wordScore * 3 + if (timeMatch) 8 else 0)
            }
            .filter { (_, score) -> score > 0 }
            .sortedWith(compareByDescending<Pair<CalendarEvent, Int>> { it.second }.thenBy { it.first.startTimeMs })
            .map { it.first }
            .distinctBy { it.id.substringBefore("::occurrence::") }
            .take(8)
            .toList()
    }

    private fun CalendarEvent.localDate(): LocalDate = Instant.ofEpochMilli(startTimeMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    private fun CalendarEvent.localStartTime(): LocalTime = Instant.ofEpochMilli(startTimeMs)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
}
