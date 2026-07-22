package com.dotfield.dotcal.data.sidestore

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class SharedSideStoreTest {
    @Test
    fun writeReadAndRemoveGhostNamespaceEntry() = runBlocking {
        val file = File.createTempFile("dotcal-side-store", ".json")
        file.deleteOnExit()
        val store = SharedSideStore(file)

        store.write("ghost_flags", "event-1", "1")

        assertEquals("1", store.read("ghost_flags", "event-1"))
        assertEquals(mapOf("event-1" to "1"), store.readNamespace("ghost_flags"))

        store.remove("ghost_flags", "event-1")

        assertNull(store.read("ghost_flags", "event-1"))
        assertEquals(emptyMap<String, String>(), store.readNamespace("ghost_flags"))
    }

    @Test
    fun ghostFlagSurvivesStoreReload() = runBlocking {
        val file = File.createTempFile("dotcal-side-store-reload", ".json")
        file.deleteOnExit()

        SharedSideStore(file).write("ghost_flags", "event-2", "1")

        assertEquals("1", SharedSideStore(file).read("ghost_flags", "event-2"))
    }
}
