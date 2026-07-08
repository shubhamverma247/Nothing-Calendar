package com.dotfield.dotcal.data.shifts

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class ShiftType(
    val id: String,
    val name: String,
    val colorHex: String,
    val startMinuteOfDay: Int?,
    val durationMinutes: Int?,
    val isAllDay: Boolean,
    val reminderMinutes: Int?,
    val createdAtMs: Long,
) {
    val generatesEvent: Boolean
        get() = isAllDay || (startMinuteOfDay != null && durationMinutes != null && durationMinutes > 0)

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

data class ShiftPattern(
    val id: String,
    val name: String,
    val cycleShiftTypeIds: List<String>,
    val cycleStartDate: LocalDate,
    val createdAtMs: Long,
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

data class GeneratedShiftOccurrence(
    val date: LocalDate,
    val shiftType: ShiftType,
)

data class ShiftGenerationRecord(
    val id: String,
    val patternId: String,
    val generatedAtMs: Long,
    val eventIds: List<String>,
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

data class ShiftApplyResult(
    val generatedCount: Int,
    val replacedCount: Int = 0,
)

fun expandShiftPattern(
    pattern: ShiftPattern,
    shiftTypes: Map<String, ShiftType>,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
): List<GeneratedShiftOccurrence> {
    if (rangeEnd.isBefore(rangeStart) || pattern.cycleShiftTypeIds.isEmpty()) return emptyList()
    val cycleLength = pattern.cycleShiftTypeIds.size
    val result = ArrayList<GeneratedShiftOccurrence>()
    var date = rangeStart
    while (!date.isAfter(rangeEnd)) {
        val days = ChronoUnit.DAYS.between(pattern.cycleStartDate, date)
        val index = Math.floorMod(days, cycleLength.toLong()).toInt()
        val shiftType = shiftTypes[pattern.cycleShiftTypeIds[index]]
        if (shiftType != null && shiftType.generatesEvent) {
            result.add(GeneratedShiftOccurrence(date, shiftType))
        }
        date = date.plusDays(1)
    }
    return result
}

class ShiftPatternStore(context: Context) {

    private val rootDir: File = context.applicationContext.filesDir
    private val typeDir: File = File(rootDir, TYPE_DIR)
    private val patternDir: File = File(rootDir, PATTERN_DIR)
    private val generationDir: File = File(rootDir, GENERATION_DIR)

    fun saveType(type: ShiftType) {
        runCatching {
            if (!typeDir.exists()) typeDir.mkdirs()
            fileFor(typeDir, type.id).writeText(encodeType(type).toString())
        }
    }

    fun listTypes(): List<ShiftType> =
        listJson(typeDir, ::decodeType).sortedByDescending { it.createdAtMs }

    fun removeType(id: String) {
        runCatching { fileFor(typeDir, id).delete() }
    }

    fun savePattern(pattern: ShiftPattern) {
        runCatching {
            if (!patternDir.exists()) patternDir.mkdirs()
            fileFor(patternDir, pattern.id).writeText(encodePattern(pattern).toString())
        }
    }

    fun listPatterns(): List<ShiftPattern> =
        listJson(patternDir, ::decodePattern).sortedByDescending { it.createdAtMs }

    fun removePattern(id: String) {
        runCatching { fileFor(patternDir, id).delete() }
    }

    fun saveGeneration(record: ShiftGenerationRecord) {
        runCatching {
            if (!generationDir.exists()) generationDir.mkdirs()
            fileFor(generationDir, record.id).writeText(encodeGeneration(record).toString())
        }
    }

    fun listGenerations(): List<ShiftGenerationRecord> =
        listJson(generationDir, ::decodeGeneration).sortedByDescending { it.generatedAtMs }

    fun removeGeneration(id: String) {
        runCatching { fileFor(generationDir, id).delete() }
    }

    fun removeGenerationsForPattern(patternId: String) {
        listGenerations().filter { it.patternId == patternId }.forEach { removeGeneration(it.id) }
    }

    private fun <T> listJson(dir: File, decode: (String) -> T): List<T> {
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(EXT) } ?: return emptyList()
        val result = ArrayList<T>(files.size)
        for (file in files) {
            val item = runCatching { decode(file.readText()) }.getOrNull()
            if (item == null) {
                runCatching { file.delete() }
            } else {
                result.add(item)
            }
        }
        return result
    }

    private fun fileFor(dir: File, id: String): File = File(dir, safeName(id) + EXT)

    private fun encodeType(type: ShiftType): JSONObject = JSONObject()
        .put("id", type.id)
        .put("name", type.name)
        .put("colorHex", type.colorHex)
        .put("startMinuteOfDay", type.startMinuteOfDay ?: JSONObject.NULL)
        .put("durationMinutes", type.durationMinutes ?: JSONObject.NULL)
        .put("isAllDay", type.isAllDay)
        .put("reminderMinutes", type.reminderMinutes ?: JSONObject.NULL)
        .put("createdAtMs", type.createdAtMs)

    private fun decodeType(text: String): ShiftType {
        val o = JSONObject(text)
        return ShiftType(
            id = o.getString("id"),
            name = o.optString("name", "Shift"),
            colorHex = o.optString("colorHex", "#FF3B30"),
            startMinuteOfDay = if (o.isNull("startMinuteOfDay")) null else o.optInt("startMinuteOfDay"),
            durationMinutes = if (o.isNull("durationMinutes")) null else o.optInt("durationMinutes"),
            isAllDay = o.optBoolean("isAllDay", false),
            reminderMinutes = if (o.isNull("reminderMinutes")) null else o.optInt("reminderMinutes"),
            createdAtMs = o.optLong("createdAtMs", 0L),
        )
    }

    private fun encodePattern(pattern: ShiftPattern): JSONObject {
        val cycle = JSONArray()
        pattern.cycleShiftTypeIds.forEach { cycle.put(it) }
        return JSONObject()
            .put("id", pattern.id)
            .put("name", pattern.name)
            .put("cycleShiftTypeIds", cycle)
            .put("cycleStartDate", pattern.cycleStartDate.toString())
            .put("createdAtMs", pattern.createdAtMs)
    }

    private fun decodePattern(text: String): ShiftPattern {
        val o = JSONObject(text)
        val cycle = o.optJSONArray("cycleShiftTypeIds")
        val ids = ArrayList<String>()
        if (cycle != null) {
            for (i in 0 until cycle.length()) ids.add(cycle.getString(i))
        }
        return ShiftPattern(
            id = o.getString("id"),
            name = o.optString("name", "Pattern"),
            cycleShiftTypeIds = ids,
            cycleStartDate = LocalDate.parse(o.optString("cycleStartDate", LocalDate.now().toString())),
            createdAtMs = o.optLong("createdAtMs", 0L),
        )
    }

    private fun encodeGeneration(record: ShiftGenerationRecord): JSONObject {
        val ids = JSONArray()
        record.eventIds.forEach { ids.put(it) }
        return JSONObject()
            .put("id", record.id)
            .put("patternId", record.patternId)
            .put("generatedAtMs", record.generatedAtMs)
            .put("eventIds", ids)
            .put("rangeStart", record.rangeStart.toString())
            .put("rangeEnd", record.rangeEnd.toString())
    }

    private fun decodeGeneration(text: String): ShiftGenerationRecord {
        val o = JSONObject(text)
        val idsArray = o.optJSONArray("eventIds")
        val ids = ArrayList<String>()
        if (idsArray != null) {
            for (i in 0 until idsArray.length()) ids.add(idsArray.getString(i))
        }
        return ShiftGenerationRecord(
            id = o.getString("id"),
            patternId = o.getString("patternId"),
            generatedAtMs = o.optLong("generatedAtMs", 0L),
            eventIds = ids,
            rangeStart = LocalDate.parse(o.getString("rangeStart")),
            rangeEnd = LocalDate.parse(o.getString("rangeEnd")),
        )
    }

    private fun safeName(id: String): String = id.replace(Regex("[^A-Za-z0-9_.-]"), "_")

    companion object {
        private const val TYPE_DIR = "shift_types"
        private const val PATTERN_DIR = "shift_patterns"
        private const val GENERATION_DIR = "shift_generations"
        private const val EXT = ".json"
    }
}
