package com.dotfield.dotcal.sync

import java.util.concurrent.TimeUnit

internal enum class SyncWidgetHealthStatus {
    Healthy,
    NeedsAttention,
    ActionRequired,
}

internal data class SyncWidgetHealthSnapshot(
    val syncEnabled: Boolean,
    val hasCalendarPermission: Boolean,
    val lastSyncMs: Long?,
    val syncErrorMessage: String?,
    val widgetCount: Int,
    val lastWidgetRefreshMs: Long?,
    val widgetErrorMessage: String? = null,
    val nowMs: Long,
)

internal data class SyncWidgetHealth(
    val status: SyncWidgetHealthStatus,
)

internal fun syncWidgetHealth(snapshot: SyncWidgetHealthSnapshot): SyncWidgetHealth {
    if (!snapshot.hasCalendarPermission && snapshot.syncEnabled) {
        return SyncWidgetHealth(SyncWidgetHealthStatus.ActionRequired)
    }
    if (!snapshot.syncErrorMessage.isNullOrBlank()) {
        return SyncWidgetHealth(SyncWidgetHealthStatus.NeedsAttention)
    }
    if (!snapshot.widgetErrorMessage.isNullOrBlank()) {
        return SyncWidgetHealth(SyncWidgetHealthStatus.NeedsAttention)
    }
    if (snapshot.syncEnabled && snapshot.lastSyncMs == null) {
        return SyncWidgetHealth(SyncWidgetHealthStatus.NeedsAttention)
    }
    val widgetIsStale = snapshot.widgetCount > 0 &&
        (snapshot.lastWidgetRefreshMs == null ||
            snapshot.nowMs - snapshot.lastWidgetRefreshMs >= WIDGET_STALE_AFTER_MS)
    if (widgetIsStale) {
        return SyncWidgetHealth(SyncWidgetHealthStatus.NeedsAttention)
    }
    return SyncWidgetHealth(SyncWidgetHealthStatus.Healthy)
}

private val WIDGET_STALE_AFTER_MS = TimeUnit.HOURS.toMillis(6)
