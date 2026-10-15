package com.dotfield.dotcal.data.qr

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.ics.IcsExporter
import com.dotfield.dotcal.data.ics.IcsParser
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.client.result.CalendarParsedResult
import com.google.zxing.client.result.ResultParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Base64
import java.util.zip.Deflater

class QrEventPayloadCodecTest {

    private val zone = ZoneId.of("Asia/Kolkata")

    @Test
    fun generatedPayloadIsStandardIcalendarText() {
        val ics = IcsExporter.export(
            listOf(
                event(
                    title = "Open calendar",
                    start = LocalDateTime.of(2026, 11, 1, 14, 0),
                    end = LocalDateTime.of(2026, 11, 1, 15, 0),
                ),
            ),
        )

        val encoded = QrEventPayloadCodec.encode(ics)

        assertEquals(ics, encoded.payload)
        assertTrue(encoded.payload.startsWith("BEGIN:VCALENDAR\r\n"))
        assertTrue(encoded.payload.contains("BEGIN:VEVENT\r\n"))
    }

    @Test
    fun generatedPayloadIsRecognizedByStandardQrCalendarParser() {
        val ics = IcsExporter.export(
            listOf(
                event(
                    title = "External calendar",
                    start = LocalDateTime.of(2026, 11, 1, 14, 0),
                    end = LocalDateTime.of(2026, 11, 1, 15, 0),
                ),
            ),
        )

        val encoded = QrEventPayloadCodec.encode(ics)
        val parsed = ResultParser.parseResult(
            Result(encoded.payload, null, emptyArray<ResultPoint>(), BarcodeFormat.QR_CODE),
        )

        assertTrue(parsed is CalendarParsedResult)
        assertEquals("External calendar", (parsed as CalendarParsedResult).summary)
    }

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
        assertTrue(QrEventPayloadCodec.decode("https://example.com") is QrEventDecodeResult.NotCalendar)
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

    @Test
    fun bareVeventTextIsAcceptedForInterop() {
        val ics = IcsExporter.export(
            listOf(
                event(
                    title = "External QR",
                    start = LocalDateTime.of(2026, 11, 1, 14, 0),
                    end = LocalDateTime.of(2026, 11, 1, 15, 0),
                ),
            ),
        )
        val vevent = ics.substringAfter("BEGIN:VEVENT").substringBefore("END:VEVENT")
            .let { "BEGIN:VEVENT$it" + "END:VEVENT\r\n" }

        val result = QrEventPayloadCodec.decode(vevent)

        assertTrue(result is QrEventDecodeResult.Success)
        assertFalse((result as QrEventDecodeResult.Success).dotCalPayload)
        assertEquals("External QR", IcsParser.parse(result.icsText).single().title)
    }

    @Test
    fun legacyDotCalPayloadStillDecodes() {
        val ics = IcsExporter.export(
            listOf(
                event(
                    title = "Legacy QR",
                    start = LocalDateTime.of(2026, 11, 1, 14, 0),
                    end = LocalDateTime.of(2026, 11, 1, 15, 0),
                ),
            ),
        )

        val result = QrEventPayloadCodec.decode(legacyPayload(ics))

        assertTrue(result is QrEventDecodeResult.Success)
        assertTrue((result as QrEventDecodeResult.Success).dotCalPayload)
        assertEquals("Legacy QR", IcsParser.parse(result.icsText).single().title)
    }

    @Test
    fun dotCalPayloadKeepsFourteenEventShiftPlanCompact() {
        val events = (1..14).map { day ->
            event(
                title = "Night shift $day",
                start = LocalDateTime.of(2026, 11, day, 22, 30),
                end = LocalDateTime.of(2026, 11, day + 1, 6, 15),
            )
        }

        val encoded = QrEventPayloadCodec.encodeDotCal(IcsExporter.export(events))
        val decoded = QrEventPayloadCodec.decode(encoded.payload)

        assertTrue(encoded.payload.startsWith(QrEventPayloadCodec.PREFIX))
        assertTrue(encoded.payload.toByteArray(Charsets.UTF_8).size <= 1_500)
        assertTrue(decoded is QrEventDecodeResult.Success)
        assertTrue((decoded as QrEventDecodeResult.Success).dotCalPayload)
        assertEquals(14, IcsParser.parse(decoded.icsText).size)
    }

    @Test
    fun calendarTextWithBomAndLeadingWhitespaceIsAccepted() {
        val ics = IcsExporter.export(
            listOf(
                event(
                    title = "BOM event",
                    start = LocalDateTime.of(2026, 11, 1, 14, 0),
                    end = LocalDateTime.of(2026, 11, 1, 15, 0),
                ),
            ),
        )

        val result = QrEventPayloadCodec.decode("\uFEFF\n  $ics")

        assertTrue(result is QrEventDecodeResult.Success)
        assertEquals("BOM event", IcsParser.parse((result as QrEventDecodeResult.Success).icsText).single().title)
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

    private fun legacyPayload(ics: String): String {
        val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true)
        return try {
            deflater.setInput(ics.toByteArray(Charsets.UTF_8))
            deflater.finish()
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(1_024)
            while (!deflater.finished()) {
                output.write(buffer, 0, deflater.deflate(buffer))
            }
            QrEventPayloadCodec.PREFIX + Base64.getEncoder().encodeToString(output.toByteArray())
        } finally {
            deflater.end()
        }
    }
}
