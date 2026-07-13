package com.dotfield.dotcal.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.countdown.CountdownPinStore
import com.dotfield.dotcal.data.baseEventId
import java.io.File
import java.time.ZoneId
import java.util.Locale

object CardImageExporter {
    fun createCountdownShareUri(
        context: Context,
        event: CalendarEvent,
        accentColor: Int,
        darkTheme: Boolean,
        nowMs: Long = System.currentTimeMillis(),
    ): Uri {
        val bitmap = renderCountdownCard(event, accentColor, darkTheme, nowMs)
        val shareDir = File(context.cacheDir, "shared_events").apply { mkdirs() }
        val file = File(shareDir, "countdown-${event.baseEventId().safeShareFilename()}.png")
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun renderCountdownCard(
        event: CalendarEvent,
        accentColor: Int,
        darkTheme: Boolean,
        nowMs: Long,
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bg = if (darkTheme) Color.rgb(11, 11, 13) else Color.rgb(250, 250, 250)
        val surface = if (darkTheme) Color.rgb(18, 18, 22) else Color.WHITE
        val text = if (darkTheme) Color.WHITE else Color.rgb(17, 17, 17)
        val secondary = if (darkTheme) Color.rgb(156, 163, 175) else Color.rgb(107, 114, 128)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(bg)
        paint.color = surface
        canvas.drawRoundRect(RectF(96f, 150f, 984f, 1120f), 56f, 56f, paint)

        val zoneId = runCatching { ZoneId.of(event.timeZone) }.getOrDefault(ZoneId.systemDefault())
        val days = CountdownPinStore.daysUntil(event.startTimeMs, zoneId, nowMs).toString()
        drawDotDigits(canvas, days, accentColor, 180f, 300f, 720f, 360f)

        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.color = text
        paint.textSize = 54f
        val title = "DAYS UNTIL ${event.title.uppercase(Locale.getDefault())}"
        drawCenteredWrappedText(canvas, title, paint, 540f, 760f, 760f, 2)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = secondary
        paint.textSize = 34f
        canvas.drawText("Count down to what matters.", 540f, 1010f, paint)

        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.color = text
        paint.textSize = 38f
        canvas.drawText("DotCal", 540f, 1210f, paint)
        return bitmap
    }

    private fun drawDotDigits(canvas: Canvas, text: String, color: Int, x: Float, y: Float, width: Float, height: Float) {
        val patterns = text.map { digitPatterns[it] ?: digitPatterns.getValue('0') }
        val columns = patterns.sumOf { it.first().length } + (patterns.size - 1).coerceAtLeast(0)
        val rows = 7
        val gap = 14f
        val dot = minOf((width - gap * (columns - 1)) / columns, (height - gap * (rows - 1)) / rows)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val totalWidth = columns * dot + (columns - 1) * gap
        var xCursor = x + (width - totalWidth) / 2f
        val yStart = y + (height - (rows * dot + (rows - 1) * gap)) / 2f
        patterns.forEach { pattern ->
            pattern.forEachIndexed { row, line ->
                line.forEachIndexed { column, mark ->
                    if (mark == '1') {
                        canvas.drawCircle(xCursor + column * (dot + gap) + dot / 2f, yStart + row * (dot + gap) + dot / 2f, dot / 2f, paint)
                    }
                }
            }
            xCursor += pattern.first().length * (dot + gap)
        }
    }

    private fun drawCenteredWrappedText(canvas: Canvas, text: String, paint: Paint, centerX: Float, y: Float, maxWidth: Float, maxLines: Int) {
        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = ""
        words.forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth || current.isBlank()) current = candidate else {
                lines += current
                current = word
            }
        }
        if (current.isNotBlank()) lines += current
        lines.take(maxLines).forEachIndexed { index, line ->
            canvas.drawText(line, centerX, y + index * 62f, paint)
        }
    }

    private fun String.safeShareFilename(): String {
        return lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-").trim('-').take(48).ifBlank { "event" }
    }

    private val digitPatterns = mapOf(
        '0' to listOf("111", "101", "101", "101", "101", "101", "111"),
        '1' to listOf("010", "110", "010", "010", "010", "010", "111"),
        '2' to listOf("111", "001", "001", "111", "100", "100", "111"),
        '3' to listOf("111", "001", "001", "111", "001", "001", "111"),
        '4' to listOf("101", "101", "101", "111", "001", "001", "001"),
        '5' to listOf("111", "100", "100", "111", "001", "001", "111"),
        '6' to listOf("111", "100", "100", "111", "101", "101", "111"),
        '7' to listOf("111", "001", "001", "010", "010", "010", "010"),
        '8' to listOf("111", "101", "101", "111", "101", "101", "111"),
        '9' to listOf("111", "101", "101", "111", "001", "001", "111"),
    )
}
