package com.dotfield.dotcal.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_accounts",
    indices = [Index(value = ["accountType"]), Index(value = ["isVisible"])],
)
data class CalendarAccount(
    @PrimaryKey val id: String,
    val accountName: String,
    val displayName: String,
    val accountType: String,
    val color: String,
    val isVisible: Int,
    val isPrimary: Int,
    val sortOrder: Int,
)
