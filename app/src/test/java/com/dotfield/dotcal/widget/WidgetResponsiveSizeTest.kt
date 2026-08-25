package com.dotfield.dotcal.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetResponsiveSizeTest {
    @Test
    fun classifiesSupportedWidgetShapes() {
        assertEquals(WidgetResponsiveSizeClass.CompactSquare, classifyWidgetSize(90, 90))
        assertEquals(WidgetResponsiveSizeClass.CompactWide, classifyWidgetSize(180, 90))
        assertEquals(WidgetResponsiveSizeClass.MediumSquare, classifyWidgetSize(190, 190))
        assertEquals(WidgetResponsiveSizeClass.MediumWide, classifyWidgetSize(320, 140))
        assertEquals(WidgetResponsiveSizeClass.Large, classifyWidgetSize(320, 260))
    }

    @Test
    fun handlesInvalidDimensionsAsCompactSquare() {
        assertEquals(WidgetResponsiveSizeClass.CompactSquare, classifyWidgetSize(-1, -1))
    }
}
