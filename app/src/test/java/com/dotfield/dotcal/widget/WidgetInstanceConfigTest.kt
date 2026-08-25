package com.dotfield.dotcal.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetInstanceConfigTest {
    @Test
    fun encodeDecodeRoundTripsConfiguration() {
        val config = WidgetInstanceConfig(
            category = WidgetCategory.Schedule,
            viewType = WidgetViewType.Agenda,
            content = WidgetContentOptions(showTasks = true, showLocation = true),
            calendarFilter = WidgetCalendarFilter("work"),
            timeRange = WidgetTimeRange.Next14Days,
            layoutMode = WidgetLayoutMode.Detailed,
            density = WidgetContentDensity.High,
            appearance = WidgetAppearanceConfig(transparent = true, opacityPercent = 42, showDotTexture = false),
            interaction = WidgetInteractionConfig(WidgetTapAction.OpenAgenda, eventTapActionOpenEvent = false),
        )

        assertEquals(config, WidgetInstanceConfig.decodeOrDefault(config.encode(), WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Small)))
    }

    @Test
    fun invalidConfigFallsBackToLegacyDefault() {
        val fallback = WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.MonthGrid, accountId = "personal")

        assertEquals(fallback, WidgetInstanceConfig.decodeOrDefault("{bad json", fallback))
    }

    @Test
    fun legacyDefaultsMapOldWidgetsToUnifiedCategories() {
        assertEquals(WidgetCategory.Schedule, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Small).category)
        assertEquals(WidgetViewType.NextEvent, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Small).viewType)
        assertEquals(WidgetCategory.Calendar, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Large).category)
        assertEquals(WidgetCategory.Countdown, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Countdown).category)
    }

    @Test
    fun configsKeepIndependentCalendarFilters() {
        val work = WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Agenda, "work")
        val personal = WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Agenda, "personal")

        assertEquals("work", work.calendarFilter.accountId)
        assertEquals("personal", personal.calendarFilter.accountId)
    }

    @Test
    fun countdownLegacyUsesFourteenDayRange() {
        assertEquals(WidgetTimeRange.Next14Days, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Countdown).timeRange)
    }
}
