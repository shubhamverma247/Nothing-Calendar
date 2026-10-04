package com.dotfield.dotcal.data.shifts

import com.dotfield.dotcal.NOTHING_RED_HEX
import android.content.Context
import com.dotfield.dotcal.data.BulkEditUndoToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val MINUTES_PER_DAY = 24 * 60
const val SHIFT_OFF_TYPE_ID = "__off__"

data class ShiftType(
    val id: String,
    val name: String,
    val colorHex: String,
    val startMinuteOfDay: Int?,
    val durationMinutes: Int?,
    val isAllDay: Boolean,
    val reminderMinutes: Int?,
    val breakMinutes: Int? = null,
    val createdAtMs: Long,
) {
    val generatesEvent: Boolean
        get() = isAllDay || (startMinuteOfDay != null && durationMinutes != null && durationMinutes > 0)

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

enum class ShiftTypeMode {
    Timed,
    AllDay,
    OffDay,
}

fun ShiftType.mode(): ShiftTypeMode = when {
    isAllDay -> ShiftTypeMode.AllDay
    startMinuteOfDay != null && durationMinutes != null && durationMinutes > 0 -> ShiftTypeMode.Timed
    else -> ShiftTypeMode.OffDay
}

data class ShiftPattern(
    val id: String,
    val name: String,
    val cycleShiftTypeIds: List<String>,
    val cycleStartDate: LocalDate,
    val createdAtMs: Long,
    val archivedAtMs: Long? = null,
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

enum class ShiftPatternPreset {
    RotatingDaysNights,
    FourOnFourOff,
}

fun shiftPatternPresetCycle(
    preset: ShiftPatternPreset,
    primaryTypeId: String,
    secondaryTypeId: String? = null,
): List<String> = when (preset) {
    ShiftPatternPreset.RotatingDaysNights -> {
        val nightTypeId = requireNotNull(secondaryTypeId) { "Rotating days/nights needs two shift types" }
        listOf(primaryTypeId, primaryTypeId, nightTypeId, nightTypeId) + List(4) { SHIFT_OFF_TYPE_ID }
    }
    ShiftPatternPreset.FourOnFourOff -> List(4) { primaryTypeId } + List(4) { SHIFT_OFF_TYPE_ID }
}

fun duplicateShiftPattern(
    original: ShiftPattern,
    copyName: String,
    createdAtMs: Long = System.currentTimeMillis(),
): ShiftPattern = original.copy(
    id = ShiftPattern.newId(),
    name = copyName,
    createdAtMs = createdAtMs,
    archivedAtMs = null,
)

fun shiftTypeUsageCount(patterns: List<ShiftPattern>, shiftTypeId: String): Int =
    patterns.count { shiftTypeId in it.cycleShiftTypeIds }

data class GeneratedShiftOccurrence(
    val date: LocalDate,
    val shiftType: ShiftType,
)

fun shiftDurationMinutes(startMinuteOfDay: Int, endMinuteOfDay: Int): Int {
    val start = Math.floorMod(startMinuteOfDay, MINUTES_PER_DAY)
    val end = Math.floorMod(endMinuteOfDay, MINUTES_PER_DAY)
    val duration = Math.floorMod(end - start, MINUTES_PER_DAY)
    return if (duration == 0) MINUTES_PER_DAY else duration
}

fun shiftEndMinuteOfDay(startMinuteOfDay: Int, durationMinutes: Int): Int =
    Math.floorMod(startMinuteOfDay + durationMinutes, MINUTES_PER_DAY)

fun shiftEndDayOffset(startMinuteOfDay: Int, durationMinutes: Int): Int =
    Math.floorDiv(
        Math.floorMod(startMinuteOfDay, MINUTES_PER_DAY) + durationMinutes.coerceAtLeast(0),
        MINUTES_PER_DAY,
    )

fun isValidShiftBreakMinutes(breakMinutes: Int?, durationMinutes: Int?): Boolean =
    breakMinutes == null || (durationMinutes != null && breakMinutes in 1 until durationMinutes)

data class ShiftGenerationRecord(
    val id: String,
    val patternId: String,
    val generatedAtMs: Long,
    val events: List<ShiftGenerationEvent>,
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
) {
    val eventIds: List<String> get() = events.map { it.eventId }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

data class ShiftGenerationEvent(
    val eventId: String,
    val date: LocalDate? = null,
    val updatedAtMs: Long? = null,
    val cancelled: Boolean = false,
)

data class ShiftApplyResult(
    val createdCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val removedCount: Int,
    val undoToken: BulkEditUndoToken? = null,
) {
    val generatedCount: Int get() = createdCount + updatedCount
    val replacedCount: Int get() = updatedCount + removedCount
}

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

class ShiftPatternStore internal constructor(private val rootDir: File) {

    constructor(context: Context) : this(context.applicationContext.filesDir)
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

    fun saveGeneration(record: ShiftGenerationRecord): Boolean =
        runCatching {
            if (!generationDir.exists()) generationDir.mkdirs()
            fileFor(generationDir, record.id).writeText(encodeGeneration(record).toString())
            true
        }.getOrDefault(false)

    fun listGenerations(): List<ShiftGenerationRecord> =
        listJson(generationDir, ::decodeGeneration).sortedByDescending { it.generatedAtMs }

    fun removeGeneration(id: String): Boolean =
        runCatching { !fileFor(generationDir, id).exists() || fileFor(generationDir, id).delete() }
            .getOrDefault(false)

    fun removeGenerationsForPattern(patternId: String) {
        listGenerations().filter { it.patternId == patternId }.forEach { removeGeneration(it.id) }
    }

    fun markEventCancelled(eventId: String, date: LocalDate?): Boolean {
        var changed = false
        var persisted = true
        listGenerations().forEach { record ->
            if (record.events.none { it.eventId == eventId }) return@forEach
            changed = true
            persisted = saveGeneration(
                record.copy(
                    events = record.events.map { event ->
                        if (event.eventId == eventId) event.copy(date = event.date ?: date, cancelled = true) else event
                    },
                ),
            ) && persisted
        }
        return changed && persisted
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
        .put("breakMinutes", type.breakMinutes ?: JSONObject.NULL)
        .put("createdAtMs", type.createdAtMs)

    private fun decodeType(text: String): ShiftType {
        val o = JSONObject(text)
        return ShiftType(
            id = o.getString("id"),
            name = o.optString("name", "Shift"),
            colorHex = o.optString("colorHex", NOTHING_RED_HEX),
            startMinuteOfDay = if (o.isNull("startMinuteOfDay")) null else o.optInt("startMinuteOfDay"),
            durationMinutes = if (o.isNull("durationMinutes")) null else o.optInt("durationMinutes"),
            isAllDay = o.optBoolean("isAllDay", false),
            reminderMinutes = if (o.isNull("reminderMinutes")) null else o.optInt("reminderMinutes"),
            breakMinutes = if (!o.has("breakMinutes") || o.isNull("breakMinutes")) null else o.optInt("breakMinutes"),
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
            .put("archivedAtMs", pattern.archivedAtMs ?: JSONObject.NULL)
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
            archivedAtMs = if (o.isNull("archivedAtMs")) null else o.optLong("archivedAtMs"),
        )
    }

    private fun encodeGeneration(record: ShiftGenerationRecord): JSONObject {
        val ids = JSONArray()
        record.eventIds.forEach { ids.put(it) }
        val events = JSONArray()
        record.events.forEach { event ->
            events.put(
                JSONObject()
                    .put("eventId", event.eventId)
                    .put("date", event.date?.toString() ?: JSONObject.NULL)
                    .put("updatedAtMs", event.updatedAtMs ?: JSONObject.NULL)
                    .put("cancelled", event.cancelled),
            )
        }
        return JSONObject()
            .put("id", record.id)
            .put("patternId", record.patternId)
            .put("generatedAtMs", record.generatedAtMs)
            .put("eventIds", ids)
            .put("events", events)
            .put("rangeStart", record.rangeStart.toString())
            .put("rangeEnd", record.rangeEnd.toString())
    }

    private fun decodeGeneration(text: String): ShiftGenerationRecord {
        val o = JSONObject(text)
        val events = ArrayList<ShiftGenerationEvent>()
        val eventsArray = o.optJSONArray("events")
        if (eventsArray != null) {
            for (i in 0 until eventsArray.length()) {
                val event = eventsArray.getJSONObject(i)
                events.add(
                    ShiftGenerationEvent(
                        eventId = event.getString("eventId"),
                        date = if (event.isNull("date")) null else event.optString("date").takeIf { it.isNotBlank() }?.let(LocalDate::parse),
                        updatedAtMs = if (event.isNull("updatedAtMs")) null else event.optLong("updatedAtMs"),
                        cancelled = event.optBoolean("cancelled", false),
                    ),
                )
            }
        } else {
            val idsArray = o.optJSONArray("eventIds")
            if (idsArray != null) {
                for (i in 0 until idsArray.length()) {
                    events.add(ShiftGenerationEvent(eventId = idsArray.getString(i)))
                }
            }
        }
        return ShiftGenerationRecord(
            id = o.getString("id"),
            patternId = o.getString("patternId"),
            generatedAtMs = o.optLong("generatedAtMs", 0L),
            events = events,
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
