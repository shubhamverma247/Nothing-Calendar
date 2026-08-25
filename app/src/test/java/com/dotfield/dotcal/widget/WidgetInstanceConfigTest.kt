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
            appearance = WidgetAppearanceConfig(themeMode = "Dark", accentColor = "BLUE", transparent = true, opacityPercent = 42, showDotTexture = false),
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
        assertEquals(WidgetCategory.Today, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.DateOnly).category)
        assertEquals(WidgetViewType.Today, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.DateOnly).viewType)
        assertEquals(WidgetCategory.Calendar, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.MonthCompact).category)
        assertEquals(WidgetViewType.Month, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.MonthCompact).viewType)
        assertEquals(WidgetCategory.Shift, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.ShiftWide).category)
        assertEquals(WidgetViewType.ShiftList, WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.ShiftWide).viewType)
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

    @Test
    fun largeWidgetSupportsDenserDetailedAgenda() {
        val config = WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Agenda).copy(
            layoutMode = WidgetLayoutMode.Detailed,
            density = WidgetContentDensity.High,
        )

        assertEquals(7, config.maxVisibleItems(DotCalWidgetSize.Large.maxItems))
    }

    @Test
    fun smallWidgetStaysSingleItemEvenAtHighDensity() {
        val config = WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Small).copy(
            layoutMode = WidgetLayoutMode.Detailed,
            density = WidgetContentDensity.High,
        )

        assertEquals(1, config.maxVisibleItems(DotCalWidgetSize.Small.maxItems))
    }

    @Test
    fun calendarWidgetKeepsMonthGridAgendaShort() {
        val config = WidgetInstanceConfig.legacyDefault(LegacyWidgetKind.Large).copy(
            layoutMode = WidgetLayoutMode.Detailed,
            density = WidgetContentDensity.High,
        )

        assertEquals(2, config.maxVisibleItems(DotCalWidgetSize.Large.maxItems))
    }
}
