package com.dotfield.dotcal.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

internal const val CALENDAR_ACCESS_NONE = 0
internal const val CALENDAR_ACCESS_FREEBUSY = 100
internal const val CALENDAR_ACCESS_READ = 200
internal const val CALENDAR_ACCESS_CONTRIBUTOR = 500
internal const val CALENDAR_ACCESS_EDITOR = 600
internal const val CALENDAR_ACCESS_OWNER = 700

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
    @ColumnInfo(defaultValue = "$CALENDAR_ACCESS_OWNER")
    val accessLevel: Int = CALENDAR_ACCESS_OWNER,
)

internal fun CalendarAccount.isWritableDestination(): Boolean {
    return accessLevel >= CALENDAR_ACCESS_CONTRIBUTOR
}
