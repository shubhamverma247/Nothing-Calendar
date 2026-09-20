package com.dotfield.dotcal.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import java.io.FileNotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CalendarPreferencesRecoveryTest {
    @Test
    fun recognizesMissingDataStoreFile() {
        assertTrue(FileNotFoundException("calendar_preferences.preferences_pb").isMissingDataStoreFile())
    }

    @Test
    fun ignoresUnrelatedDataStoreFailure() {
        assertFalse(IllegalStateException("corrupt preferences").isMissingDataStoreFile())
    }

    @Test
    fun missingDataStoreReadEmitsEmptyPreferencesAfterRetry() = runBlocking {
        val delegate = ThrowingPreferencesDataStore(FileNotFoundException("calendar_preferences.preferences_pb"))

        assertEquals(emptyPreferences(), ResilientPreferencesDataStore(delegate).data.first())
    }

    @Test
    fun unrelatedDataStoreFailureStillPropagates() = runBlocking {
        val failure = IllegalStateException("corrupt preferences")
        val delegate = ThrowingPreferencesDataStore(failure)

        try {
            ResilientPreferencesDataStore(delegate).data.first()
            fail("Unrelated DataStore failures must not be swallowed")
        } catch (actual: IllegalStateException) {
            assertEquals(failure.message, actual.message)
        }
    }

    private class ThrowingPreferencesDataStore(
        private val failure: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw failure }

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(emptyPreferences())
    }
}
