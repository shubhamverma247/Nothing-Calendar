package com.dotfield.dotcal.data.smartviews

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.json.JSONObject

class SmartViewStore(private val directory: File) {
    constructor(context: Context) : this(File(context.applicationContext.filesDir, DIRECTORY_NAME))

    fun save(view: SmartView): Result<Unit> = runCatching {
        check(directory.isDirectory || directory.mkdirs()) { "Could not create Smart View storage" }
        val target = fileFor(view.id)
        val temporary = File.createTempFile(target.name, ".tmp", directory)
        try {
            FileOutputStream(temporary).use { output ->
                output.write(encode(view).toString().toByteArray(Charsets.UTF_8))
                output.fd.sync()
            }
            Files.move(
                temporary.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
            Unit
        } finally {
            temporary.delete()
        }
    }

    fun list(): List<SmartView> {
        val files = directory.listFiles { file -> file.isFile && file.name.endsWith(EXTENSION) }
            ?: return emptyList()
        return files.mapNotNull { file ->
            runCatching { decode(file.readText()) }.getOrNull()
        }.sortedByDescending { it.createdAtMs }
    }

    fun remove(id: String): Result<Unit> = runCatching {
        val target = fileFor(id)
        check(!target.exists() || target.delete()) { "Could not delete Smart View" }
    }

    private fun fileFor(id: String) = File(directory, id.replace(UNSAFE_FILE_CHARS, "_") + EXTENSION)

    private fun encode(view: SmartView) = JSONObject()
        .put("id", view.id)
        .put("name", view.name)
        .put("query", view.query)
        .put("type", view.type.name)
        .put("dateRange", view.dateRange.name)
        .put("accountId", view.accountId)
        .put("taskStatus", view.taskStatus.name)
        .put("createdAtMs", view.createdAtMs)

    private fun decode(text: String): SmartView {
        val json = JSONObject(text)
        return SmartView(
            id = json.getString("id"),
            name = json.getString("name"),
            query = json.optString("query"),
            type = json.enumOrDefault("type", SmartViewType.All),
            dateRange = json.enumOrDefault("dateRange", SmartViewDateRange.AnyTime),
            accountId = json.optString("accountId").takeIf { it.isNotBlank() && it != "null" },
            taskStatus = json.enumOrDefault("taskStatus", SmartViewTaskStatus.Any),
            createdAtMs = json.optLong("createdAtMs"),
        )
    }

    private inline fun <reified T : Enum<T>> JSONObject.enumOrDefault(key: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == optString(key) } ?: default

    companion object {
        private const val DIRECTORY_NAME = "smart_views"
        private const val EXTENSION = ".json"
        private val UNSAFE_FILE_CHARS = Regex("[^A-Za-z0-9_.-]")
    }
}
