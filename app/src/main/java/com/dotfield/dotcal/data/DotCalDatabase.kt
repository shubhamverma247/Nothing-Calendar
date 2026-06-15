package com.dotfield.dotcal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CalendarAccount::class,
        CalendarEvent::class,
        EventReminder::class,
        SyncMetadata::class,
        DeletedEventLog::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class DotCalDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao

    companion object {
        fun create(context: Context): DotCalDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                DotCalDatabase::class.java,
                "dotcal.db",
            ).build()
        }
    }
}
