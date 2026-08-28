package com.dotfield.dotcal.reminders

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.dotfield.dotcal.prefs.CalendarPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderNotificationSettingsTest {
    @Test
    fun freeEntitlementDisablesAdvancedReminderOverrides() {
        val preferences = mutablePreferencesOf(
            CalendarPreferences.KEY_REMINDER_REPEAT_ENABLED to true,
            CalendarPreferences.KEY_REMINDER_VIBRATION_ENABLED to false,
            CalendarPreferences.KEY_REMINDER_FULL_SCREEN_ENABLED to true,
            CalendarPreferences.KEY_REMINDER_SOUND_URI to ReminderNotificationActions.SILENT_SOUND_VALUE,
        )

        val settings = ReminderNotificationSettings.from(preferences)

        assertFalse(settings.repeatEnabled)
        assertTrue(settings.vibrationEnabled)
        assertFalse(settings.fullScreenEnabled)
    }

    @Test
    fun proEntitlementKeepsAdvancedReminderOverrides() {
        val preferences = mutablePreferencesOf(
            CalendarPreferences.KEY_IS_PRO to true,
            CalendarPreferences.KEY_REMINDER_REPEAT_ENABLED to true,
            CalendarPreferences.KEY_REMINDER_VIBRATION_ENABLED to false,
            CalendarPreferences.KEY_REMINDER_FULL_SCREEN_ENABLED to true,
            CalendarPreferences.KEY_REMINDER_SOUND_URI to ReminderNotificationActions.SILENT_SOUND_VALUE,
        )

        val settings = ReminderNotificationSettings.from(preferences)

        assertTrue(settings.repeatEnabled)
        assertFalse(settings.vibrationEnabled)
        assertTrue(settings.fullScreenEnabled)
        assertTrue(settings.soundUri == null)
    }
}
