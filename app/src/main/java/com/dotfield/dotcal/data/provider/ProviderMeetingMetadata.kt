package com.dotfield.dotcal.data.provider

data class ProviderAttendee(
    val name: String? = null,
    val email: String? = null,
    val status: Int? = null,
    val type: Int? = null,
    val relationship: Int? = null,
)

data class ProviderMeetingMetadata(
    val organizer: String? = null,
    val accessLevel: Int? = null,
    val availability: Int? = null,
    val guestsCanModify: Boolean? = null,
    val guestsCanInviteOthers: Boolean? = null,
    val guestsCanSeeGuests: Boolean? = null,
    val attendees: List<ProviderAttendee> = emptyList(),
)

internal fun encodeProviderMeetingMetadata(metadata: ProviderMeetingMetadata): String? {
    if (metadata.isBlank()) return null
    return buildString {
        append('{')
        val fields = mutableListOf<String>()
        metadata.organizer?.takeUnless { it.isBlank() }?.let { fields += jsonField("organizer", it) }
        metadata.accessLevel?.let { fields += jsonField("accessLevel", it) }
        metadata.availability?.let { fields += jsonField("availability", it) }
        metadata.guestsCanModify?.let { fields += jsonField("guestsCanModify", it) }
        metadata.guestsCanInviteOthers?.let { fields += jsonField("guestsCanInviteOthers", it) }
        metadata.guestsCanSeeGuests?.let { fields += jsonField("guestsCanSeeGuests", it) }
        if (metadata.attendees.isNotEmpty()) fields += "\"attendees\":${metadata.attendees.toJsonArray()}"
        append(fields.joinToString(","))
        append('}')
    }
}

internal fun decodeProviderMeetingMetadata(json: String?): ProviderMeetingMetadata? {
    if (json.isNullOrBlank()) return null
    return runCatching {
        val root = MeetingJsonParser(json).parseObject()
        ProviderMeetingMetadata(
            organizer = root["organizer"] as? String,
            accessLevel = root["accessLevel"] as? Int,
            availability = root["availability"] as? Int,
            guestsCanModify = root["guestsCanModify"] as? Boolean,
            guestsCanInviteOthers = root["guestsCanInviteOthers"] as? Boolean,
            guestsCanSeeGuests = root["guestsCanSeeGuests"] as? Boolean,
            attendees = (root["attendees"] as? List<*>).orEmpty().mapNotNull { item ->
                val attendee = item as? Map<*, *> ?: return@mapNotNull null
                ProviderAttendee(
                    name = attendee["name"] as? String,
                    email = attendee["email"] as? String,
                    status = attendee["status"] as? Int,
                    type = attendee["type"] as? Int,
                    relationship = attendee["relationship"] as? Int,
                )
            },
        )
    }.getOrNull()
}

private fun ProviderMeetingMetadata.isBlank(): Boolean {
    return organizer.isNullOrBlank() &&
        accessLevel == null &&
        availability == null &&
        guestsCanModify == null &&
        guestsCanInviteOthers == null &&
        guestsCanSeeGuests == null &&
        attendees.isEmpty()
}

private fun List<ProviderAttendee>.toJsonArray(): String {
    return joinToString(prefix = "[", postfix = "]") { attendee ->
        buildString {
            append('{')
            val fields = mutableListOf<String>()
            attendee.name?.takeUnless { it.isBlank() }?.let { fields += jsonField("name", it) }
            attendee.email?.takeUnless { it.isBlank() }?.let { fields += jsonField("email", it) }
            attendee.status?.let { fields += jsonField("status", it) }
            attendee.type?.let { fields += jsonField("type", it) }
            attendee.relationship?.let { fields += jsonField("relationship", it) }
            append(fields.joinToString(","))
            append('}')
        }
    }
}

private fun jsonField(name: String, value: String): String = "\"$name\":\"${escapeJson(value)}\""

private fun jsonField(name: String, value: Int): String = "\"$name\":$value"

private fun jsonField(name: String, value: Boolean): String = "\"$name\":$value"

private fun escapeJson(value: String): String = buildString(value.length) {
    value.forEach { char ->
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
}

private class MeetingJsonParser(private val text: String) {
    private var index = 0

    fun parseObject(): Map<String, Any> {
        val result = linkedMapOf<String, Any>()
        consume('{')
        skipWhitespace()
        while (!peek('}')) {
            val key = readString()
            consume(':')
            result[key] = readValue()
            skipWhitespace()
            if (peek(',')) consume(',') else break
        }
        consume('}')
        skipWhitespace()
        if (index != text.length) error("Trailing data")
        return result
    }

    private fun readObjectValue(): Map<String, Any> {
        val result = linkedMapOf<String, Any>()
        consume('{')
        skipWhitespace()
        while (!peek('}')) {
            val key = readString()
            consume(':')
            result[key] = readValue()
            skipWhitespace()
            if (peek(',')) consume(',') else break
        }
        consume('}')
        return result
    }

    private fun readArray(): List<Any> {
        val result = mutableListOf<Any>()
        consume('[')
        skipWhitespace()
        while (!peek(']')) {
            result += readValue()
            skipWhitespace()
            if (peek(',')) consume(',') else break
        }
        consume(']')
        return result
    }

    private fun readValue(): Any {
        skipWhitespace()
        return when {
            peek('"') -> readString()
            peek('{') -> readObjectValue()
            peek('[') -> readArray()
            matchLiteral("true") -> true
            matchLiteral("false") -> false
            else -> readInt()
        }
    }

    private fun readInt(): Int {
        skipWhitespace()
        val start = index
        if (index < text.length && text[index] == '-') index += 1
        while (index < text.length && text[index].isDigit()) index += 1
        if (start == index) error("Expected int")
        return text.substring(start, index).toInt()
    }

    private fun readString(): String {
        skipWhitespace()
        consume('"')
        val result = StringBuilder()
        while (index < text.length) {
            val char = text[index++]
            when (char) {
                '"' -> return result.toString()
                '\\' -> result.append(readEscape())
                else -> result.append(char)
            }
        }
        error("Unterminated string")
    }

    private fun readEscape(): Char {
        if (index >= text.length) error("Unterminated escape")
        return when (val escaped = text[index++]) {
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            '"', '\\' -> escaped
            else -> escaped
        }
    }

    private fun matchLiteral(value: String): Boolean {
        skipWhitespace()
        if (!text.regionMatches(index, value, 0, value.length)) return false
        index += value.length
        return true
    }

    private fun consume(expected: Char) {
        skipWhitespace()
        if (index >= text.length || text[index] != expected) error("Expected $expected")
        index += 1
    }

    private fun peek(char: Char): Boolean {
        skipWhitespace()
        return index < text.length && text[index] == char
    }

    private fun skipWhitespace() {
        while (index < text.length && text[index].isWhitespace()) index += 1
    }
}
