package com.dotfield.dotcal.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File

object QrEventImageExporter {
    private const val CARD_WIDTH = 1_200
    private const val CARD_HEIGHT = 1_500
    private const val QR_SIZE = 960

    fun createCard(payload: String, eventTitle: String, eventDateTime: String = "", eventMeta: String = ""): Bitmap {
        val card = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(card)
        canvas.drawColor(Color.WHITE)

        val qr = createQrBitmap(payload, QR_SIZE)
        canvas.drawBitmap(qr, ((CARD_WIDTH - QR_SIZE) / 2).toFloat(), 96f, null)
        qr.recycle()

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titleLayout = StaticLayout.Builder
            .obtain(eventTitle.trim(), 0, eventTitle.trim().length, titlePaint, CARD_WIDTH - 160)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(2)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .build()
        canvas.save()
        canvas.translate(80f, 1_100f)
        titleLayout.draw(canvas)
        canvas.restore()

        val detailText = listOf(eventDateTime.trim(), eventMeta.trim())
            .filter { it.isNotBlank() }
            .joinToString("\n")
        if (detailText.isNotBlank()) {
            val detailPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(80, 80, 80)
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val detailLayout = StaticLayout.Builder
                .obtain(detailText, 0, detailText.length, detailPaint, CARD_WIDTH - 180)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(3)
                .setEllipsize(android.text.TextUtils.TruncateAt.END)
                .build()
            canvas.save()
            canvas.translate(90f, 1_220f)
            detailLayout.draw(canvas)
            canvas.restore()
        }

        val wordmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 58f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0f
        }
        canvas.drawText("DotCal", CARD_WIDTH / 2f, 1_440f, wordmarkPaint)
        return card
    }

    fun createShareUri(context: Context, bitmap: Bitmap, eventId: String): Uri {
        val shareDir = File(context.cacheDir, "shared_events").apply { mkdirs() }
        val file = File(shareDir, "dotcal-qr-${eventId.safeFilename()}.png")
        file.outputStream().use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "QR image export failed" }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun createQrBitmap(payload: String, size: Int = QR_SIZE): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to Charsets.UTF_8.name(),
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
        )
        val matrix = MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val rowOffset = y * size
            for (x in 0 until size) {
                pixels[rowOffset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
    }

    private fun String.safeFilename(): String =
        lowercase().replace(Regex("[^a-z0-9._-]+"), "-").trim('-').take(80).ifBlank { "event" }
}
