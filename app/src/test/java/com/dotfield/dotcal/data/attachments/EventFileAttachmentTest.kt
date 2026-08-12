package com.dotfield.dotcal.data.attachments

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

class EventFileAttachmentTest {
    @Test
    fun attachmentMetadataRoundTripsJson() {
        val attachments = listOf(
            EventFileAttachment(
                id = "file-1",
                displayName = "ticket.pdf",
                mimeType = "application/pdf",
                sizeBytes = 12_345L,
                localPath = "/tmp/ticket.pdf",
                addedAtMs = 99L,
            ),
        )

        assertEquals(attachments, parseEventFileAttachments(attachments.encodeEventFileAttachments()))
    }

    @Test
    fun invalidAttachmentJsonReturnsEmptyList() {
        assertTrue(parseEventFileAttachments("not json").isEmpty())
    }

    @Test
    fun attachmentPathsStayInsideEventFilesRootForHostileIds() {
        val filesDir = File(createTempDirectory().pathString)

        val output = eventAttachmentFile(filesDir, "../../datastore", "../evil")
        val eventDir = eventAttachmentDirectory(filesDir, "../../datastore")
        val root = File(filesDir, "event_files").canonicalFile

        assertTrue(output.path.startsWith(root.path + File.separator))
        assertTrue(eventDir.path.startsWith(root.path + File.separator))
        assertEquals("pdf", output.extension)
    }
}
