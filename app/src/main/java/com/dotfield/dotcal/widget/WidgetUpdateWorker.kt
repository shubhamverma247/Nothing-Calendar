package com.dotfield.dotcal.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        when (inputData.getString(WORK_ACTION)) {
            ACTION_UNREGISTER_WIDGETS -> {
                unregisterConfiguredWidgets(
                    applicationContext,
                    inputData.getIntArray(APP_WIDGET_IDS) ?: IntArray(0),
                )
                return Result.success()
            }
            ACTION_CLEAR_RECEIVER -> {
                val receiverClass = inputData.getString(RECEIVER_CLASS).orEmpty()
                if (receiverClass.isNotBlank()) {
                    clearConfiguredWidgetsForReceiver(applicationContext, receiverClass)
                }
                return Result.success()
            }
        }
        val configuredId = inputData.getInt(APP_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (configuredId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (AppWidgetManager.getInstance(applicationContext).getAppWidgetInfo(configuredId) == null) {
                return Result.success()
            }
            return runCatching {
                updateConfiguredWidgetNow(applicationContext, configuredId)
            }.fold(
                onSuccess = { Result.success() },
                onFailure = { if (runAttemptCount < 3) Result.retry() else Result.failure() },
            )
        }
        updateNow(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK = "dotcal_widget_update"
        private const val CONFIGURED_WIDGET_WORK_PREFIX = "dotcal_widget_configured_"
        private const val WIDGET_CLEANUP_WORK_PREFIX = "dotcal_widget_cleanup_"
        private const val WORK_ACTION = "work_action"
        private const val ACTION_UNREGISTER_WIDGETS = "unregister_widgets"
        private const val ACTION_CLEAR_RECEIVER = "clear_receiver"
        private const val APP_WIDGET_ID = "app_widget_id"
        private const val APP_WIDGET_IDS = "app_widget_ids"
        private const val RECEIVER_CLASS = "receiver_class"
        private const val CONFIGURED_WIDGET_ATTEMPT_COUNT = 3
        private val CONFIGURED_WIDGET_ATTEMPT_DELAYS = longArrayOf(750L, 3_000L, 10_000L)

        fun enqueue(context: Context) {
            // KEEP (not REPLACE): several receivers enqueue on the same trigger; REPLACE would
            // cancel and restart an already-running update, delaying widget refreshes.
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }

        fun enqueueConfiguredWidgetUpdate(context: Context, appWidgetId: Int) {
            val workManager = WorkManager.getInstance(context.applicationContext)
            repeat(CONFIGURED_WIDGET_ATTEMPT_COUNT) { attempt ->
                val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                    .setInitialDelay(CONFIGURED_WIDGET_ATTEMPT_DELAYS[attempt], TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(APP_WIDGET_ID to appWidgetId))
                    .build()
                workManager.enqueueUniqueWork(
                    "$CONFIGURED_WIDGET_WORK_PREFIX$appWidgetId-$attempt",
                    ExistingWorkPolicy.REPLACE,
                    request,
                )
            }
        }

        fun enqueueDeletedWidgets(context: Context, appWidgetIds: IntArray) {
            if (appWidgetIds.isEmpty()) return
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setInputData(
                    workDataOf(
                        WORK_ACTION to ACTION_UNREGISTER_WIDGETS,
                        APP_WIDGET_IDS to appWidgetIds,
                    ),
                )
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "$WIDGET_CLEANUP_WORK_PREFIX${appWidgetIds.joinToString("-")}",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun enqueueClearReceiver(context: Context, receiverClass: String) {
            if (receiverClass.isBlank()) return
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setInputData(
                    workDataOf(
                        WORK_ACTION to ACTION_CLEAR_RECEIVER,
                        RECEIVER_CLASS to receiverClass,
                    ),
                )
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "$WIDGET_CLEANUP_WORK_PREFIX$receiverClass",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        suspend fun updateNow(context: Context) {
            val appContext = context.applicationContext
            updateWidget(appContext, DateOnlyDotCalWidget(), DateOnlyDotCalWidget::class.java, LegacyWidgetKind.DateOnly)
            updateWidget(appContext, CompactMonthDotCalWidget(), CompactMonthDotCalWidget::class.java, LegacyWidgetKind.MonthCompact)
            updateWidget(appContext, ShiftWideDotCalWidget(), ShiftWideDotCalWidget::class.java, LegacyWidgetKind.ShiftWide)
            updateWidget(appContext, SmallDotCalWidget(), SmallDotCalWidget::class.java, LegacyWidgetKind.Small)
            updateWidget(appContext, MediumDotCalWidget(), MediumDotCalWidget::class.java, LegacyWidgetKind.Medium)
            updateWidget(appContext, LargeDotCalWidget(), LargeDotCalWidget::class.java, LegacyWidgetKind.Large)
            updateWidget(appContext, EventCountdownDotCalWidget(), EventCountdownDotCalWidget::class.java, LegacyWidgetKind.Countdown)
            updateWidget(appContext, AgendaListDotCalWidget(), AgendaListDotCalWidget::class.java, LegacyWidgetKind.Agenda)
            updateWidget(appContext, MonthGridDotCalWidget(), MonthGridDotCalWidget::class.java, LegacyWidgetKind.MonthGrid)
            notifyWidgetHosts(appContext)
        }

        suspend fun updateCompactMonthNow(context: Context) {
            val appContext = context.applicationContext
            updateWidget(appContext, CompactMonthDotCalWidget(), CompactMonthDotCalWidget::class.java, LegacyWidgetKind.MonthCompact)
            notifyWidgetHost(appContext, CompactMonthDotCalWidgetReceiver::class.java)
        }

        /**
         * Refresh the widget that was just configured instead of waiting for a provider-wide
         * enumeration. During the launcher configure flow the new app-widget id can be visible
         * to the host before it appears in getGlanceIds(), which otherwise leaves the saved
         * config rendered only after the next host refresh.
         */
        suspend fun updateConfiguredWidgetNow(context: Context, appWidgetId: Int) {
            val appContext = context.applicationContext
            val kind = legacyKindForAppWidget(appWidgetId, appContext)
            val widget = widgetForKind(kind)
            val glanceId = GlanceAppWidgetManager(appContext).getGlanceIdBy(appWidgetId)
            registerConfiguredWidget(
                appContext,
                receiverClassNameForWidgetKind(kind),
                appWidgetId,
            )
            syncDotCalWidgetState(appContext, glanceId, kind)
            widget.update(appContext, glanceId)
            notifyWidgetHost(appContext, receiverClassForWidgetKind(kind))
        }

        private suspend fun updateWidget(
            context: Context,
            widget: GlanceAppWidget,
            widgetClass: Class<out GlanceAppWidget>,
            legacyKind: LegacyWidgetKind,
        ) {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(widgetClass).forEach { glanceId ->
                registerConfiguredWidget(
                    context,
                    receiverClassNameForWidgetKind(legacyKind),
                    manager.getAppWidgetId(glanceId),
                )
                syncDotCalWidgetState(context, glanceId, legacyKind)
                widget.update(context, glanceId)
            }
        }

        private fun widgetForKind(kind: LegacyWidgetKind): GlanceAppWidget = when (kind) {
            LegacyWidgetKind.DateOnly -> DateOnlyDotCalWidget()
            LegacyWidgetKind.MonthCompact -> CompactMonthDotCalWidget()
            LegacyWidgetKind.ShiftWide -> ShiftWideDotCalWidget()
            LegacyWidgetKind.Small -> SmallDotCalWidget()
            LegacyWidgetKind.Medium -> MediumDotCalWidget()
            LegacyWidgetKind.Large -> LargeDotCalWidget()
            LegacyWidgetKind.Countdown -> EventCountdownDotCalWidget()
            LegacyWidgetKind.Agenda -> AgendaListDotCalWidget()
            LegacyWidgetKind.MonthGrid -> MonthGridDotCalWidget()
        }

        private fun receiverClassForWidgetKind(kind: LegacyWidgetKind): Class<*> = when (kind) {
            LegacyWidgetKind.DateOnly -> DateOnlyDotCalWidgetReceiver::class.java
            LegacyWidgetKind.MonthCompact -> CompactMonthDotCalWidgetReceiver::class.java
            LegacyWidgetKind.ShiftWide -> ShiftWideWidgetReceiver::class.java
            LegacyWidgetKind.Small -> SmallDotCalWidgetReceiver::class.java
            LegacyWidgetKind.Medium -> MediumDotCalWidgetReceiver::class.java
            LegacyWidgetKind.Large -> LargeDotCalWidgetReceiver::class.java
            LegacyWidgetKind.Countdown -> EventCountdownWidgetReceiver::class.java
            LegacyWidgetKind.Agenda -> AgendaListWidgetReceiver::class.java
            LegacyWidgetKind.MonthGrid -> MonthGridWidgetReceiver::class.java
        }

        private fun notifyWidgetHosts(context: Context) {
            listOf(
                DateOnlyDotCalWidgetReceiver::class.java,
                CompactMonthDotCalWidgetReceiver::class.java,
                ShiftWideWidgetReceiver::class.java,
                SmallDotCalWidgetReceiver::class.java,
                MediumDotCalWidgetReceiver::class.java,
                LargeDotCalWidgetReceiver::class.java,
                EventCountdownWidgetReceiver::class.java,
                AgendaListWidgetReceiver::class.java,
                MonthGridWidgetReceiver::class.java,
            ).forEach { receiverClass ->
                notifyWidgetHost(context, receiverClass)
            }
        }

        private fun notifyWidgetHost(context: Context, receiverClass: Class<*>) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, receiverClass)
            val ids = appWidgetManager.getAppWidgetIds(provider)
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .setComponent(provider)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids),
                )
            }
        }
    }
}
