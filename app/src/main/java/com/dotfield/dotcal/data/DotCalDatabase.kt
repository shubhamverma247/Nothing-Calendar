package com.dotfield.dotcal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

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
        private const val DATABASE_NAME = "dotcal.db"
        private const val BUSY_TIMEOUT_MS = 5_000

        @Volatile
        private var instance: DotCalDatabase? = null

        fun create(context: Context): DotCalDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DotCalDatabase {
            return Room.databaseBuilder(
                context,
                DotCalDatabase::class.java,
                DATABASE_NAME,
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            db.query("PRAGMA busy_timeout=$BUSY_TIMEOUT_MS").use { }
                        }
                    },
                )
                .build()
        }
    }
}
