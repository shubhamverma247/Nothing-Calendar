package com.dotfield.dotcal.data.shifts

import java.time.LocalDate
import java.util.Base64

enum class ShiftEventGeneratedBy(val storageKey: String) {
    Manual("manual"),
    Pattern("pattern");

    companion object {
        fun fromStorage(value: String?): ShiftEventGeneratedBy =
            entries.firstOrNull { it.storageKey == value } ?: Manual
    }
}

data class ShiftEventMetadata(
    val shiftTypeId: String,
    val shiftTypeName: String,
    val colorHex: String,
    val date: LocalDate,
    val generatedBy: ShiftEventGeneratedBy,
    val patternId: String? = null,
)

fun ShiftEventMetadata.encode(): String = listOf(
    SHIFT_METADATA_VERSION,
    shiftTypeId.encodedField(),
    shiftTypeName.encodedField(),
    colorHex.encodedField(),
    date.toString(),
    generatedBy.storageKey,
    patternId.orEmpty().encodedField(),
).joinToString(FIELD_SEPARATOR.toString())

fun parseShiftEventMetadata(text: String): ShiftEventMetadata? = runCatching {
    val parts = text.split(FIELD_SEPARATOR)
    require(parts.size == 7 && parts[0] == SHIFT_METADATA_VERSION)
    ShiftEventMetadata(
        shiftTypeId = parts[1].decodedField(),
        shiftTypeName = parts[2].decodedField().ifBlank { "Shift" },
        colorHex = parts[3].decodedField().ifBlank { "#FF3B30" },
        date = LocalDate.parse(parts[4]),
        generatedBy = ShiftEventGeneratedBy.fromStorage(parts[5]),
        patternId = parts[6].decodedField().takeIf { it.isNotBlank() },
    )
}.getOrNull()

fun shiftMetadataFor(type: ShiftType, date: LocalDate, generatedBy: ShiftEventGeneratedBy, patternId: String? = null) =
    ShiftEventMetadata(
        shiftTypeId = type.id,
        shiftTypeName = type.name,
        colorHex = type.colorHex,
        date = date,
        generatedBy = generatedBy,
        patternId = patternId,
    )

private const val SHIFT_METADATA_VERSION = "v1"
private const val FIELD_SEPARATOR = '|'

private fun String.encodedField(): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(toByteArray(Charsets.UTF_8))

private fun String.decodedField(): String =
    String(Base64.getUrlDecoder().decode(this), Charsets.UTF_8)
