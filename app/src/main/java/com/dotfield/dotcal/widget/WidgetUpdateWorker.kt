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
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        suspend fun updateNow(context: Context) {
            val appContext = context.applicationContext
            updateWidget(appContext, SmallDotCalWidget(), SmallDotCalWidget::class.java)
            updateWidget(appContext, MediumDotCalWidget(), MediumDotCalWidget::class.java)
            updateWidget(appContext, LargeDotCalWidget(), LargeDotCalWidget::class.java)
            updateWidget(appContext, EventCountdownDotCalWidget(), EventCountdownDotCalWidget::class.java)
            updateWidget(appContext, AgendaListDotCalWidget(), AgendaListDotCalWidget::class.java)
            updateWidget(appContext, MonthGridDotCalWidget(), MonthGridDotCalWidget::class.java)
            notifyWidgetHosts(appContext)
        }

        private suspend fun updateWidget(
            context: Context,
            widget: GlanceAppWidget,
            widgetClass: Class<out GlanceAppWidget>,
        ) {
            GlanceAppWidgetManager(context).getGlanceIds(widgetClass).forEach { glanceId ->
                syncDotCalWidgetState(context, glanceId)
                widget.update(context, glanceId)
            }
        }

        private fun notifyWidgetHosts(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            listOf(
                SmallDotCalWidgetReceiver::class.java,
                MediumDotCalWidgetReceiver::class.java,
                LargeDotCalWidgetReceiver::class.java,
                EventCountdownWidgetReceiver::class.java,
                AgendaListWidgetReceiver::class.java,
                MonthGridWidgetReceiver::class.java,
            ).forEach { receiverClass ->
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
}
