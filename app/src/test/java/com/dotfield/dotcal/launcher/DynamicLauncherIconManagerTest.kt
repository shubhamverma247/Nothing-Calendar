package com.dotfield.dotcal.launcher

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicLauncherIconManagerTest {
    @Test
    fun mapsSupportedDaysToZeroPaddedAliases() {
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay01", launcherAliasClassName(1))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay02", launcherAliasClassName(2))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay08", launcherAliasClassName(8))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay09", launcherAliasClassName(9))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay10", launcherAliasClassName(10))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay28", launcherAliasClassName(28))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay29", launcherAliasClassName(29))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay30", launcherAliasClassName(30))
        assertEquals("com.dotfield.dotcal.launcher.LauncherDay31", launcherAliasClassName(31))
    }

    @Test
    fun rejectsInvalidDayNumbers() {
        assertNull(launcherAliasClassName(0))
        assertNull(launcherAliasClassName(32))
        assertNull(launcherAliasClassName(-1))
    }

    @Test
    fun exposesExactlyOneAliasForEachCalendarDay() {
        val aliases = launcherAliasClassNames()

        assertEquals(31, aliases.size)
        assertEquals(31, aliases.toSet().size)
        assertTrue(aliases.first().endsWith("LauncherDay01"))
        assertTrue(aliases.last().endsWith("LauncherDay31"))
    }

    @Test
    fun resolvesLocalDayWithoutMonthOrTimezoneState() {
        assertEquals(8, localDayOfMonth(LocalDate.of(2026, 9, 8)))
        assertEquals(31, localDayOfMonth(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun schedulesNextRefreshAtNextLocalMidnight() {
        val now = ZonedDateTime.of(2026, 9, 9, 0, 46, 57, 0, ZoneId.of("Asia/Kolkata"))

        assertEquals(
            ZonedDateTime.of(2026, 9, 10, 0, 0, 0, 0, ZoneId.of("Asia/Kolkata")),
            nextLauncherIconRefreshAt(now),
        )
    }

}
