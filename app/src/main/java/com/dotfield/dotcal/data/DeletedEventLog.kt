package com.dotfield.dotcal.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deleted_event_log",
    indices = [Index(value = ["googleEventId"], unique = true)],
)
data class DeletedEventLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val googleEventId: String,
    val deletedAtMs: Long,
)
