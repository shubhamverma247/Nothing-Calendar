package com.dotfield.dotcal.widget

enum class WidgetResponsiveSizeClass {
    CompactSquare,
    CompactWide,
    MediumSquare,
    MediumWide,
    Large,
}

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
