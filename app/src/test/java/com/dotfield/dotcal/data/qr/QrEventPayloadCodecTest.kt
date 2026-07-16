package com.dotfield.dotcal.data.qr

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.ics.IcsExporter
import com.dotfield.dotcal.data.ics.IcsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class QrEventPayloadCodecTest {

    private val zone = ZoneId.of("Asia/Kolkata")

    @Test
    fun timedEventRoundTripsThroughExistingIcsParser() {
        val event = event(
            title = "House party",
            start = LocalDateTime.of(2026, 8, 8, 20, 0),
            end = LocalDateTime.of(2026, 8, 8, 23, 0),
            description = "Bring snacks",
            location = "Terrace",
        )

        assertRoundTrip(event)
    }

    @Test
    fun allDayEventRoundTripsThroughExistingIcsParser() {
        val startDate = LocalDate.of(2026, 9, 12)
        val event = event(
            title = "Family day",
            start = startDate.atStartOfDay(),
            end = startDate.plusDays(1).atStartOfDay(),
            isAllDay = true,
        )

        assertRoundTrip(event)
    }

    @Test
    fun recurringEventRoundTripsThroughExistingIcsParser() {
        val event = event(
            title = "Training",
            start = LocalDateTime.of(2026, 7, 20, 7, 0),
            end = LocalDateTime.of(2026, 7, 20, 8, 0),
            rrule = "FREQ=WEEKLY;BYDAY=MO,WE,FR",
        )

        assertRoundTrip(event)
    }

    @Test
    fun oversizedPayloadFallsBackToDescriptionFreeIcs() {
        val event = event(
            title = "Large event",
            start = LocalDateTime.of(2026, 7, 20, 9, 0),
            end = LocalDateTime.of(2026, 7, 20, 10, 0),
            description = noisyText(12_000),
        )
        val fullIcs = IcsExporter.export(listOf(event))
        val compactIcs = IcsExporter.export(listOf(event.copy(description = "")))

        val encoded = QrEventPayloadCodec.encode(fullIcs, compactIcs)
        val decoded = QrEventPayloadCodec.decode(encoded.payload)

        assertTrue(encoded.sharedWithoutDescription)
        assertTrue(decoded is QrEventDecodeResult.Success)
        assertEquals("", IcsParser.parse((decoded as QrEventDecodeResult.Success).icsText).single().description)
    }

    @Test
    fun malformedPayloadsAreRejected() {
        assertTrue(QrEventPayloadCodec.decode("https://example.com") is QrEventDecodeResult.NotDotCal)
        assertTrue(QrEventPayloadCodec.decode("DOTCAL2:abcd") is QrEventDecodeResult.UnsupportedVersion)
        assertTrue(QrEventPayloadCodec.decode("DOTCAL1:not-base64%%%") is QrEventDecodeResult.Malformed)
        assertTrue(QrEventPayloadCodec.decode("DOTCAL1:SGVsbG8=") is QrEventDecodeResult.Malformed)
    }

    @Test
    fun foreignIcsTextIsAcceptedForInterop() {
        val ics = IcsExporter.export(
            listOf(
                event(
                    title = "Foreign invite",
                    start = LocalDateTime.of(2026, 11, 1, 14, 0),
                    end = LocalDateTime.of(2026, 11, 1, 15, 0),
                ),
            ),
        )

        val result = QrEventPayloadCodec.decode(ics)

        assertTrue(result is QrEventDecodeResult.Success)
        assertFalse((result as QrEventDecodeResult.Success).dotCalPayload)
    }

    private fun assertRoundTrip(event: CalendarEvent) {
        val encoded = QrEventPayloadCodec.encode(IcsExporter.export(listOf(event)))
        val decoded = QrEventPayloadCodec.decode(encoded.payload)

        assertFalse(encoded.sharedWithoutDescription)
        assertTrue(decoded is QrEventDecodeResult.Success)
        val parsed = IcsParser.parse((decoded as QrEventDecodeResult.Success).icsText).single()
        assertEquals(event.id, parsed.uid)
        assertEquals(event.title, parsed.title)
        assertEquals(event.description, parsed.description)
        assertEquals(event.location, parsed.location)
        assertEquals(event.startTimeMs, parsed.startTimeMs)
        assertEquals(event.endTimeMs, parsed.endTimeMs)
        assertEquals(event.isAllDay == 1, parsed.isAllDay)
        assertEquals(event.rrule, parsed.rrule)
    }

    private fun event(
        title: String,
        start: LocalDateTime,
        end: LocalDateTime,
        description: String = "",
        location: String = "",
        isAllDay: Boolean = false,
        rrule: String? = null,
    ): CalendarEvent {
        val now = System.currentTimeMillis()
        return CalendarEvent(
            id = "event-${title.lowercase().replace(' ', '-')}",
            accountId = "local",
            title = title,
            description = description,
            location = location,
            startTimeMs = start.atZone(zone).toInstant().toEpochMilli(),
            endTimeMs = end.atZone(zone).toInstant().toEpochMilli(),
            timeZone = zone.id,
            isAllDay = if (isAllDay) 1 else 0,
            colorHex = null,
            rrule = rrule,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = now,
            updatedAtMs = now,
        )
    }

    private fun noisyText(length: Int): String = buildString(length) {
        var value = 0x12345678
        repeat(length) {
            value = value * 1103515245 + 12345
            append(('!'..'~').elementAt((value ushr 16).mod(94)))
        }
    }
}
