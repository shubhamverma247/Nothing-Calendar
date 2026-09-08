package com.dotfield.dotcal.data

import android.database.sqlite.SQLiteDatabaseLockedException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseLockRecoveryTest {
    @Test
    fun recognizesNestedDatabaseLockFailure() {
        assertTrue(RuntimeException(SQLiteDatabaseLockedException("database is locked")).hasDatabaseLockedCause())
    }

    @Test
    fun ignoresUnrelatedDatabaseFailure() {
        assertFalse(IllegalStateException("database is corrupt").hasDatabaseLockedCause())
    }

    @Test
    fun lockedListFlowEmitsEmptyListAfterBoundedRetry() = runBlocking {
        val values = flow<List<Int>> {
            throw SQLiteDatabaseLockedException("database is locked")
        }.retryOnDatabaseLocked().first()

        assertTrue(values.isEmpty())
    }
}
