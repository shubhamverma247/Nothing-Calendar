package com.dotfield.dotcal.data.qr

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

data class EncodedQrEventPayload(
    val payload: String,
    val sharedWithoutDescription: Boolean,
)

sealed interface QrEventDecodeResult {
    data class Success(
        val icsText: String,
        val dotCalPayload: Boolean,
    ) : QrEventDecodeResult

    data object NotCalendar : QrEventDecodeResult
    data object UnsupportedVersion : QrEventDecodeResult
    data object Malformed : QrEventDecodeResult
}

object QrEventPayloadCodec {
    const val PREFIX = "DOTCAL1:"
    private const val PREFIX_ROOT = "DOTCAL"
    private const val RELIABLE_QR_PAYLOAD_BYTES = 1_500
    private const val MAX_INFLATED_BYTES = 256 * 1024

    fun encode(fullIcs: String, descriptionFreeIcs: String = fullIcs): EncodedQrEventPayload {
        require(fullIcs.isNotBlank()) { "ICS payload cannot be blank" }
        if (fullIcs.toByteArray(Charsets.UTF_8).size <= RELIABLE_QR_PAYLOAD_BYTES) {
            return EncodedQrEventPayload(fullIcs, sharedWithoutDescription = false)
        }

        require(descriptionFreeIcs.isNotBlank()) { "Description-free ICS payload cannot be blank" }
        require(descriptionFreeIcs.toByteArray(Charsets.UTF_8).size <= RELIABLE_QR_PAYLOAD_BYTES) {
            "Event is too large to encode as a reliable QR code"
        }
        return EncodedQrEventPayload(descriptionFreeIcs, sharedWithoutDescription = true)
    }

    fun encodeDotCal(icsText: String): EncodedQrEventPayload {
        require(icsText.isNotBlank()) { "ICS payload cannot be blank" }
        val payload = PREFIX + Base64.getEncoder().encodeToString(deflate(icsText.toByteArray(Charsets.UTF_8)))
        require(payload.toByteArray(Charsets.UTF_8).size <= RELIABLE_QR_PAYLOAD_BYTES) {
            "Shift plan is too large to encode as a reliable QR code"
        }
        return EncodedQrEventPayload(payload, sharedWithoutDescription = false)
    }

    fun decode(rawPayload: String): QrEventDecodeResult {
        val payload = rawPayload.removePrefix("\uFEFF").trim()
        if (payload.isStandardCalendarText()) {
            return QrEventDecodeResult.Success(payload, dotCalPayload = false)
        }
        if (!payload.startsWith(PREFIX)) {
            return if (payload.startsWith(PREFIX_ROOT)) {
                QrEventDecodeResult.UnsupportedVersion
            } else {
                QrEventDecodeResult.NotCalendar
            }
        }

        val encoded = payload.removePrefix(PREFIX)
        if (encoded.isBlank()) return QrEventDecodeResult.Malformed
        return try {
            val compressed = Base64.getDecoder().decode(encoded)
            val inflated = inflate(compressed)
            val text = inflated.toString(Charsets.UTF_8)
            if (!text.trimStart().startsWith("BEGIN:VCALENDAR", ignoreCase = true)) {
                QrEventDecodeResult.Malformed
            } else {
                QrEventDecodeResult.Success(text, dotCalPayload = true)
            }
        } catch (_: IllegalArgumentException) {
            QrEventDecodeResult.Malformed
        } catch (_: DataFormatException) {
            QrEventDecodeResult.Malformed
        }
    }

    private fun String.isStandardCalendarText(): Boolean =
        startsWith("BEGIN:VCALENDAR", ignoreCase = true) ||
            startsWith("BEGIN:VEVENT", ignoreCase = true)

    private fun deflate(input: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true)
        return try {
            deflater.setInput(input)
            deflater.finish()
            val output = ByteArrayOutputStream(input.size.coerceAtMost(4_096))
            val buffer = ByteArray(1_024)
            while (!deflater.finished()) {
                output.write(buffer, 0, deflater.deflate(buffer))
            }
            output.toByteArray()
        } finally {
            deflater.end()
        }
    }

    @Throws(DataFormatException::class)
    private fun inflate(input: ByteArray): ByteArray {
        val inflater = Inflater(true)
        return try {
            inflater.setInput(input)
            val output = ByteArrayOutputStream(input.size.coerceAtLeast(1_024))
            val buffer = ByteArray(1_024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) throw DataFormatException("Incomplete payload")
                } else {
                    if (output.size() + count > MAX_INFLATED_BYTES) throw DataFormatException("Payload too large")
                    output.write(buffer, 0, count)
                }
            }
            output.toByteArray()
        } finally {
            inflater.end()
        }
    }
}
