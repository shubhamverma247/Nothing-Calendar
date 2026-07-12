package com.dotfield.dotcal.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.calendarPreferencesDataStore by preferencesDataStore(name = "calendar_preferences")

object CalendarPreferences {
    val KEY_DEFAULT_VIEW = stringPreferencesKey("default_view")
    val KEY_WEEK_START = stringPreferencesKey("week_start")
    val KEY_DEFAULT_REMINDER = intPreferencesKey("default_reminder")
    val KEY_DEFAULT_EVENT_DURATION = intPreferencesKey("default_event_duration")
    val KEY_SHOW_WEEK_NUMBERS = booleanPreferencesKey("show_week_numbers")
    val KEY_YEAR_HEATMAP = booleanPreferencesKey("year_heatmap")
    val KEY_DEFAULT_ALL_DAY_REMINDER_TIME = stringPreferencesKey("default_all_day_reminder_time")
    val KEY_SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
    val KEY_SYNC_INTERVAL_MINS = intPreferencesKey("sync_interval_mins")
    val KEY_BIRTHDAY_ENABLED = booleanPreferencesKey("birthday_enabled")
    val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val KEY_LAST_SYNC_MS = longPreferencesKey("last_sync_ms")
    val KEY_SHOW_DECLINED = booleanPreferencesKey("show_declined")
    val KEY_24_HOUR_FORMAT = booleanPreferencesKey("twenty_four_hour_format")
    val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color")
    val KEY_APP_FONT = stringPreferencesKey("app_font")
    val KEY_LAST_SELECTED_DATE = stringPreferencesKey("last_selected_date")
    val KEY_IS_PRO = booleanPreferencesKey("is_pro")
    val KEY_WIDGET_TRANSPARENT = booleanPreferencesKey("widget_transparent")
    val KEY_WIDGET_DOT_TEXTURE = booleanPreferencesKey("widget_dot_texture")
    val KEY_WIDGET_ACCOUNT_ID = stringPreferencesKey("widget_account_id")
    val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    val KEY_APP_LOCK_PIN_SALT = stringPreferencesKey("app_lock_pin_salt")
    val KEY_APP_LOCK_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
    val KEY_PRIVATE_VAULT_EVENT_IDS = stringPreferencesKey("private_vault_event_ids")
}
