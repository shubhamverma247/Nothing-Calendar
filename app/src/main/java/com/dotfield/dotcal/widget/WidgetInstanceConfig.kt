package com.dotfield.dotcal.widget

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class WidgetCategory {
    Calendar,
    Schedule,
    Today,
    Tasks,
    Countdown,
    QuickActions,
}

enum class WidgetViewType {
    Month,
    Week,
    Day,
    Agenda,
    NextEvent,
    Upcoming,
    Today,
    TaskList,
    Countdown,
    QuickAction,
}

enum class WidgetTimeRange(val days: Long) {
    Today(1),
    Next24Hours(1),
    Next3Days(3),
    Next7Days(7),
    Next14Days(14),
}

enum class WidgetLayoutMode {
    Minimal,
    Compact,
    Detailed,
}

enum class WidgetContentDensity {
    Low,
    Medium,
    High,
}

enum class WidgetTapAction {
    OpenCalendar,
    OpenToday,
    OpenAgenda,
    QuickAdd,
    CreateEvent,
    CreateTask,
    Search,
}

enum class LegacyWidgetKind {
    Small,
    Medium,
    Large,
    Countdown,
    Agenda,
    MonthGrid,
}

data class WidgetContentOptions(
    val showEvents: Boolean = true,
    val showTasks: Boolean = false,
    val showEventTitle: Boolean = true,
    val showTime: Boolean = true,
    val showLocation: Boolean = false,
    val showEventColors: Boolean = true,
    val showAllDayEvents: Boolean = true,
    val showTodayHighlight: Boolean = true,
    val showWeekNumbers: Boolean = false,
    val showCompletedTasks: Boolean = false,
    val showDayProgress: Boolean = false,
)

data class WidgetCalendarFilter(
    val accountId: String? = null,
)

data class WidgetAppearanceConfig(
    val transparent: Boolean? = null,
    val opacityPercent: Int? = null,
    val showDotTexture: Boolean? = null,
)

data class WidgetInteractionConfig(
    val tapAction: WidgetTapAction = WidgetTapAction.OpenCalendar,
    val eventTapActionOpenEvent: Boolean = true,
)

data class WidgetInstanceConfig(
    val category: WidgetCategory,
    val viewType: WidgetViewType,
    val content: WidgetContentOptions = WidgetContentOptions(),
    val calendarFilter: WidgetCalendarFilter = WidgetCalendarFilter(),
    val timeRange: WidgetTimeRange = WidgetTimeRange.Next7Days,
    val layoutMode: WidgetLayoutMode = WidgetLayoutMode.Compact,
    val density: WidgetContentDensity = WidgetContentDensity.Medium,
    val appearance: WidgetAppearanceConfig = WidgetAppearanceConfig(),
    val interaction: WidgetInteractionConfig = WidgetInteractionConfig(),
) {
    fun encode(): String {
        return buildList {
            add("category" to category.name)
            add("viewType" to viewType.name)
            add("timeRange" to timeRange.name)
            add("layoutMode" to layoutMode.name)
            add("density" to density.name)
            add("calendar.accountId" to calendarFilter.accountId.orEmpty())
            addAll(content.entries())
            addAll(appearance.entries())
            add("interaction.tapAction" to interaction.tapAction.name)
            add("interaction.eventTapActionOpenEvent" to interaction.eventTapActionOpenEvent.toString())
        }.joinToString("\n") { (key, value) -> "${key.urlEncode()}\t${value.urlEncode()}" }
    }

    companion object {
        fun decodeOrDefault(text: String?, fallback: WidgetInstanceConfig): WidgetInstanceConfig {
            if (text.isNullOrBlank()) return fallback
            return runCatching {
                val values = text.lineSequence()
                    .mapNotNull { line ->
                        val parts = line.split('\t', limit = 2)
                        if (parts.size != 2) null else parts[0].urlDecode() to parts[1].urlDecode()
                    }
                    .toMap()
                WidgetInstanceConfig(
                    category = values.enumOrDefault("category", fallback.category),
                    viewType = values.enumOrDefault("viewType", fallback.viewType),
                    content = values.decodeContent(fallback.content),
                    calendarFilter = WidgetCalendarFilter(values["calendar.accountId"]?.takeIf(String::isNotBlank) ?: fallback.calendarFilter.accountId),
                    timeRange = values.enumOrDefault("timeRange", fallback.timeRange),
                    layoutMode = values.enumOrDefault("layoutMode", fallback.layoutMode),
                    density = values.enumOrDefault("density", fallback.density),
                    appearance = values.decodeAppearance(fallback.appearance),
                    interaction = values.decodeInteraction(fallback.interaction),
                )
            }.getOrDefault(fallback)
        }

        fun legacyDefault(kind: LegacyWidgetKind, accountId: String? = null): WidgetInstanceConfig {
            val filter = WidgetCalendarFilter(accountId)
            return when (kind) {
                LegacyWidgetKind.Small -> WidgetInstanceConfig(
                    category = WidgetCategory.Schedule,
                    viewType = WidgetViewType.NextEvent,
                    calendarFilter = filter,
                    timeRange = WidgetTimeRange.Next7Days,
                    layoutMode = WidgetLayoutMode.Minimal,
                    interaction = WidgetInteractionConfig(WidgetTapAction.CreateEvent),
                )
                LegacyWidgetKind.Medium,
                LegacyWidgetKind.Agenda -> WidgetInstanceConfig(
                    category = WidgetCategory.Schedule,
                    viewType = WidgetViewType.Agenda,
                    calendarFilter = filter,
                    timeRange = WidgetTimeRange.Next7Days,
                    layoutMode = WidgetLayoutMode.Compact,
                    interaction = WidgetInteractionConfig(WidgetTapAction.OpenAgenda),
                )
                LegacyWidgetKind.Large,
                LegacyWidgetKind.MonthGrid -> WidgetInstanceConfig(
                    category = WidgetCategory.Calendar,
                    viewType = WidgetViewType.Month,
                    calendarFilter = filter,
                    timeRange = WidgetTimeRange.Next7Days,
                    layoutMode = WidgetLayoutMode.Detailed,
                    interaction = WidgetInteractionConfig(WidgetTapAction.OpenCalendar),
                )
                LegacyWidgetKind.Countdown -> WidgetInstanceConfig(
                    category = WidgetCategory.Countdown,
                    viewType = WidgetViewType.Countdown,
                    calendarFilter = filter,
                    timeRange = WidgetTimeRange.Next14Days,
                    layoutMode = WidgetLayoutMode.Minimal,
                )
            }
        }
    }
}

