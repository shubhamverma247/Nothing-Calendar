package com.dotfield.dotcal.widget

enum class WidgetResponsiveSizeClass {
    CompactSquare,
    CompactWide,
    MediumSquare,
    MediumWide,
    Large,
}

internal const val MONTH_AGENDA_MAX_ITEMS = 20
internal const val MONTH_AGENDA_COLUMN_GROUP_SIZE = 9
private const val MONTH_AGENDA_FIXED_HEIGHT_DP = 224
private const val MONTH_AGENDA_MORE_ROW_HEIGHT_DP = 18

fun classifyWidgetSize(widthDp: Int, heightDp: Int): WidgetResponsiveSizeClass {
    val width = widthDp.coerceAtLeast(0)
    val height = heightDp.coerceAtLeast(0)
    return when {
        width >= 300 && height >= 220 -> WidgetResponsiveSizeClass.Large
        width >= 300 -> WidgetResponsiveSizeClass.MediumWide
        width >= 180 && height >= 180 -> WidgetResponsiveSizeClass.MediumSquare
        width > height -> WidgetResponsiveSizeClass.CompactWide
        else -> WidgetResponsiveSizeClass.CompactSquare
    }
}

internal fun monthAgendaRowCapacity(heightDp: Int, showLocation: Boolean = false): Int {
    val rowHeight = if (showLocation) 31 else 23
    val availableHeight = heightDp - MONTH_AGENDA_FIXED_HEIGHT_DP - MONTH_AGENDA_MORE_ROW_HEIGHT_DP
    return (availableHeight / rowHeight).coerceIn(1, MONTH_AGENDA_MAX_ITEMS)
}

internal fun monthAgendaVisibleRowCount(eventCount: Int, heightDp: Int, showLocation: Boolean = false): Int {
    val capacity = monthAgendaRowCapacity(heightDp, showLocation)
    return if (eventCount > capacity) (capacity - 1).coerceAtLeast(1) else eventCount.coerceAtLeast(0)
}

internal fun monthAgendaColumnGroupSizes(rowCount: Int): List<Int> {
    var remaining = rowCount.coerceAtLeast(0)
    return buildList {
        while (remaining > 0) {
            val groupSize = remaining.coerceAtMost(MONTH_AGENDA_COLUMN_GROUP_SIZE)
            add(groupSize)
            remaining -= groupSize
        }
    }
}
