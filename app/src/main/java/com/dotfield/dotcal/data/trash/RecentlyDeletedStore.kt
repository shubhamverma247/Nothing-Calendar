package com.dotfield.dotcal.data.trash

import android.content.Context
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * One deleted event/task, snapshotted to disk so it can be restored.
 * Holds the full [CalendarEvent] row plus its reminders and the deletion time.
 */
data class DeletedSnapshot(
    val event: CalendarEvent,
    val reminders: List<EventReminder>,
    val deletedAtMs: Long,
)

/**
 * File-based "Recently Deleted" trash. Deliberately NOT a Room table — the 5-table
 * schema is locked, and [com.dotfield.dotcal.data.DeletedEventLog] only holds sync
 * tombstones (googleEventId), not enough to rebuild an event. Each deleted event is
 * written as a single JSON file under filesDir/recently_deleted/{eventId}.json.
 *
 * All writes are best-effort: a snapshot failure must never block the delete itself.
 * Entries older than [RETENTION_MS] (30 days) are pruned on every [list].
 */
class RecentlyDeletedStore(context: Context) {

    private val dir: File = File(context.applicationContext.filesDir, DIR_NAME)

    /** Snapshot an event and its reminders before it is hard-deleted. Never throws. */
    fun save(event: CalendarEvent, reminders: List<EventReminder>, deletedAtMs: Long) {
        runCatching {
            if (!dir.exists()) dir.mkdirs()
            val json = encode(event, reminders, deletedAtMs)
            fileFor(event.id).writeText(json.toString())
        }
    }

    /** All non-expired snapshots, newest deletion first. Prunes expired files as a side effect. */
    fun list(nowMs: Long): List<DeletedSnapshot> {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(EXT) } ?: return emptyList()
        val result = ArrayList<DeletedSnapshot>(files.size)
        for (file in files) {
            val snapshot = runCatching { decode(file.readText()) }.getOrNull()
            if (snapshot == null) {
                runCatching { file.delete() }
                continue
            }
            if (nowMs - snapshot.deletedAtMs >= RETENTION_MS) {
                runCatching { file.delete() }
                continue
            }
            result.add(snapshot)
        }
        return result.sortedByDescending { it.deletedAtMs }
    }

    /** Read a single snapshot by event id, or null if missing/unreadable. */
    fun get(eventId: String): DeletedSnapshot? {
        val file = fileFor(eventId)
        if (!file.exists()) return null
        return runCatching { decode(file.readText()) }.getOrNull()
    }

    /** Drop a single snapshot (after restore or permanent delete). Never throws. */
    fun remove(eventId: String) {
        runCatching { fileFor(eventId).delete() }
    }

    /** Empty the entire trash. Never throws. */
    fun clear() {
        runCatching { dir.listFiles()?.forEach { it.delete() } }
    }

    private fun fileFor(eventId: String): File = File(dir, safeName(eventId) + EXT)

    private fun encode(event: CalendarEvent, reminders: List<EventReminder>, deletedAtMs: Long): JSONObject {
        val e = JSONObject()
            .put("id", event.id)
            .put("accountId", event.accountId)
            .put("title", event.title)
            .put("description", event.description)
            .put("location", event.location)
            .put("startTimeMs", event.startTimeMs)
            .put("endTimeMs", event.endTimeMs)
            .put("timeZone", event.timeZone)
            .put("isAllDay", event.isAllDay)
            .put("colorHex", event.colorHex ?: JSONObject.NULL)
            .put("rrule", event.rrule ?: JSONObject.NULL)
            .put("exceptionDates", event.exceptionDates)
            .put("source", event.source)
            .put("googleEventId", event.googleEventId ?: JSONObject.NULL)
            .put("googleCalendarId", event.googleCalendarId ?: JSONObject.NULL)
            .put("syncVersion", event.syncVersion)
            .put("isTask", event.isTask)
            .put("isCompleted", event.isCompleted)
            .put("completedAtMs", event.completedAtMs ?: JSONObject.NULL)
            .put("imageUris", event.imageUris)
            .put("voiceNotePath", event.voiceNotePath ?: JSONObject.NULL)
            .put("createdAtMs", event.createdAtMs)
            .put("updatedAtMs", event.updatedAtMs)
        val r = JSONArray()
        for (reminder in reminders) {
            r.put(
                JSONObject()
                    .put("minutesBefore", reminder.minutesBefore)
                    .put("triggerAtMs", reminder.triggerAtMs)
                    .put("alarmRequestCode", reminder.alarmRequestCode)
                    .put("isDelivered", reminder.isDelivered),
            )
        }
        return JSONObject()
            .put("deletedAtMs", deletedAtMs)
            .put("event", e)
            .put("reminders", r)
    }

    private fun decode(text: String): DeletedSnapshot {
        val root = JSONObject(text)
        val e = root.getJSONObject("event")
        val event = CalendarEvent(
            id = e.getString("id"),
            accountId = e.getString("accountId"),
            title = e.getString("title"),
            description = e.optString("description", ""),
            location = e.optString("location", ""),
            startTimeMs = e.getLong("startTimeMs"),
            endTimeMs = e.getLong("endTimeMs"),
            timeZone = e.getString("timeZone"),
            isAllDay = e.getInt("isAllDay"),
            colorHex = e.optNullableString("colorHex"),
            rrule = e.optNullableString("rrule"),
            exceptionDates = e.optString("exceptionDates", "[]"),
            source = e.getString("source"),
            googleEventId = e.optNullableString("googleEventId"),
            googleCalendarId = e.optNullableString("googleCalendarId"),
            syncVersion = e.optInt("syncVersion", 0),
            isTask = e.optInt("isTask", 0),
            isCompleted = e.optInt("isCompleted", 0),
            completedAtMs = if (e.isNull("completedAtMs")) null else e.getLong("completedAtMs"),
            imageUris = e.optString("imageUris", "[]"),
            voiceNotePath = e.optNullableString("voiceNotePath"),
            createdAtMs = e.getLong("createdAtMs"),
            updatedAtMs = e.getLong("updatedAtMs"),
        )
        val remindersJson = root.optJSONArray("reminders") ?: JSONArray()
        val reminders = ArrayList<EventReminder>(remindersJson.length())
        for (i in 0 until remindersJson.length()) {
            val rm = remindersJson.getJSONObject(i)
            reminders.add(
                EventReminder(
                    // id = 0 so Room auto-generates a fresh PK on restore.
                    eventId = event.id,
                    minutesBefore = rm.getInt("minutesBefore"),
                    triggerAtMs = rm.getLong("triggerAtMs"),
                    alarmRequestCode = rm.getInt("alarmRequestCode"),
                    isDelivered = rm.optInt("isDelivered", 0),
                ),
            )
        }
        return DeletedSnapshot(
            event = event,
            reminders = reminders,
            deletedAtMs = root.getLong("deletedAtMs"),
        )
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    /** Keep the filename filesystem-safe; event ids are UUIDs but device rows can vary. */
    private fun safeName(eventId: String): String =
        eventId.replace(Regex("[^A-Za-z0-9_.-]"), "_")

    companion object {
        private const val DIR_NAME = "recently_deleted"
        private const val EXT = ".json"

        /** 30-day trash window. */
        const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000
    }
}
