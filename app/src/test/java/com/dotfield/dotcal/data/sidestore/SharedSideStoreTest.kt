package com.dotfield.dotcal.data.sidestore

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class SharedSideStoreTest {
    @Test
    fun roundTripsNamespaceValues() = runBlocking {
        val file = File.createTempFile("dotcal-side-store", ".json").apply { deleteOnExit() }
        val store = SharedSideStore(file)

        store.write("punchcard", "2026-07-01", "true")
        store.write("countdown_pins", "event-1", "{\"active\":true}")

        assertEquals("true", store.read("punchcard", "2026-07-01"))
        assertEquals(mapOf("event-1" to "{\"active\":true}"), store.readNamespace("countdown_pins"))
    }

    @Test
    fun removeDeletesOnlyRequestedKey() = runBlocking {
        val file = File.createTempFile("dotcal-side-store", ".json").apply { deleteOnExit() }
        val store = SharedSideStore(file)

        store.write("punchcard", "2026-07-01", "true")
        store.write("punchcard", "2026-07-02", "true")
        store.remove("punchcard", "2026-07-01")

        assertNull(store.read("punchcard", "2026-07-01"))
        assertEquals("true", store.read("punchcard", "2026-07-02"))
    }
}
