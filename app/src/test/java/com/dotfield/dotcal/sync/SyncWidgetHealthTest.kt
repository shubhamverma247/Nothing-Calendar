package com.dotfield.dotcal.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncWidgetHealthTest {
    @Test
    fun `missing calendar permission requires action`() {
        val health = syncWidgetHealth(
            SyncWidgetHealthSnapshot(
                syncEnabled = true,
                hasCalendarPermission = false,
                lastSyncMs = 10_000L,
                syncErrorMessage = null,
                widgetCount = 1,
                lastWidgetRefreshMs = 10_000L,
                nowMs = 20_000L,
            ),
        )

        assertEquals(SyncWidgetHealthStatus.ActionRequired, health.status)
    }

    @Test
    fun `sync failure needs attention`() {
        val health = syncWidgetHealth(
            SyncWidgetHealthSnapshot(
                syncEnabled = true,
                hasCalendarPermission = true,
                lastSyncMs = 10_000L,
                syncErrorMessage = "Provider unavailable",
                widgetCount = 0,
                lastWidgetRefreshMs = null,
                nowMs = 20_000L,
            ),
        )

        assertEquals(SyncWidgetHealthStatus.NeedsAttention, health.status)
    }

    @Test
    fun `enabled sync without a successful run needs attention`() {
        val health = syncWidgetHealth(
            SyncWidgetHealthSnapshot(
                syncEnabled = true,
                hasCalendarPermission = true,
                lastSyncMs = null,
                syncErrorMessage = null,
                widgetCount = 0,
                lastWidgetRefreshMs = null,
                nowMs = 20_000L,
            ),
        )

        assertEquals(SyncWidgetHealthStatus.NeedsAttention, health.status)
    }

    @Test
    fun `configured widget without recent refresh needs attention`() {
        val health = syncWidgetHealth(
            SyncWidgetHealthSnapshot(
                syncEnabled = false,
                hasCalendarPermission = true,
                lastSyncMs = null,
                syncErrorMessage = null,
                widgetCount = 1,
                lastWidgetRefreshMs = 0L,
                nowMs = 6L * 60L * 60L * 1000L + 1L,
            ),
        )

        assertEquals(SyncWidgetHealthStatus.NeedsAttention, health.status)
    }

    @Test
    fun `recent sync and widget refresh are healthy`() {
        val health = syncWidgetHealth(
            SyncWidgetHealthSnapshot(
                syncEnabled = true,
                hasCalendarPermission = true,
                lastSyncMs = 10_000L,
                syncErrorMessage = null,
                widgetCount = 2,
                lastWidgetRefreshMs = 10_000L,
                nowMs = 20_000L,
            ),
        )

        assertEquals(SyncWidgetHealthStatus.Healthy, health.status)
    }

    @Test
    fun `widget refresh failure needs attention even when previous refresh was recent`() {
        val health = syncWidgetHealth(
            SyncWidgetHealthSnapshot(
                syncEnabled = false,
                hasCalendarPermission = true,
                lastSyncMs = null,
                syncErrorMessage = null,
                widgetCount = 1,
                lastWidgetRefreshMs = 19_000L,
                widgetErrorMessage = "WIDGET_REFRESH_FAILED",
                nowMs = 20_000L,
            ),
        )

        assertEquals(SyncWidgetHealthStatus.NeedsAttention, health.status)
    }
}
