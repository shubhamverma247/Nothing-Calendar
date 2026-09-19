package com.dotfield.dotcal.data.readiness

import com.dotfield.dotcal.data.sidestore.SharedSideStore
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventReadinessStoreTest {
    @Test
    fun checklistLifecyclePersistsInEventOrder() = runBlocking {
        val file = temporaryStoreFile("lifecycle")
        val ids = ArrayDeque(listOf("item-1", "item-2"))
        val store = EventReadinessStore(SharedSideStore(file)) { ids.removeFirst() }

        store.add("event-1", "  Bring passport  ")
        store.add("event-1", "Print confirmation")
        store.setCompleted("event-1", "item-1", completed = true)
        store.rename("event-1", "item-2", "Print two confirmations")

        assertEquals(
            listOf(
                EventReadinessItem("item-1", "Bring passport", isCompleted = true),
                EventReadinessItem("item-2", "Print two confirmations", isCompleted = false),
            ),
            EventReadinessStore(SharedSideStore(file)).read("event-1"),
        )
    }

    @Test
    fun removingLastItemClearsStoredChecklist() = runBlocking {
        val file = temporaryStoreFile("remove")
        val sideStore = SharedSideStore(file)
        val store = EventReadinessStore(sideStore) { "item-1" }

        store.add("event-1", "Bring passport")
        store.remove("event-1", "item-1")

        assertTrue(store.read("event-1").isEmpty())
        assertFalse(sideStore.readNamespace(EventReadinessStore.Namespace).containsKey("event-1"))
    }

    @Test
    fun movingEventIdentityPreservesChecklistAndClearsOldKey() = runBlocking {
        val file = temporaryStoreFile("move")
        val store = EventReadinessStore(SharedSideStore(file)) { "item-1" }
        store.add("local-event", "Bring passport")

        store.move("local-event", "provider-event")

        assertTrue(store.read("local-event").isEmpty())
        assertEquals(
            listOf(EventReadinessItem("item-1", "Bring passport", isCompleted = false)),
            EventReadinessStore(SharedSideStore(file)).read("provider-event"),
        )
    }

    @Test
    fun malformedRowsDoNotDiscardValidChecklistItems() {
        val encoded = listOf(
            "item-1\tBring+passport\ttrue",
            "broken-row",
            "%ZZ\tInvalid+escape\tfalse",
            "item-2\tPrint+confirmation\tfalse",
        ).joinToString("\n")

        assertEquals(
            listOf(
                EventReadinessItem("item-1", "Bring passport", isCompleted = true),
                EventReadinessItem("item-2", "Print confirmation", isCompleted = false),
            ),
            parseEventReadinessItems(encoded),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankChecklistItemIsRejected() = runBlocking {
        val store = EventReadinessStore(SharedSideStore(temporaryStoreFile("blank"))) { "item-1" }

        store.add("event-1", "   ")
        Unit
    }

    private fun temporaryStoreFile(suffix: String): File {
        return File.createTempFile("dotcal-readiness-$suffix", ".json").apply { deleteOnExit() }
    }
}
