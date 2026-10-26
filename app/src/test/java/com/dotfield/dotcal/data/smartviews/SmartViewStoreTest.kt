package com.dotfield.dotcal.data.smartviews

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SmartViewStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun saveListAndRemoveRoundTrip() {
        val store = SmartViewStore(temporaryFolder.newFolder("smart_views"))
        val older = view("older", 1L)
        val newer = view("newer", 2L).copy(
            query = "dentist",
            type = SmartViewType.Events,
            dateRange = SmartViewDateRange.Upcoming,
            accountId = "personal",
        )

        store.save(older)
        store.save(newer)

        assertEquals(listOf(newer, older), store.list())

        store.remove("newer")

        assertEquals(listOf(older), store.list())
    }

    @Test
    fun overwritingIdDoesNotCreateDuplicate() {
        val store = SmartViewStore(temporaryFolder.newFolder("smart_views"))

        store.save(view("same", 1L))
        store.save(view("same", 2L).copy(name = "Updated"))

        assertEquals(listOf("Updated"), store.list().map { it.name })
    }

    @Test
    fun saveReturnsFailureWhenStorageDirectoryCannotBeCreated() {
        val fileInsteadOfDirectory = temporaryFolder.newFile("not-a-directory")
        val store = SmartViewStore(fileInsteadOfDirectory)

        assertTrue(store.save(view("blocked", 1L)).isFailure)
    }

    @Test
    fun malformedViewFileIsIgnoredWithoutBeingDeleted() {
        val directory = temporaryFolder.newFolder("smart_views")
        val malformed = directory.resolve("broken.json").apply { writeText("not json") }
        val store = SmartViewStore(directory)

        assertEquals(emptyList<SmartView>(), store.list())
        assertTrue(malformed.exists())
    }

    @Test
    fun removeReturnsFailureWhenStoredPathCannotBeDeleted() {
        val directory = temporaryFolder.newFolder("smart_views")
        directory.resolve("blocked.json").mkdirs()
        directory.resolve("blocked.json/child").writeText("keeps directory non-empty")
        val store = SmartViewStore(directory)

        assertTrue(store.remove("blocked").isFailure)
    }

    private fun view(id: String, createdAtMs: Long) = SmartView(
        id = id,
        name = id,
        type = SmartViewType.Tasks,
        dateRange = SmartViewDateRange.AnyTime,
        taskStatus = SmartViewTaskStatus.Pending,
        createdAtMs = createdAtMs,
    )
}
