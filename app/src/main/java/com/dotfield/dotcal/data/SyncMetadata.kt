package com.dotfield.dotcal.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_metadata",
    foreignKeys = [
        ForeignKey(
            entity = CalendarAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["accountId"], unique = true)],
)
data class SyncMetadata(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: String,
    val lastSyncMs: Long = 0,
    val lastSyncStatus: String = "NEVER",
    val errorMessage: String?,
    val eventsInserted: Int = 0,
    val eventsUpdated: Int = 0,
    val eventsDeleted: Int = 0,
)
