package com.dotfield.dotcal.data.profiles

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * A named snapshot of which calendars should be visible — "Work", "Personal", "Family".
 * Applying a set shows exactly its [accountIds] and hides every other calendar.
 */
data class FocusProfile(
    val id: String,
    val name: String,
    val accountIds: Set<String>,
    val createdAtMs: Long,
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

/**
 * File-based store for Calendar Sets / Focus Profiles. Deliberately NOT a Room table — the
 * 5-table schema is locked. Each set is written as a single JSON file under
 * filesDir/focus_profiles/{id}.json (same approach as
 * [com.dotfield.dotcal.data.templates.EventTemplateStore]).
 *
 * All writes are best-effort and never throw. Unreadable files are pruned on [list].
 */
class FocusProfileStore(context: Context) {

    private val dir: File = File(context.applicationContext.filesDir, DIR_NAME)

    /** Persist a set (insert or overwrite by id). Never throws. */
    fun save(profile: FocusProfile) {
        runCatching {
            if (!dir.exists()) dir.mkdirs()
            fileFor(profile.id).writeText(encode(profile).toString())
        }
    }

    /** All sets, newest first. Prunes unreadable files as a side effect. */
    fun list(): List<FocusProfile> {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(EXT) } ?: return emptyList()
        val result = ArrayList<FocusProfile>(files.size)
        for (file in files) {
            val profile = runCatching { decode(file.readText()) }.getOrNull()
            if (profile == null) {
                runCatching { file.delete() }
                continue
            }
            result.add(profile)
        }
        return result.sortedByDescending { it.createdAtMs }
    }

    /** Drop a single set. Never throws. */
    fun remove(id: String) {
        runCatching { fileFor(id).delete() }
    }

    private fun fileFor(id: String): File = File(dir, safeName(id) + EXT)

    private fun encode(p: FocusProfile): JSONObject {
        val ids = JSONArray()
        p.accountIds.forEach { ids.put(it) }
        return JSONObject()
            .put("id", p.id)
            .put("name", p.name)
            .put("accountIds", ids)
            .put("createdAtMs", p.createdAtMs)
    }

    private fun decode(text: String): FocusProfile {
        val o = JSONObject(text)
        val idsArray = o.optJSONArray("accountIds")
        val ids = LinkedHashSet<String>()
        if (idsArray != null) {
            for (i in 0 until idsArray.length()) ids.add(idsArray.getString(i))
        }
        return FocusProfile(
            id = o.getString("id"),
            name = o.optString("name", "Set"),
            accountIds = ids,
            createdAtMs = o.optLong("createdAtMs", 0L),
        )
    }

    private fun safeName(id: String): String = id.replace(Regex("[^A-Za-z0-9_.-]"), "_")

    companion object {
        private const val DIR_NAME = "focus_profiles"
        private const val EXT = ".json"
    }
}
