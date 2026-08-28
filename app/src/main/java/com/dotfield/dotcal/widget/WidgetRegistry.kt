package com.dotfield.dotcal.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import kotlinx.coroutines.flow.first

private fun widgetEntry(receiverClass: String, appWidgetId: Int) = "$receiverClass:$appWidgetId"

internal fun receiverClassNameForWidgetKind(kind: LegacyWidgetKind): String = when (kind) {
    LegacyWidgetKind.DateOnly -> "DateOnlyDotCalWidgetReceiver"
    LegacyWidgetKind.MonthCompact -> "CompactMonthDotCalWidgetReceiver"
    LegacyWidgetKind.ShiftWide -> "ShiftWideWidgetReceiver"
    LegacyWidgetKind.Small -> "SmallDotCalWidgetReceiver"
    LegacyWidgetKind.Medium -> "MediumDotCalWidgetReceiver"
    LegacyWidgetKind.Large -> "LargeDotCalWidgetReceiver"
    LegacyWidgetKind.Countdown -> "EventCountdownWidgetReceiver"
    LegacyWidgetKind.Agenda -> "AgendaListWidgetReceiver"
    LegacyWidgetKind.MonthGrid -> "MonthGridWidgetReceiver"
}

internal suspend fun registerConfiguredWidget(context: Context, receiverClass: String, appWidgetId: Int) {
    val entry = widgetEntry(receiverClass, appWidgetId)
    context.applicationContext.calendarPreferencesDataStore.edit { preferences ->
        val entries = preferences[CalendarPreferences.KEY_WIDGET_CONFIGURED_ENTRIES].orEmpty().toMutableSet()
        entries.add(entry)
        preferences[CalendarPreferences.KEY_WIDGET_CONFIGURED_ENTRIES] = entries
    }
}

internal suspend fun unregisterConfiguredWidgets(context: Context, appWidgetIds: IntArray) {
    if (appWidgetIds.isEmpty()) return
    val ids = appWidgetIds.toSet()
    context.applicationContext.calendarPreferencesDataStore.edit { preferences ->
        val entries = preferences[CalendarPreferences.KEY_WIDGET_CONFIGURED_ENTRIES].orEmpty().toMutableSet()
        val next = entries.filterTo(mutableSetOf()) { entry ->
            entry.substringAfterLast(':').toIntOrNull() !in ids
        }
        if (next != entries) {
            preferences[CalendarPreferences.KEY_WIDGET_CONFIGURED_ENTRIES] = next
        }
    }
}

internal suspend fun clearConfiguredWidgetsForReceiver(context: Context, receiverClass: String) {
    context.applicationContext.calendarPreferencesDataStore.edit { preferences ->
        val entries = preferences[CalendarPreferences.KEY_WIDGET_CONFIGURED_ENTRIES].orEmpty().toMutableSet()
        val next = entries.filterTo(mutableSetOf()) { entry ->
            entry.substringBefore(':') != receiverClass
        }
        if (next != entries) {
            preferences[CalendarPreferences.KEY_WIDGET_CONFIGURED_ENTRIES] = next
        }
    }
}

internal suspend fun loadConfiguredWidgetIds(context: Context): Set<Int> {
    return context.applicationContext.calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_WIDGET_CONFIGURED_ENTRIES]
        .orEmpty()
        .mapNotNull { it.substringAfterLast(':').toIntOrNull() }
        .toSet()
}
