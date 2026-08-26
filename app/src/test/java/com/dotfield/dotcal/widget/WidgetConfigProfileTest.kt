package com.dotfield.dotcal.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetConfigProfileTest {

    @Test
    fun `dateOnly and countdown are fully fixed widgets`() {
        for (kind in listOf(LegacyWidgetKind.DateOnly, LegacyWidgetKind.Countdown)) {
            val profile = profileForKind(kind)
            assertFalse(profile.showContent)
            assertFalse(profile.showTimeRange)
            assertFalse(profile.showCalendar)
            assertFalse(profile.showDisplay)
            assertFalse(profile.showDensity)
            assertFalse(profile.showTapAction)
            assertFalse(profile.showAdvanced)
            assertTrue(profile.categories.isEmpty())
            assertTrue(profile.ranges.isEmpty())
        }
    }

    @Test
    fun `compact month only exposes calendar category and tap action`() {
        val profile = profileForKind(LegacyWidgetKind.MonthCompact)
        assertEquals(listOf(WidgetCategory.Calendar), profile.categories)
        assertTrue(profile.showCalendar)
        assertTrue(profile.showTapAction)
        assertFalse(profile.showAdvanced)
        assertFalse(profile.showContent)
        assertFalse(profile.showDisplay)
        assertFalse(profile.showDensity)
    }

    @Test
    fun `small has no density and only short ranges`() {
        val profile = profileForKind(LegacyWidgetKind.Small)
        assertFalse(profile.showDensity)
        assertTrue(profile.showTimeRange)
        assertEquals(listOf(WidgetTimeRange.Today, WidgetTimeRange.Next3Days), profile.ranges)
        assertTrue(WidgetCategory.QuickActions in profile.categories)
    }

    @Test
    fun `small default range fits its own profile`() {
        val config = WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Small)
        assertTrue(config.timeRange in profileForKind(LegacyWidgetKind.Small).ranges)
    }

    @Test
    fun `medium and agenda expose everything with long ranges`() {
        for (kind in listOf(LegacyWidgetKind.Medium, LegacyWidgetKind.Agenda)) {
            val profile = profileForKind(kind)
            assertTrue(profile.showDensity)
            assertTrue(profile.showAdvanced)
            assertTrue(profile.showTapAction)
            assertEquals(WidgetTimeRange.Next14Days, profile.ranges.last())
        }
    }

    @Test
    fun `large and monthGrid include calendar first then all categories`() {
        for (kind in listOf(LegacyWidgetKind.Large, LegacyWidgetKind.MonthGrid)) {
            val profile = profileForKind(kind)
            assertEquals(WidgetCategory.Calendar, profile.categories.first())
            assertEquals(WidgetCategory.QuickActions, profile.categories.last())
        }
    }

    @Test
    fun `shiftWide is shift-only without display row`() {
        val profile = profileForKind(LegacyWidgetKind.ShiftWide)
        assertEquals(listOf(WidgetCategory.Shift), profile.categories)
        assertFalse(profile.showDisplay)
        assertFalse(profile.showTapAction)
        assertTrue(profile.showAdvanced)
        assertTrue(profile.showDensity)
    }

    @Test
    fun `sanitizeForFree strips every pro-gated value`() {
        val pro = WidgetInstanceConfig(
            category = WidgetCategory.Schedule,
            viewType = WidgetViewType.Agenda,
            calendarFilter = WidgetCalendarFilter("account-1"),
            timeRange = WidgetTimeRange.Next14Days,
            density = WidgetContentDensity.High,
            interaction = WidgetInteractionConfig(tapAction = WidgetTapAction.Search),
            appearance = WidgetAppearanceConfig(
                themeMode = "Dark",
                accentColor = "#00FF00",
                transparent = true,
                opacityPercent = 30,
                showDotTexture = false,
            ),
        )
        val free = sanitizeForFree(pro)
        assertEquals(null, free.calendarFilter.accountId)
        assertEquals(WidgetTimeRange.Next7Days, free.timeRange)
        assertEquals(WidgetContentDensity.Medium, free.density)
        assertEquals(WidgetTapAction.OpenCalendar, free.interaction.tapAction)
        assertEquals(null, free.appearance.accentColor)
        assertEquals(null, free.appearance.transparent)
        assertEquals(null, free.appearance.opacityPercent)
        assertEquals(null, free.appearance.showDotTexture)
    }

    @Test
    fun `sanitizeForFree keeps free-tier values untouched`() {
        val freeConfig = WidgetInstanceConfig(
            category = WidgetCategory.Schedule,
            viewType = WidgetViewType.Agenda,
            timeRange = WidgetTimeRange.Next3Days,
            density = WidgetContentDensity.Low,
            appearance = WidgetAppearanceConfig(themeMode = "Light"),
        )
        assertEquals(freeConfig, sanitizeForFree(freeConfig))
    }
}
