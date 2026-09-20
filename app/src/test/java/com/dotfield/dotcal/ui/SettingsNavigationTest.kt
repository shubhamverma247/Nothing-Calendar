package com.dotfield.dotcal.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsNavigationTest {
    @Test
    fun reminderDefaultsReturnsToRemindersBeforeSettingsRoot() {
        val parent = SettingsScreen.ReminderDefaults.parentScreen()
        assertEquals(SettingsScreen.ReminderCenter, parent)
        assertEquals(SettingsScreen.Root, parent.parentScreen())
    }

    @Test
    fun accountAndCalendarSubpagesKeepTheirParents() {
        assertEquals(SettingsScreen.CalendarAccounts, SettingsScreen.AddAccount.parentScreen())
        assertEquals(SettingsScreen.CalendarPreferences, SettingsScreen.CalendarMenu.parentScreen())
        assertEquals(SettingsScreen.Root, SettingsScreen.Theme.parentScreen())
    }
}
