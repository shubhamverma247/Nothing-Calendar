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

    @Test
    fun monthAgendaUsesExtraHeightWithoutDroppingBelowOneRow() {
        assertEquals(1, monthAgendaRowCapacity(250))
        assertEquals(3, monthAgendaRowCapacity(320))
        assertEquals(6, monthAgendaRowCapacity(400))
        assertEquals(12, monthAgendaRowCapacity(520))
        assertEquals(20, monthAgendaRowCapacity(800))
        assertEquals(1, monthAgendaRowCapacity(200))
    }

    @Test
    fun monthAgendaReservesMoreHeightWhenLocationsAreShown() {
        assertEquals(2, monthAgendaRowCapacity(320, showLocation = true))
    }

    @Test
    fun monthAgendaLeavesRoomForOverflowIndicator() {
        assertEquals(5, monthAgendaVisibleRowCount(17, 380))
        assertEquals(6, monthAgendaVisibleRowCount(17, 403))
        assertEquals(17, monthAgendaVisibleRowCount(17, 800))
    }

    @Test
    fun monthAgendaGroupsAvoidGlanceColumnChildLimit() {
        assertEquals(listOf(9, 8), monthAgendaColumnGroupSizes(17))
        assertEquals(listOf(9, 9, 2), monthAgendaColumnGroupSizes(20))
        assertEquals(emptyList<Int>(), monthAgendaColumnGroupSizes(0))
    }
}
