package com.dotfield.dotcal.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
    val KEY_HIDDEN_CALENDAR_MENU_ACTIONS = stringPreferencesKey("hidden_calendar_menu_actions")
    val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val KEY_LAST_SYNC_MS = longPreferencesKey("last_sync_ms")
    val KEY_SHOW_DECLINED = booleanPreferencesKey("show_declined")
    val KEY_24_HOUR_FORMAT = booleanPreferencesKey("twenty_four_hour_format")
    val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color")
    val KEY_APP_FONT = stringPreferencesKey("app_font")
    val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    val KEY_LAST_SELECTED_DATE = stringPreferencesKey("last_selected_date")
    val KEY_IS_PRO = booleanPreferencesKey("is_pro")
    val KEY_WIDGET_TRANSPARENT = booleanPreferencesKey("widget_transparent")
    val KEY_WIDGET_OPACITY_PERCENT = intPreferencesKey("widget_opacity_percent")
    val KEY_WIDGET_DOT_TEXTURE = booleanPreferencesKey("widget_dot_texture")
    val KEY_WIDGET_ACCOUNT_ID = stringPreferencesKey("widget_account_id")
    val KEY_WIDGET_INSTANCE_CONFIG = stringPreferencesKey("widget_instance_config")
    val KEY_WIDGET_CONFIGURED_ENTRIES = stringSetPreferencesKey("widget_configured_entries")
    val KEY_WIDGET_MONTH_OFFSET = intPreferencesKey("widget_month_offset")
    val KEY_WIDGET_SYSTEM_DARK = booleanPreferencesKey("widget_system_dark")
    val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    val KEY_APP_LOCK_PIN_SALT = stringPreferencesKey("app_lock_pin_salt")
    val KEY_APP_LOCK_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
    val KEY_PRIVATE_VAULT_EVENT_IDS = stringPreferencesKey("private_vault_event_ids")
    val KEY_FREE_TIME_START_HOUR = intPreferencesKey("free_time_start_hour")
    val KEY_FREE_TIME_END_HOUR = intPreferencesKey("free_time_end_hour")
    val KEY_AUTO_BUFFER_BEFORE_MINUTES = intPreferencesKey("auto_buffer_before_minutes")
    val KEY_AUTO_BUFFER_AFTER_MINUTES = intPreferencesKey("auto_buffer_after_minutes")
    val KEY_ON_THIS_DAY_DISMISSED_DATE = stringPreferencesKey("on_this_day_dismissed_date")
    val KEY_LAST_SHIFT_TYPE_ID = stringPreferencesKey("last_shift_type_id")
    val KEY_LAST_SHIFT_ACCOUNT_ID = stringPreferencesKey("last_shift_account_id")
    val KEY_REMINDER_SOUND_URI = stringPreferencesKey("reminder_sound_uri")
    val KEY_REMINDER_REPEAT_ENABLED = booleanPreferencesKey("reminder_repeat_enabled")
    val KEY_REMINDER_REPEAT_MINUTES = intPreferencesKey("reminder_repeat_minutes")
    val KEY_REMINDER_VIBRATION_ENABLED = booleanPreferencesKey("reminder_vibration_enabled")
    val KEY_REMINDER_FULL_SCREEN_ENABLED = booleanPreferencesKey("reminder_full_screen_enabled")
    val KEY_SMART_QUICK_ADD_CONTEXT_EVENT_ID = stringPreferencesKey("smart_quick_add_context_event_id")
    val KEY_REVIEW_SESSION_COUNT = intPreferencesKey("review_session_count")
    val KEY_REVIEW_CREATED_ITEM_COUNT = intPreferencesKey("review_created_item_count")
    val KEY_REVIEW_MEANINGFUL_ACTION_COUNT = intPreferencesKey("review_meaningful_action_count")
    val KEY_REVIEW_LAST_PROMPT_MS = longPreferencesKey("review_last_prompt_ms")
}
