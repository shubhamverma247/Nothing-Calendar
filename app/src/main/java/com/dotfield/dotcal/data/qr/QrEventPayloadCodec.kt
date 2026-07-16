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

    data object NotDotCal : QrEventDecodeResult
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
        val fullPayload = encodeIcs(fullIcs)
        if (fullPayload.toByteArray(Charsets.UTF_8).size <= RELIABLE_QR_PAYLOAD_BYTES) {
            return EncodedQrEventPayload(fullPayload, sharedWithoutDescription = false)
        }

        val compactPayload = encodeIcs(descriptionFreeIcs)
        require(compactPayload.toByteArray(Charsets.UTF_8).size <= RELIABLE_QR_PAYLOAD_BYTES) {
            "Event is too large to encode as a reliable QR code"
        }
        return EncodedQrEventPayload(compactPayload, sharedWithoutDescription = true)
    }

    fun decode(rawPayload: String): QrEventDecodeResult {
        val payload = rawPayload.trim()
        if (payload.startsWith("BEGIN:VCALENDAR", ignoreCase = true)) {
            return QrEventDecodeResult.Success(payload, dotCalPayload = false)
        }
        if (!payload.startsWith(PREFIX)) {
            return if (payload.startsWith(PREFIX_ROOT)) {
                QrEventDecodeResult.UnsupportedVersion
            } else {
                QrEventDecodeResult.NotDotCal
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

    private fun encodeIcs(icsText: String): String {
        val compressed = deflate(icsText.toByteArray(Charsets.UTF_8))
        return PREFIX + Base64.getEncoder().encodeToString(compressed)
    }

    private fun deflate(input: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true)
        return try {
            deflater.setInput(input)
            deflater.finish()
            val output = ByteArrayOutputStream(input.size.coerceAtMost(4_096))
            val buffer = ByteArray(1_024)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                output.write(buffer, 0, count)
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
