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
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

object CardImageExporter {
    fun createCalendarViewShareUri(
        context: Context,
        viewName: String,
        events: List<CalendarEvent>,
        viewDate: LocalDate,
        weekStart: DayOfWeek = DayOfWeek.MONDAY,
        accentColor: Int,
        darkTheme: Boolean,
    ): Uri {
        val bitmap = renderCalendarViewShareBitmap(context, viewName, events, viewDate, weekStart, accentColor, darkTheme)
        return writeCalendarViewShareUri(context, viewName, bitmap)
    }

    internal fun renderCalendarViewShareBitmap(
        context: Context,
        viewName: String,
        events: List<CalendarEvent>,
        viewDate: LocalDate,
        weekStart: DayOfWeek,
        accentColor: Int,
        darkTheme: Boolean,
    ): Bitmap = renderCalendarViewCard(
            viewName = viewName,
            events = events,
            accentColor = accentColor,
            darkTheme = darkTheme,
            brandLabel = context.getString(com.dotfield.dotcal.R.string.share_card_brand),
            emptyLabel = context.getString(com.dotfield.dotcal.R.string.share_card_empty),
            untitledLabel = context.getString(com.dotfield.dotcal.R.string.share_card_untitled),
            viewDate = viewDate,
            weekStart = weekStart,
        )

    internal fun writeCalendarViewShareUri(context: Context, viewName: String, bitmap: Bitmap): Uri {
        val shareDir = File(context.cacheDir, "shared_events").apply { mkdirs() }
        val file = File(shareDir, "dotcal-${viewName.lowercase(Locale.US)}-${UUID.randomUUID()}.png")
        try {
            val compressed = file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            check(compressed) { "Calendar view image compression failed" }
        } finally {
            bitmap.recycle()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    internal fun calendarViewLayout(viewName: String): String = when (viewName.lowercase(Locale.US)) {
        "month" -> "month"
        "week" -> "week"
        "agenda" -> "agenda"
        else -> "agenda"
    }

    private fun renderCalendarViewCard(
        viewName: String,
        events: List<CalendarEvent>,
        accentColor: Int,
        darkTheme: Boolean,
        brandLabel: String,
        emptyLabel: String,
        untitledLabel: String,
        viewDate: LocalDate,
        weekStart: DayOfWeek,
    ): Bitmap {
        val width = 1080
        val height = 1350
        val background = if (darkTheme) Color.rgb(11, 11, 13) else Color.rgb(250, 250, 250)
        val text = if (darkTheme) Color.WHITE else Color.rgb(17, 17, 17)
        val secondary = if (darkTheme) Color.rgb(156, 163, 175) else Color.rgb(107, 114, 128)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) }
        canvas.drawColor(background)
        paint.color = accentColor
        canvas.drawRect(72f, 86f, 1008f, 96f, paint)
        paint.color = text
        paint.textSize = 58f
        canvas.drawText("DOTCAL / ${viewName.uppercase(Locale.US)}", 72f, 190f, paint)
        paint.color = secondary
        paint.textSize = 30f
        canvas.drawText(brandLabel, 72f, 242f, paint)
        when (calendarViewLayout(viewName)) {
            "month" -> drawMonthView(canvas, viewDate, weekStart, events, accentColor, text, secondary, untitledLabel)
            "week" -> drawWeekView(canvas, viewDate, weekStart, events, accentColor, text, secondary, untitledLabel)
            else -> drawAgendaView(canvas, events, accentColor, text, secondary, untitledLabel)
        }
        if (events.isEmpty()) {
            paint.color = secondary
            paint.textSize = 32f
            canvas.drawText(emptyLabel, 72f, 390f, paint)
        }
        paint.color = text
        paint.textSize = 34f
        canvas.drawText("DotCal", 72f, 1275f, paint)
        return bitmap
    }

