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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager

class WidgetUpdateWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
            updateNow(applicationContext)
            return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK = "dotcal_widget_update"

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
