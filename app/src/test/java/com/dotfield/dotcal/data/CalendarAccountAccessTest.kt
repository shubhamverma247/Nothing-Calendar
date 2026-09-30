package com.dotfield.dotcal.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarAccountAccessTest {
    @Test
    fun writableDestinationRequiresContributorOrHigherAccess() {
        assertTrue(account(accessLevel = CALENDAR_ACCESS_OWNER).isWritableDestination())
        assertTrue(account(accessLevel = CALENDAR_ACCESS_EDITOR).isWritableDestination())
        assertTrue(account(accessLevel = CALENDAR_ACCESS_CONTRIBUTOR).isWritableDestination())

        assertFalse(account(accessLevel = CALENDAR_ACCESS_READ).isWritableDestination())
        assertFalse(account(accessLevel = CALENDAR_ACCESS_FREEBUSY).isWritableDestination())
        assertFalse(account(accessLevel = CALENDAR_ACCESS_NONE).isWritableDestination())
    }

    private fun account(accessLevel: Int) = CalendarAccount(
        id = "provider-calendar-1",
        accountName = "user@example.com",
        displayName = "User",
        accountType = "GOOGLE",
        color = "#3366FF",
        isVisible = 1,
        isPrimary = 0,
        sortOrder = 1,
        accessLevel = accessLevel,
    )
}