    private fun drawAgendaView(canvas: Canvas, events: List<CalendarEvent>, accentColor: Int, text: Int, secondary: Int, untitledLabel: String) {
        var y = 350f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) }
        events.take(12).forEach { event ->
            paint.color = accentColor; canvas.drawCircle(92f, y - 10f, 12f, paint)
            paint.color = text; paint.textSize = 36f
            canvas.drawText(event.title.ifBlank { untitledLabel }.take(30), 132f, y, paint)
            paint.color = secondary; paint.textSize = 25f
            canvas.drawText(event.shareCardDateLabel(), 132f, y + 42f, paint)
            y += 112f
        }
    }

    internal fun monthGridStart(date: LocalDate, weekStart: DayOfWeek = DayOfWeek.MONDAY): LocalDate {
        val offset = (date.withDayOfMonth(1).dayOfWeek.value - weekStart.value + 7) % 7
        return date.withDayOfMonth(1).minusDays(offset.toLong())
    }

    private fun drawMonthView(canvas: Canvas, viewDate: LocalDate, weekStart: DayOfWeek, events: List<CalendarEvent>, accentColor: Int, text: Int, secondary: Int, untitledLabel: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = secondary; paint.textSize = 24f; paint.typeface = Typeface.DEFAULT_BOLD
        val labels = (0..6).map { weekStart.plus(it.toLong()).name.take(1) }
        labels.forEachIndexed { index, label -> canvas.drawText(label, 92f + index * 132f, 350f, paint) }
        paint.color = text; paint.textSize = 22f
        val start = monthGridStart(viewDate, weekStart)
        val eventDates = events.map { eventDate(it) }.toSet()
        (0 until 42).forEach { index ->
            val day = start.plusDays(index.toLong()); val column = index % 7; val row = index / 7
            paint.color = if (day.month == viewDate.month) text else secondary
            canvas.drawText(day.dayOfMonth.toString(), 92f + column * 132f, 400f + row * 86f, paint)
            if (day in eventDates) { paint.color = accentColor; canvas.drawCircle(100f + column * 132f, 418f + row * 86f, 7f, paint) }
        }
        paint.color = secondary; paint.textSize = 22f
        canvas.drawText("${viewDate.month.name.take(3)} ${viewDate.year}  -  ${events.size} EVENTS", 72f, 930f, paint)
        canvas.drawText(events.take(3).joinToString("  -  ") { it.title.ifBlank { untitledLabel }.take(16) }.take(78), 72f, 970f, paint)
    }

    private fun drawWeekView(canvas: Canvas, viewDate: LocalDate, weekStart: DayOfWeek, events: List<CalendarEvent>, accentColor: Int, text: Int, secondary: Int, untitledLabel: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = secondary; paint.textSize = 22f; paint.typeface = Typeface.DEFAULT_BOLD
        val start = viewDate.minusDays((viewDate.dayOfWeek.value - weekStart.value + 7L) % 7L)
        (0..6).forEach { day ->
            val date = start.plusDays(day.toLong())
            canvas.drawText("${date.dayOfWeek.name.take(3)} ${date.dayOfMonth}", 70f + day * 140f, 350f, paint)
        }
        events.forEach { event ->
            val eventDateTime = eventDateTime(event)
            val column = (eventDateTime.toLocalDate().toEpochDay() - start.toEpochDay()).toInt()
            if (column !in 0..6) return@forEach
            val minutes = eventDateTime.hour * 60 + eventDateTime.minute
            val row = ((minutes - 7 * 60).coerceIn(0, 9 * 60) / 60)
            paint.color = accentColor; canvas.drawRoundRect(62f + column * 140f, 390f + row * 90f, 192f + column * 140f, 465f + row * 90f, 12f, 12f, paint)
            paint.color = text; paint.textSize = 18f
            canvas.drawText(event.title.ifBlank { untitledLabel }.take(12), 70f + column * 140f, 438f + row * 90f, paint)
        }
    }

    private fun eventDate(event: CalendarEvent): LocalDate = eventDateTime(event).toLocalDate()

    private fun eventDateTime(event: CalendarEvent) = java.time.Instant.ofEpochMilli(event.startTimeMs).atZone(
        runCatching { java.time.ZoneId.of(event.timeZone) }.getOrDefault(java.time.ZoneId.systemDefault()),
    )

    private fun CalendarEvent.shareCardDateLabel(): String {
        val zone = runCatching { java.time.ZoneId.of(timeZone) }.getOrDefault(java.time.ZoneId.systemDefault())
        val dateTime = java.time.Instant.ofEpochMilli(startTimeMs).atZone(zone)
        return if (isAllDay == 1) dateTime.toLocalDate().toString() else "${dateTime.toLocalDate()}  ${dateTime.toLocalTime().toString().take(5)}"
    }
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
