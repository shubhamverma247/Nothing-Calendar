package com.dotfield.dotcal.data.backup

import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import org.json.JSONArray
import org.json.JSONObject

/**
 * A full-fidelity, offline snapshot of the user's calendar: the accounts that own the
 * exported events, the user-owned events/tasks themselves, and their reminders.
 *
 * Deliberately only the 3 user-content tables. `sync_metadata` and `deleted_event_log` are
 * transient sync bookkeeping (regenerated automatically); restoring stale Google tombstones
 * could suppress legitimately re-synced events, so they are never backed up.
 */
data class BackupData(
    val accounts: List<CalendarAccount>,
    val events: List<CalendarEvent>,
    val reminders: List<EventReminder>,
    val createdAtMs: Long,
)

/**
 * Serializes/deserializes [BackupData] to/from a JSON document, using only `org.json`
 * (no new dependency, mirroring [com.dotfield.dotcal.data.trash.RecentlyDeletedStore]).
 * The event field-mapping is intentionally identical to that store so both round-trip the
 * same 23 [CalendarEvent] fields the same way.
 */
object BackupSerializer {

    /** Envelope tag [decode] requires; guards against restoring a foreign JSON file. */
    private const val FORMAT = "dotcal-backup"
    private const val VERSION = 1

    fun encode(data: BackupData): String {
        val accounts = JSONArray()
        for (account in data.accounts) accounts.put(encodeAccount(account))
        val events = JSONArray()
        for (event in data.events) events.put(encodeEvent(event))
        val reminders = JSONArray()
        for (reminder in data.reminders) reminders.put(encodeReminder(reminder))
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("createdAtMs", data.createdAtMs)
            .put("accounts", accounts)
            .put("events", events)
            .put("reminders", reminders)
            .toString()
    }

    /** Throws if the text is not a DotCal backup (missing/unknown `format`) or is unparseable. */
    fun decode(text: String): BackupData {
        val root = JSONObject(text)
        if (root.optString("format") != FORMAT) {
            throw IllegalArgumentException("Not a DotCal backup file")
        }
        val accountsJson = root.optJSONArray("accounts") ?: JSONArray()
        val accounts = ArrayList<CalendarAccount>(accountsJson.length())
        for (i in 0 until accountsJson.length()) accounts.add(decodeAccount(accountsJson.getJSONObject(i)))
        val eventsJson = root.optJSONArray("events") ?: JSONArray()
        val events = ArrayList<CalendarEvent>(eventsJson.length())
        for (i in 0 until eventsJson.length()) events.add(decodeEvent(eventsJson.getJSONObject(i)))
        val remindersJson = root.optJSONArray("reminders") ?: JSONArray()
        val reminders = ArrayList<EventReminder>(remindersJson.length())
        for (i in 0 until remindersJson.length()) reminders.add(decodeReminder(remindersJson.getJSONObject(i)))
        return BackupData(
            accounts = accounts,
            events = events,
            reminders = reminders,
            createdAtMs = root.optLong("createdAtMs", 0L),
        )
    }

    private fun encodeAccount(account: CalendarAccount): JSONObject =
        JSONObject()
            .put("id", account.id)
            .put("accountName", account.accountName)
            .put("displayName", account.displayName)
            .put("accountType", account.accountType)
            .put("color", account.color)
            .put("isVisible", account.isVisible)
            .put("isPrimary", account.isPrimary)
            .put("sortOrder", account.sortOrder)

    private fun decodeAccount(a: JSONObject): CalendarAccount =
        CalendarAccount(
            id = a.getString("id"),
            accountName = a.optString("accountName", ""),
            displayName = a.optString("displayName", ""),
            accountType = a.optString("accountType", "LOCAL"),
            color = a.optString("color", "#FF0000"),
            isVisible = a.optInt("isVisible", 1),
            isPrimary = a.optInt("isPrimary", 0),
            sortOrder = a.optInt("sortOrder", 0),
        )

    private fun encodeEvent(event: CalendarEvent): JSONObject =
        JSONObject()
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

    private fun decodeEvent(e: JSONObject): CalendarEvent =
        CalendarEvent(
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

    private fun encodeReminder(reminder: EventReminder): JSONObject =
        JSONObject()
            .put("eventId", reminder.eventId)
            .put("minutesBefore", reminder.minutesBefore)
            .put("triggerAtMs", reminder.triggerAtMs)
            .put("alarmRequestCode", reminder.alarmRequestCode)
            .put("isDelivered", reminder.isDelivered)

    private fun decodeReminder(r: JSONObject): EventReminder =
        EventReminder(
            // id omitted → 0 so Room auto-generates a fresh PK on restore.
            eventId = r.getString("eventId"),
            minutesBefore = r.getInt("minutesBefore"),
            triggerAtMs = r.getLong("triggerAtMs"),
            alarmRequestCode = r.getInt("alarmRequestCode"),
            isDelivered = r.optInt("isDelivered", 0),
        )

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)
}
