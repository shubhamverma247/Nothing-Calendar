package com.dotfield.dotcal.glyph

import android.content.Context
import android.content.Intent

/** Sends best-effort lifecycle updates to the optional Nothing Glyph toy. */
object DotCalGlyphBridge {
    const val ACTION_EVENT_SNOOZED = "com.dotfield.dotcal.action.GLYPH_EVENT_SNOOZED"
    const val ACTION_EVENT_CLEARED = "com.dotfield.dotcal.action.GLYPH_EVENT_CLEARED"
    const val EXTRA_EVENT_ID = "event_id"
    const val EXTRA_UNTIL_MS = "until_ms"

    fun eventSnoozed(context: Context, eventId: String, untilMs: Long) {
        send(context, ACTION_EVENT_SNOOZED) {
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_UNTIL_MS, untilMs)
        }
    }

    fun eventOpened(context: Context, eventId: String) = clear(context, eventId)
    fun taskCompleted(context: Context, eventId: String) = clear(context, eventId)
    fun reminderExpired(context: Context, eventId: String) = clear(context, eventId)

    private fun clear(context: Context, eventId: String) {
        send(context, ACTION_EVENT_CLEARED) { putExtra(EXTRA_EVENT_ID, eventId) }
    }

    private fun send(context: Context, action: String, fill: Intent.() -> Unit) {
        context.sendBroadcast(Intent(action).setPackage(context.packageName).apply(fill))
    }
}
