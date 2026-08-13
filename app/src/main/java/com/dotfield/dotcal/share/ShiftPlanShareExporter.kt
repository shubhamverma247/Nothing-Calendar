package com.dotfield.dotcal.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.ics.IcsExporter
import com.dotfield.dotcal.data.qr.QrEventPayloadCodec
import com.dotfield.dotcal.data.shifts.SHIFT_PLAN_QR_EVENT_LIMIT
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ShiftPlanShareExporter {
    fun createIcsUri(context: Context, planTitle: String, events: List<CalendarEvent>): Uri {
        require(events.isNotEmpty()) { "No shifts to share" }
        val file = shareFile(context, planTitle, "ics")
        file.writeText(IcsExporter.export(events), Charsets.UTF_8)
        return file.uri(context)
    }

    fun createImageUri(
        context: Context,
        planTitle: String,
        events: List<CalendarEvent>,
        accentColor: Int,
        darkTheme: Boolean,
    ): Uri {
        require(events.isNotEmpty()) { "No shifts to share" }
        val bitmap = renderPlanCard(planTitle, events, accentColor, darkTheme)
        val file = shareFile(context, planTitle, "png")
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return file.uri(context)
    }

    fun createPdfUri(
        context: Context,
        planTitle: String,
        events: List<CalendarEvent>,
        accentColor: Int,
        darkTheme: Boolean,
    ): Uri {
        require(events.isNotEmpty()) { "No shifts to share" }
        val file = shareFile(context, planTitle, "pdf")
        val pdf = renderPlanPdf(planTitle, events, accentColor, darkTheme)
        try {
            file.outputStream().use(pdf::writeTo)
        } finally {
            pdf.close()
        }
        return file.uri(context)
    }

    fun createQrImageUri(
        context: Context,
        planTitle: String,
        events: List<CalendarEvent>,
        accentColor: Int,
        darkTheme: Boolean,
    ): Uri {
        require(events.isNotEmpty()) { "No shifts to share" }
        require(events.size <= SHIFT_PLAN_QR_EVENT_LIMIT) { "QR shift limit exceeded" }
        val payload = QrEventPayloadCodec.encode(IcsExporter.export(events)).payload
        val bitmap = renderQrCard(planTitle, events, payload, accentColor, darkTheme)
        val file = shareFile(context, "$planTitle QR", "png")
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return file.uri(context)
    }

    private fun renderPlanCard(
        planTitle: String,
        events: List<CalendarEvent>,
        accentColor: Int,
        darkTheme: Boolean,
    ): Bitmap {
        val width = 1080
        val rowCount = events.size.coerceAtMost(14)
        val rowHeight = 76
        val moreHeight = if (events.size > rowCount) 78 else 0
        val height = 342 + rowCount * rowHeight + moreHeight + 130
        val colors = shareColors(darkTheme)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(colors.background)
        drawHeader(canvas, paint, planTitle, events, accentColor, colors, 96f, 118f, 888f)
        var y = 306f
        events.take(rowCount).forEach { event ->
            drawEventRow(canvas, paint, event, colors, accentColor, 96f, y, 888f)
            y += rowHeight
        }
        if (events.size > rowCount) {
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textSize = 28f
            paint.color = colors.secondary
            drawFittedText(canvas, "+${events.size - rowCount} more shifts", 126f, y + 38f, paint, 828f)
        }
        drawFooter(canvas, paint, colors, width / 2f, height - 70f, 1f)
        return bitmap
    }

    private fun renderQrCard(
        planTitle: String,
        events: List<CalendarEvent>,
        payload: String,
        accentColor: Int,
        darkTheme: Boolean,
    ): Bitmap {
        val width = 1080
        val height = 1420
        val colors = shareColors(darkTheme)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(colors.background)
        drawHeader(canvas, paint, planTitle, events, accentColor, colors, 96f, 120f, 888f)
        val qr = QrEventImageExporter.createQrBitmap(payload, size = 760)
        paint.color = colors.surface
        canvas.drawRoundRect(RectF(96f, 326f, 984f, 1282f), 56f, 56f, paint)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 25f
        paint.color = colors.text
        drawFittedText(canvas, "DotCal QR", 140f, 398f, paint, 800f)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 22f
        paint.color = colors.secondary
        drawFittedText(canvas, "Scan to import this shift plan", 140f, 434f, paint, 800f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(140f, 454f, 940f, 1254f), 44f, 44f, paint)
        canvas.drawBitmap(qr, 160f, 474f, paint)
        drawFooter(canvas, paint, colors, width / 2f, 1350f, 1f)
        return bitmap
    }

    private fun renderPlanPdf(
        planTitle: String,
        events: List<CalendarEvent>,
        accentColor: Int,
        darkTheme: Boolean,
    ): PdfDocument {
        val colors = shareColors(darkTheme)
        val document = PdfDocument()
        val rowsPerPage = 9
        events.chunked(rowsPerPage).forEachIndexed { pageIndex, pageEvents ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageIndex + 1).create())
            val canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            canvas.drawColor(colors.background)
            drawHeader(canvas, paint, planTitle, events, accentColor, colors, 48f, 72f, 499f)
            var y = 224f
            pageEvents.forEach { event ->
                drawEventRow(canvas, paint, event, colors, accentColor, 48f, y, 499f)
                y += 58f
            }
            drawFooter(canvas, paint, colors, 297.5f, 794f, 0.64f)
            document.finishPage(page)
        }
        return document
    }

    private fun drawHeader(
        canvas: Canvas,
        paint: Paint,
        planTitle: String,
        events: List<CalendarEvent>,
        accentColor: Int,
        colors: ShareColors,
        left: Float,
        top: Float,
        width: Float,
    ) {
        val scale = (width / 888f).coerceIn(0.55f, 1f)
        paint.color = colors.surface
        canvas.drawRoundRect(RectF(left, top, left + width, top + 132f), 36f, 36f, paint)
        paint.color = accentColor
        canvas.drawCircle(left + 40f * scale, top + 42f, 12f * scale, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 38f * scale
        paint.color = colors.text
        drawFittedText(canvas, planTitle, left + 70f * scale, top + 56f, paint, width - 90f * scale)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 24f * scale
        paint.color = colors.secondary
        drawFittedText(canvas, shareRangeLine(events), left + 70f * scale, top + 96f, paint, width - 90f * scale)
    }

    private fun drawEventRow(
        canvas: Canvas,
        paint: Paint,
        event: CalendarEvent,
        colors: ShareColors,
        accentColor: Int,
        left: Float,
        top: Float,
        width: Float,
    ) {
        val scale = (width / 888f).coerceIn(0.56f, 1f)
        paint.color = colors.surface
        canvas.drawRoundRect(RectF(left, top, left + width, top + 52f), 18f * scale, 18f * scale, paint)
        paint.color = runCatching { Color.parseColor(event.colorHex ?: "") }.getOrDefault(accentColor)
        canvas.drawRoundRect(
            RectF(left + 16f * scale, top + 14f, left + 28f * scale, top + 38f),
            6f * scale,
            6f * scale,
            paint,
        )
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 23f * scale
        paint.color = colors.text
        val titlePaint = Paint(paint)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 20f * scale
        val timeText = eventTimeLine(event)
        val timeWidth = paint.measureText(timeText)
        titlePaint.textSize = 23f * scale
        titlePaint.color = colors.text
        titlePaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val titleLeft = left + 46f * scale
        val titleMaxWidth = (left + width - 24f * scale - timeWidth - 14f * scale - titleLeft).coerceAtLeast(0f)
        drawFittedText(canvas, event.title, titleLeft, top + 32f, titlePaint, titleMaxWidth)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = colors.secondary
        canvas.drawText(timeText, left + width - 14f * scale, top + 32f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawFooter(
        canvas: Canvas,
        paint: Paint,
        colors: ShareColors,
        centerX: Float,
        y: Float,
        scale: Float,
    ) {
        val width = 136f * scale
        val height = 46f * scale
        val left = centerX - width / 2f
        val top = y - height + 10f * scale
        paint.color = colors.surface
        canvas.drawRoundRect(RectF(left, top, left + width, top + height), 22f * scale, 22f * scale, paint)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 24f * scale
        paint.color = colors.text
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("DotCal", centerX, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Float,
    ) {
        canvas.drawText(fittedText(text, paint, maxWidth), x, y, paint)
    }

    private fun fittedText(text: String, paint: Paint, maxWidth: Float): String {
        if (maxWidth <= 0f || text.isEmpty()) return ""
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "..."
        val ellipsisWidth = paint.measureText(ellipsis)
        if (ellipsisWidth >= maxWidth) return ellipsis
        var low = 0
        var high = text.length
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (paint.measureText(text.take(mid).trimEnd() + ellipsis) <= maxWidth) {
                low = mid
            } else {
                high = mid - 1
            }
        }
        return text.take(low).trimEnd() + ellipsis
    }

    private fun shareRangeLine(events: List<CalendarEvent>): String {
        val first = events.first().localDate()
        val last = events.last().localDate()
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        return "${events.size} shifts | ${first.format(formatter)} - ${last.format(formatter)}"
    }

    private fun eventTimeLine(event: CalendarEvent): String {
        val zone = runCatching { ZoneId.of(event.timeZone) }.getOrDefault(ZoneId.systemDefault())
        val start = Instant.ofEpochMilli(event.startTimeMs).atZone(zone)
        if (event.isAllDay == 1) return start.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))
        val end = Instant.ofEpochMilli(event.endTimeMs).atZone(zone)
        val date = start.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))
        val time = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return "$date ${start.format(time)}-${end.format(time)}"
    }

    private fun CalendarEvent.localDate() =
        Instant.ofEpochMilli(startTimeMs).atZone(runCatching { ZoneId.of(timeZone) }.getOrDefault(ZoneId.systemDefault())).toLocalDate()

    private fun shareFile(context: Context, title: String, extension: String): File {
        val shareDir = File(context.cacheDir, "shared_events").apply { mkdirs() }
        return File(shareDir, "shift-plan-${title.safeShareFilename()}.$extension")
    }

    private fun File.uri(context: Context): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)

    private fun String.safeShareFilename(): String =
        lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-").trim('-').take(48).ifBlank { "shift-plan" }

    private fun shareColors(darkTheme: Boolean): ShareColors = if (darkTheme) {
        ShareColors(
            background = Color.rgb(11, 11, 13),
            surface = Color.rgb(18, 18, 22),
            text = Color.WHITE,
            secondary = Color.rgb(156, 163, 175),
        )
    } else {
        ShareColors(
            background = Color.rgb(250, 250, 250),
            surface = Color.WHITE,
            text = Color.rgb(17, 17, 17),
            secondary = Color.rgb(107, 114, 128),
        )
    }

    private data class ShareColors(
        val background: Int,
        val surface: Int,
        val text: Int,
        val secondary: Int,
    )
}
