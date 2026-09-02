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

    @Test
    fun fullScreenReminderRequiresProPreferenceAndSystemAccess() {
        assertFalse(shouldUseFullScreenReminder(isProEnabled = false, preferenceEnabled = true, canUseFullScreenIntent = true))
        assertFalse(shouldUseFullScreenReminder(isProEnabled = true, preferenceEnabled = false, canUseFullScreenIntent = true))
        assertFalse(shouldUseFullScreenReminder(isProEnabled = true, preferenceEnabled = true, canUseFullScreenIntent = false))
        assertTrue(shouldUseFullScreenReminder(isProEnabled = true, preferenceEnabled = true, canUseFullScreenIntent = true))
    }

    @Test
    fun strongAlertSettingsOpenOnlyWhenAndroid14PlusPermissionIsDenied() {
        assertFalse(shouldOpenFullScreenReminderSettings(isEnabling = false, sdkInt = 34, canUseFullScreenIntent = false))
        assertFalse(shouldOpenFullScreenReminderSettings(isEnabling = true, sdkInt = 33, canUseFullScreenIntent = false))
        assertFalse(shouldOpenFullScreenReminderSettings(isEnabling = true, sdkInt = 34, canUseFullScreenIntent = true))
        assertTrue(shouldOpenFullScreenReminderSettings(isEnabling = true, sdkInt = 34, canUseFullScreenIntent = false))
        assertTrue(shouldOpenFullScreenReminderSettings(isEnabling = true, sdkInt = 36, canUseFullScreenIntent = false))
    }
}
