package com.dotfield.dotcal.data.attachments

import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class EventFileAttachment(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val localPath: String,
    val addedAtMs: Long,
)

fun List<EventFileAttachment>.encodeEventFileAttachments(): String {
    return joinToString("\n") { attachment ->
        listOf(
            attachment.id,
            attachment.displayName,
            attachment.mimeType,
            attachment.sizeBytes.toString(),
            attachment.localPath,
            attachment.addedAtMs.toString(),
        ).joinToString("\t") { it.urlEncode() }
    }
}

fun parseEventFileAttachments(value: String): List<EventFileAttachment> {
    return runCatching {
        value.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split('\t').map { it.urlDecode() }
                if (parts.size < 6) return@mapNotNull null
                EventFileAttachment(
                    id = parts[0],
                    displayName = parts[1],
                    mimeType = parts[2].ifBlank { "application/octet-stream" },
                    sizeBytes = parts[3].toLongOrNull() ?: 0L,
                    localPath = parts[4],
                    addedAtMs = parts[5].toLongOrNull() ?: 0L,
                )
            }
            .toList()
    }.getOrDefault(emptyList())
}

fun eventAttachmentFile(filesDir: File, eventId: String, attachmentId: String): File {
    val eventFilesRoot = File(filesDir, EVENT_FILES_DIR).canonicalFile
    val eventDir = File(eventFilesRoot, eventId.stablePathSegment()).canonicalFile
    val output = File(eventDir, "${attachmentId.stablePathSegment()}.pdf").canonicalFile
    require(output.path.startsWith(eventFilesRoot.path + File.separator)) { "INVALID ATTACHMENT PATH" }
    return output
}

fun eventAttachmentDirectory(filesDir: File, eventId: String): File {
    val eventFilesRoot = File(filesDir, EVENT_FILES_DIR).canonicalFile
    val eventDir = File(eventFilesRoot, eventId.stablePathSegment()).canonicalFile
    require(eventDir.path.startsWith(eventFilesRoot.path + File.separator)) { "INVALID ATTACHMENT PATH" }
    return eventDir
}

private fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())

private fun String.urlDecode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8.name())

private fun String.stablePathSegment(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray(StandardCharsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

private const val EVENT_FILES_DIR = "event_files"