internal fun WidgetInstanceConfig.maxVisibleItems(defaultMax: Int): Int {
    val densityMax = when (density) {
        WidgetContentDensity.Low -> 1
        WidgetContentDensity.Medium -> defaultMax
        WidgetContentDensity.High -> defaultMax + 2
    }
    return when (layoutMode) {
        WidgetLayoutMode.Minimal -> 1
        WidgetLayoutMode.Compact -> densityMax
        WidgetLayoutMode.Detailed -> densityMax + 1
    }.coerceIn(1, 8)
}

private fun WidgetContentOptions.entries(): List<Pair<String, String>> {
    return listOf(
        "content.showEvents" to showEvents.toString(),
        "content.showTasks" to showTasks.toString(),
        "content.showEventTitle" to showEventTitle.toString(),
        "content.showTime" to showTime.toString(),
        "content.showLocation" to showLocation.toString(),
        "content.showEventColors" to showEventColors.toString(),
        "content.showAllDayEvents" to showAllDayEvents.toString(),
        "content.showTodayHighlight" to showTodayHighlight.toString(),
        "content.showWeekNumbers" to showWeekNumbers.toString(),
        "content.showCompletedTasks" to showCompletedTasks.toString(),
        "content.showDayProgress" to showDayProgress.toString(),
    )
}

private fun WidgetAppearanceConfig.entries(): List<Pair<String, String>> {
    return listOf(
        "appearance.transparent" to transparent?.toString().orEmpty(),
        "appearance.opacityPercent" to opacityPercent?.toString().orEmpty(),
        "appearance.showDotTexture" to showDotTexture?.toString().orEmpty(),
    )
}

private fun Map<String, String>.decodeContent(fallback: WidgetContentOptions): WidgetContentOptions {
    return WidgetContentOptions(
        showEvents = booleanOrDefault("content.showEvents", fallback.showEvents),
        showTasks = booleanOrDefault("content.showTasks", fallback.showTasks),
        showEventTitle = booleanOrDefault("content.showEventTitle", fallback.showEventTitle),
        showTime = booleanOrDefault("content.showTime", fallback.showTime),
        showLocation = booleanOrDefault("content.showLocation", fallback.showLocation),
        showEventColors = booleanOrDefault("content.showEventColors", fallback.showEventColors),
        showAllDayEvents = booleanOrDefault("content.showAllDayEvents", fallback.showAllDayEvents),
        showTodayHighlight = booleanOrDefault("content.showTodayHighlight", fallback.showTodayHighlight),
        showWeekNumbers = booleanOrDefault("content.showWeekNumbers", fallback.showWeekNumbers),
        showCompletedTasks = booleanOrDefault("content.showCompletedTasks", fallback.showCompletedTasks),
        showDayProgress = booleanOrDefault("content.showDayProgress", fallback.showDayProgress),
    )
}

private fun Map<String, String>.decodeAppearance(fallback: WidgetAppearanceConfig): WidgetAppearanceConfig {
    return WidgetAppearanceConfig(
        transparent = nullableBoolean("appearance.transparent") ?: fallback.transparent,
        opacityPercent = this["appearance.opacityPercent"]?.toIntOrNull()?.coerceIn(0, 100) ?: fallback.opacityPercent,
        showDotTexture = nullableBoolean("appearance.showDotTexture") ?: fallback.showDotTexture,
    )
}

private fun Map<String, String>.decodeInteraction(fallback: WidgetInteractionConfig): WidgetInteractionConfig {
    return WidgetInteractionConfig(
        tapAction = enumOrDefault("interaction.tapAction", fallback.tapAction),
        eventTapActionOpenEvent = booleanOrDefault("interaction.eventTapActionOpenEvent", fallback.eventTapActionOpenEvent),
    )
}

private inline fun <reified T : Enum<T>> Map<String, String>.enumOrDefault(key: String, fallback: T): T {
    return runCatching { enumValueOf<T>(this[key].orEmpty()) }.getOrDefault(fallback)
}

private fun Map<String, String>.booleanOrDefault(key: String, fallback: Boolean): Boolean {
    return when (this[key]) {
        "true" -> true
        "false" -> false
        else -> fallback
    }
}

private fun Map<String, String>.nullableBoolean(key: String): Boolean? {
    return when (this[key]) {
        "true" -> true
        "false" -> false
        else -> null
    }
}

private fun String.urlEncode(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.name())
}

private fun String.urlDecode(): String {
    return URLDecoder.decode(this, StandardCharsets.UTF_8.name())
}
