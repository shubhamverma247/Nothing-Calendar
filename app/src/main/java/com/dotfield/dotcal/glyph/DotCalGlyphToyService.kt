package com.dotfield.dotcal.glyph

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.dotfield.dotcal.DotCalApplication
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphToy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat

/**
 * Glyph Matrix "toy" that shows a live countdown to the user's next DotCal item on
 * Nothing Phone (3) / (4a) Pro — the differentiator a generic calendar app can't offer.
 *
 * Free tier: countdown to the next event only.
 * Pro tier: tasks are folded into the countdown, and a long-press on the Glyph Button
 * cycles forward through the next few upcoming items.
 *
 * The Nothing OS toy manager binds this service when the user selects the toy, so it
 * only ever loads on Nothing hardware (Android 14+). Every SDK call is wrapped so a
 * Glyph failure can never crash the host app; on an unsupported device the toy is inert.
 */
class DotCalGlyphToyService : Service() {

    private var manager: GlyphMatrixManager? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var renderJob: Job? = null
    @Volatile private var snoozedEventId: String? = null
    @Volatile private var snoozedUntilMs = 0L
    @Volatile private var clearedEventId: String? = null
    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            applyCommand(intent)
            scope.launch { runCatching { renderOnce() } }
        }
    }

    /** Pro long-press cycling: offset into the upcoming list. */
    @Volatile
    private var cycleIndex = 0

    private val eventThread = HandlerThread("dotcal-glyph").apply { start() }
    private val buttonHandler = Handler(eventThread.looper) { msg -> onGlyphMessage(msg); true }
    private val messenger = Messenger(buttonHandler)

    private val deviceTarget: String?
        get() = when {
            Common.is23112() -> Glyph.DEVICE_23112
            Common.is25111p() -> Glyph.DEVICE_25111p
            else -> null
        }

    override fun onCreate() {
        super.onCreate()
        ContextCompat.registerReceiver(
            this,
            commandReceiver,
            IntentFilter().apply {
                addAction(DotCalGlyphBridge.ACTION_EVENT_SNOOZED)
                addAction(DotCalGlyphBridge.ACTION_EVENT_CLEARED)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        val target = deviceTarget
        if (target != null) {
            val mgr = runCatching { GlyphMatrixManager.getInstance(applicationContext) }.getOrNull()
            manager = mgr
            runCatching {
                mgr?.init(object : GlyphMatrixManager.Callback {
                    override fun onServiceConnected(name: ComponentName?) {
                        runCatching { mgr.register(target) }
                        startRenderLoop()
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        stopRenderLoop()
                    }
                })
            }
        }
        // Always return the messenger so the toy manager can bind and deliver button
        // events, even on unsupported hardware (where nothing renders).
        return messenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        applyCommand(intent)
        scope.launch { runCatching { renderOnce() } }
        return START_NOT_STICKY
    }

    private fun applyCommand(intent: Intent?) {
        when (intent?.action) {
            DotCalGlyphBridge.ACTION_EVENT_SNOOZED -> {
                snoozedEventId = intent.getStringExtra(DotCalGlyphBridge.EXTRA_EVENT_ID)
                snoozedUntilMs = intent.getLongExtra(DotCalGlyphBridge.EXTRA_UNTIL_MS, 0L)
                clearedEventId = null
            }
            DotCalGlyphBridge.ACTION_EVENT_CLEARED -> {
                clearedEventId = intent.getStringExtra(DotCalGlyphBridge.EXTRA_EVENT_ID)
                if (clearedEventId == snoozedEventId) {
                    snoozedEventId = null
                    snoozedUntilMs = 0L
                }
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopRenderLoop()
        runCatching { manager?.turnOff() }
        runCatching { manager?.unInit() }
        manager = null
        return false
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(commandReceiver) }
        stopRenderLoop()
        scope.cancel()
        eventThread.quitSafely()
        super.onDestroy()
    }

    private fun startRenderLoop() {
        renderJob?.cancel()
        renderJob = scope.launch {
            while (isActive) {
                runCatching { renderOnce() }
                delay(REFRESH_MS)
            }
        }
    }

    private fun stopRenderLoop() {
        renderJob?.cancel()
        renderJob = null
    }

    private suspend fun renderOnce() {
        val app = applicationContext as? DotCalApplication ?: return
        val isPro = app.repository.readIsPro()
        val now = System.currentTimeMillis()
        val upcoming = app.repository.getNextUpcomingList(
            includeTasks = isPro,
            nowMs = now,
            limit = if (isPro) PRO_CYCLE_LIMIT else 1,
            includeActive = true,
        )
        val visible = upcoming.filterNot { it.id == clearedEventId || it.id == snoozedEventId }
        val snoozeTarget = upcoming.firstOrNull { it.id == snoozedEventId }
        if (snoozeTarget != null && snoozedUntilMs > now) {
            renderProgressFrame(
                progress = 0f,
                label = formatCountdown(snoozedUntilMs - now),
            )
            return
        }
        if (snoozeTarget != null) {
            snoozedEventId = null
            snoozedUntilMs = 0L
        }
        if (visible.isEmpty()) {
            renderText(EMPTY_GLYPH)
            return
        }
        val index = if (isPro) cycleIndex % visible.size else 0
        val target = visible[index]
        renderProgressFrame(
            progress = eventProgress(target.startTimeMs, target.endTimeMs, now),
            label = formatCountdown(target.startTimeMs - now),
        )
    }

    private fun eventProgress(startMs: Long, endMs: Long, nowMs: Long): Float {
        if (endMs <= startMs || nowMs <= startMs) return 0f
        return ((nowMs - startMs).toFloat() / (endMs - startMs).toFloat()).coerceIn(0f, 1f)
    }

    private fun renderProgressFrame(progress: Float, label: String) {
        val size = matrixSize()
        val frame = buildCountdownFrame(label)
        val innerStart = (size * 0.12f).toInt().coerceAtLeast(1)
        val innerEnd = (size * 0.88f).toInt().coerceAtMost(size - 2)
        val width = (innerEnd - innerStart + 1).coerceAtLeast(1)
        val filled = (width * progress.coerceIn(0f, 1f)).toInt()
        for (x in innerStart..innerEnd) {
            setPixel(frame, size, x, innerStart, TEXT_BRIGHTNESS)
            setPixel(frame, size, x, innerEnd, TEXT_BRIGHTNESS)
        }
        for (y in innerStart..innerEnd) {
            setPixel(frame, size, innerStart, y, TEXT_BRIGHTNESS)
            setPixel(frame, size, innerEnd, y, TEXT_BRIGHTNESS)
        }
        for (x in innerStart until (innerStart + filled).coerceAtMost(innerEnd + 1)) {
            for (y in (innerEnd - PROGRESS_THICKNESS + 1)..innerEnd) {
                setPixel(frame, size, x, y, TEXT_BRIGHTNESS)
            }
        }
        renderFrame(frame)
    }

    private fun renderFrame(frame: IntArray) {
        runCatching { manager?.setMatrixFrame(frame) }
    }

    private fun renderText(text: String) {
        val mgr = manager ?: return
        runCatching {
            mgr.setMatrixFrame(buildCountdownFrame(text))
        }
    }

    private fun buildCountdownFrame(text: String): IntArray {
        val size = matrixSize()
        val frame = IntArray(size * size)
        // Clean render: countdown text on black. No background plate, no ring.
        drawCenteredText(frame, size, text.lowercase())
        return frame
    }

    private fun matrixSize(): Int {
        return runCatching { Common.getDeviceMatrixLength() }
            .getOrDefault(DEFAULT_MATRIX_SIZE)
            .takeIf { it > 0 }
            ?: DEFAULT_MATRIX_SIZE
    }

    private fun drawCenteredText(frame: IntArray, size: Int, text: String) {
        val glyphs = text.mapNotNull { GLYPH_FONT[it] }
        if (glyphs.isEmpty()) return
        val scale = TEXT_SCALE
        val width = textWidth(glyphs, scale)
        val height = FONT_HEIGHT * scale
        // Integer centering of the text bounding box. Round leftover up so the box never
        // leans left when (size - width) is odd.
        var x = ((size - width + 1) / 2).coerceAtLeast(0)
        val y = ((size - height + 1) / 2).coerceAtLeast(0)
        glyphs.forEachIndexed { index, glyph ->
            drawGlyph(frame, size, glyph, x, y, scale)
            x += glyph.first().length * scale
            if (index != glyphs.lastIndex) x += LETTER_SPACING
        }
    }

    private fun textWidth(glyphs: List<Array<String>>, scale: Int): Int {
        val glyphWidth = glyphs.sumOf { it.first().length * scale }
        return glyphWidth + LETTER_SPACING * (glyphs.size - 1)
    }

    private fun drawGlyph(
        frame: IntArray,
        size: Int,
        glyph: Array<String>,
        left: Int,
        top: Int,
        scale: Int,
    ) {
        glyph.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                if (cell != '#') return@forEachIndexed
                repeat(scale) { dy ->
                    repeat(scale) { dx ->
                        setPixel(
                            frame = frame,
                            size = size,
                            x = left + colIndex * scale + dx,
                            y = top + rowIndex * scale + dy,
                            brightness = TEXT_BRIGHTNESS,
                        )
                    }
                }
            }
        }
    }

    private fun setPixel(frame: IntArray, size: Int, x: Int, y: Int, brightness: Int) {
        if (x !in 0 until size || y !in 0 until size) return
        val index = y * size + x
        frame[index] = maxOf(frame[index], brightness)
    }

    private fun onGlyphMessage(msg: Message) {
        if (msg.what != GlyphToy.MSG_GLYPH_TOY) return
        when (msg.data?.getString(GlyphToy.MSG_GLYPH_TOY_DATA)) {
            // Long-press: Pro cycles to the next item; free users get a refresh.
            GlyphToy.EVENT_CHANGE -> {
                cycleIndex += 1
                scope.launch { runCatching { renderOnce() } }
            }
            // Always-on-display tick (once per minute when selected as AOD toy).
            GlyphToy.EVENT_AOD -> scope.launch { runCatching { renderOnce() } }
        }
    }

    /** Compact countdown for the 25x25 matrix: "3d" / "5h" / "12m" / "now". */
    private fun formatCountdown(deltaMs: Long): String {
        if (deltaMs <= 0L) return NOW_GLYPH
        val minutes = deltaMs / 60_000L
        return when {
            minutes >= MINUTES_PER_DAY -> "${(minutes / MINUTES_PER_DAY).coerceAtMost(MAX_DAYS)}d"
            minutes >= MINUTES_PER_HOUR -> "${minutes / MINUTES_PER_HOUR}h"
            minutes >= 1L -> "${minutes}m"
            else -> NOW_GLYPH
        }
    }

    companion object {
        private const val REFRESH_MS = 30_000L
        private const val PRO_CYCLE_LIMIT = 5
        private const val DEFAULT_MATRIX_SIZE = 25
        private const val FONT_HEIGHT = 5
        private const val TEXT_SCALE = 1
        private const val LETTER_SPACING = 1
        private const val TEXT_BRIGHTNESS = 4095
        private const val PROGRESS_THICKNESS = 2
        private const val MINUTES_PER_HOUR = 60L
        private const val MINUTES_PER_DAY = 60L * 24L
        private const val MAX_DAYS = 99L
        private const val EMPTY_GLYPH = "--"
        private const val NOW_GLYPH = "now"

        private val GLYPH_FONT = mapOf(
            '0' to arrayOf("###", "#.#", "#.#", "#.#", "###"),
            '1' to arrayOf(".#.", "##.", ".#.", ".#.", "###"),
            '2' to arrayOf("###", "..#", "###", "#..", "###"),
            '3' to arrayOf("###", "..#", "###", "..#", "###"),
            '4' to arrayOf("#.#", "#.#", "###", "..#", "..#"),
            '5' to arrayOf("###", "#..", "###", "..#", "###"),
            '6' to arrayOf("###", "#..", "###", "#.#", "###"),
            '7' to arrayOf("###", "..#", ".#.", ".#.", ".#."),
            '8' to arrayOf("###", "#.#", "###", "#.#", "###"),
            '9' to arrayOf("###", "#.#", "###", "..#", "###"),
            'd' to arrayOf("##.", "#.#", "#.#", "#.#", "##."),
            'h' to arrayOf("#.#", "#.#", "###", "#.#", "#.#"),
            'm' to arrayOf("#...#", "##.##", "#.#.#", "#...#", "#...#"),
            'n' to arrayOf("#.#", "###", "###", "#.#", "#.#"),
            'o' to arrayOf("###", "#.#", "#.#", "#.#", "###"),
            'w' to arrayOf("#.#", "#.#", "#.#", "###", "#.#"),
            '-' to arrayOf("...", "...", "###", "...", "..."),
        )
    }
}
