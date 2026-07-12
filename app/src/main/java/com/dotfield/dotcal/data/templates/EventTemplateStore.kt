package com.dotfield.dotcal.data.templates

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * A reusable event/task preset. Date-agnostic: only the time-of-day, duration, reminder,
 * recurrence, and calendar are stored, so a template can be applied to any day.
 *
 * [startMinuteOfDay] null means all-day (events) or no specific time (tasks).
 * [durationMinutes] only matters for timed events.
 */
data class EventTemplate(
    val id: String,
    val name: String,
    val isTask: Boolean,
    val title: String,
    val description: String,
    val location: String,
    val accountId: String?,
    val isAllDay: Boolean,
    val startMinuteOfDay: Int?,
    val durationMinutes: Int,
    val reminderMinutes: Int?,
    val rrule: String?,
    val createdAtMs: Long,
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

/**
 * File-based store for event/task templates. Deliberately NOT a Room table — the 5-table
 * schema is locked. Each template is written as a single JSON file under
 * filesDir/event_templates/{id}.json (same approach as [com.dotfield.dotcal.data.trash.RecentlyDeletedStore]).
 *
 * All writes are best-effort and never throw. Unreadable files are pruned on [list].
 */
class EventTemplateStore(context: Context) {

    private val dir: File = File(context.applicationContext.filesDir, DIR_NAME)

    /** Persist a template (insert or overwrite by id). Never throws. */
    fun save(template: EventTemplate) {
        runCatching {
            if (!dir.exists()) dir.mkdirs()
            fileFor(template.id).writeText(encode(template).toString())
        }
    }

    /** All templates, newest first. Prunes unreadable files as a side effect. */
    fun list(): List<EventTemplate> {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(EXT) } ?: return emptyList()
        val result = ArrayList<EventTemplate>(files.size)
        for (file in files) {
            val template = runCatching { decode(file.readText()) }.getOrNull()
            if (template == null) {
                runCatching { file.delete() }
                continue
            }
            result.add(template)
        }
        return result.sortedByDescending { it.createdAtMs }
    }

    /** Drop a single template. Never throws. */
    fun remove(id: String) {
        runCatching { fileFor(id).delete() }
    }

    private fun fileFor(id: String): File = File(dir, safeName(id) + EXT)

    private fun encode(t: EventTemplate): JSONObject = JSONObject()
        .put("id", t.id)
        .put("name", t.name)
        .put("isTask", t.isTask)
        .put("title", t.title)
        .put("description", t.description)
        .put("location", t.location)
        .put("accountId", t.accountId ?: JSONObject.NULL)
        .put("isAllDay", t.isAllDay)
        .put("startMinuteOfDay", t.startMinuteOfDay ?: JSONObject.NULL)
        .put("durationMinutes", t.durationMinutes)
        .put("reminderMinutes", t.reminderMinutes ?: JSONObject.NULL)
        .put("rrule", t.rrule ?: JSONObject.NULL)
        .put("createdAtMs", t.createdAtMs)

    private fun decode(text: String): EventTemplate {
        val o = JSONObject(text)
        return EventTemplate(
            id = o.getString("id"),
            name = o.optString("name", o.optString("title", "Template")),
            isTask = o.optBoolean("isTask", false),
            title = o.optString("title", ""),
            description = o.optString("description", ""),
            location = o.optString("location", ""),
            accountId = o.optNullableString("accountId"),
            isAllDay = o.optBoolean("isAllDay", false),
            startMinuteOfDay = if (o.isNull("startMinuteOfDay")) null else o.optInt("startMinuteOfDay"),
            durationMinutes = o.optInt("durationMinutes", 60),
            reminderMinutes = if (o.isNull("reminderMinutes")) null else o.optInt("reminderMinutes"),
            rrule = o.optNullableString("rrule"),
            createdAtMs = o.optLong("createdAtMs", 0L),
        )
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    private fun safeName(id: String): String = id.replace(Regex("[^A-Za-z0-9_.-]"), "_")

    companion object {
        private const val DIR_NAME = "event_templates"
        private const val EXT = ".json"
    }
}
