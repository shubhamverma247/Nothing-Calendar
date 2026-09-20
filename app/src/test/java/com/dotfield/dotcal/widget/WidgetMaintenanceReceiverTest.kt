package com.dotfield.dotcal.widget

import android.content.Intent
import com.dotfield.dotcal.launcher.ACTION_DAILY_ICON_REFRESH
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetMaintenanceReceiverTest {
    @Test
    fun mapsConfigurationActionToAsyncIconRefresh() {
        assertEquals(
            WidgetMaintenanceAction.CONFIGURATION,
            widgetMaintenanceActionFor(Intent.ACTION_CONFIGURATION_CHANGED),
        )
    }

    @Test
    fun mapsSystemClockActionsToAsyncIconRefresh() {
        listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
        ).forEach { action ->
            assertEquals(WidgetMaintenanceAction.STANDARD, widgetMaintenanceActionFor(action))
        }
    }

    @Test
    fun mapsScheduledDailyIconRefreshToAsyncIconRefresh() {
        assertEquals(
            WidgetMaintenanceAction.DAILY_ICON,
            widgetMaintenanceActionFor(ACTION_DAILY_ICON_REFRESH),
        )
    }

    @Test
    fun ignoresUnrelatedActions() {
        assertEquals(WidgetMaintenanceAction.NONE, widgetMaintenanceActionFor(Intent.ACTION_AIRPLANE_MODE_CHANGED))
    }
}
