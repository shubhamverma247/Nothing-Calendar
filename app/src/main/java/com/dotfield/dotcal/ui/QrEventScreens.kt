package com.dotfield.dotcal.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dotfield.dotcal.data.ics.ParsedIcsItem
import com.dotfield.dotcal.share.QrEventImageExporter
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal sealed interface QrScanOutcome {
    data object Accepted : QrScanOutcome
    data class Rejected(val message: String) : QrScanOutcome
}

@Composable
internal fun QrEventShareScreen(
    eventTitle: String,
    eventDateTime: String,
    eventMeta: String,
    payload: String,
    sharedWithoutDescription: Boolean,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onShare: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportBitmapResult by produceState<Result<Bitmap>?>(initialValue = null, payload, eventTitle) {
        value = withContext(Dispatchers.Default) {
            runCatching { QrEventImageExporter.createCard(payload, eventTitle, eventDateTime, eventMeta) }
        }
    }
    val qrBitmapResult by produceState<Result<Bitmap>?>(initialValue = null, payload) {
        value = withContext(Dispatchers.Default) {
            runCatching { QrEventImageExporter.createQrBitmap(payload, size = 920) }
        }
    }
    var pendingSaveBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png"),
    ) { uri ->
        val bitmap = pendingSaveBitmap
        pendingSaveBitmap = null
        if (uri != null && bitmap != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        } == true
                    }.getOrDefault(false)
                }
                showDotCalToast(context, palette, if (saved) "QR image saved" else "Could not save QR image")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        QrTopBar(title = "Share as QR", palette = palette, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                qrBitmapResult?.fold(
                    onSuccess = { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Event QR code",
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                    onFailure = {
                        Text(
                            "Could not create QR code",
                            color = palette.accent,
                            textAlign = TextAlign.Center,
                        )
                    },
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                eventTitle,
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            if (eventDateTime.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    eventDateTime,
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            if (eventMeta.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    eventMeta,
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
            if (sharedWithoutDescription) {
                Text(
                    "Shared without description",
                    color = palette.accent,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.bottomNavSurface)
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = {
                    exportBitmapResult?.getOrNull()?.let { bitmap ->
                        pendingSaveBitmap = bitmap
                        saveLauncher.launch("${eventTitle.safeQrFilename()}.png")
                    }
                },
                enabled = exportBitmapResult?.isSuccess == true,
                modifier = Modifier.weight(1f).height(56.dp),
                border = secondaryActionBorder(palette),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryActionContainer(palette),
                    contentColor = secondaryActionContent(palette),
                    disabledContainerColor = secondaryActionContainer(palette).copy(alpha = 0.55f),
                    disabledContentColor = palette.disabledText,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Save image", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { exportBitmapResult?.getOrNull()?.let(onShare) },
                enabled = exportBitmapResult?.isSuccess == true,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.onAccent,
                    disabledContainerColor = palette.accent.copy(alpha = 0.45f),
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Share", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
internal fun QrEventScannerScreen(
    palette: DotCalPalette,
    onBack: () -> Unit,
    onCodeDetected: (String) -> QrScanOutcome,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        QrTopBar(title = "Scan event QR", palette = palette, onBack = onBack)
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(42.dp),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "Camera access lets DotCal scan event QR codes. Frames stay in memory and are never saved.",
                    color = palette.primaryText,
                    fontSize = 16.sp,
                    lineHeight = 23.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Allow camera")
                }
            }
        } else {
            CameraQrScanner(
                modifier = Modifier.fillMaxSize(),
                palette = palette,
                onCodeDetected = onCodeDetected,
            )
        }
    }
}

@Composable
@SuppressLint("UnsafeOptInUsageError")
private fun CameraQrScanner(
    modifier: Modifier,
    palette: DotCalPalette,
    onCodeDetected: (String) -> QrScanOutcome,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build(),
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val handlingFrame = remember { AtomicBoolean(false) }
    val lastValue = remember { AtomicReference<String?>(null) }
    val lastScanAt = remember { AtomicLong(0L) }
    val active = remember { AtomicBoolean(true) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                if (!active.get()) return@addListener
                val provider = runCatching { providerFuture.get() }
                    .onFailure { errorMessage = "Camera unavailable" }
                    .getOrNull()
                    ?: return@addListener
                cameraProvider = provider
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage == null || !handlingFrame.compareAndSet(false, true)) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener(mainExecutor) { barcodes ->
                            if (!active.get()) return@addOnSuccessListener
                            val value = barcodes.firstNotNullOfOrNull { it.rawValue } ?: return@addOnSuccessListener
                            val now = SystemClock.elapsedRealtime()
                            if (value == lastValue.get() && now - lastScanAt.get() < 1_500L) return@addOnSuccessListener
                            lastValue.set(value)
                            lastScanAt.set(now)
                            when (val result = onCodeDetected(value)) {
                                QrScanOutcome.Accepted -> errorMessage = null
                                is QrScanOutcome.Rejected -> errorMessage = result.message
                            }
                        }
                        .addOnCompleteListener(mainExecutor) {
                            handlingFrame.set(false)
                            imageProxy.close()
                        }
                }
                runCatching {
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                }.onFailure {
                    errorMessage = "Camera unavailable"
                }
            },
            mainExecutor,
        )
        onDispose {
            active.set(false)
            cameraProvider?.unbindAll()
            scanner.close()
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp)),
        )
        Text(
            errorMessage ?: "Point camera at event QR",
            color = if (errorMessage == null) Color.White else palette.accent,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(horizontal = 24.dp, vertical = 18.dp),
        )
    }
}

@Composable
internal fun IcsImportPreviewScreen(
    items: List<ParsedIcsItem>,
    palette: DotCalPalette,
    use24HourFormat: Boolean,
    onBack: () -> Unit,
    onImport: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        QrTopBar(title = "Import preview", palette = palette, onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        ) {
            itemsIndexed(
                items = items,
                key = { index, item -> "${item.uid ?: "${item.title}-${item.startTimeMs}"}-$index" },
            ) { _, item ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                    Text(
                        item.title,
                        color = palette.primaryText,
                        fontFamily = LocalHeadingFont.current,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.previewDateTime(use24HourFormat), color = palette.secondaryText, fontSize = 14.sp)
                    if (item.location.isNotBlank()) {
                        Text(item.location, color = palette.secondaryText, fontSize = 14.sp)
                    }
                    if (!item.rrule.isNullOrBlank()) {
                        Text("Repeats", color = palette.accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = palette.line.copy(alpha = 0.55f))
            }
        }
        Button(
            onClick = onImport,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 20.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (items.size == 1) "Import event" else "Import ${items.size} items")
        }
    }
}

@Composable
private fun QrTopBar(
    title: String,
    palette: DotCalPalette,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.topBarSurface).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
        }
        Text(
            title,
            color = palette.primaryText,
            fontFamily = LocalHeadingFont.current,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(48.dp))
    }
}

private fun ParsedIcsItem.previewDateTime(use24HourFormat: Boolean): String {
    if (startTimeMs <= 0L) return "No date"
    val zone = runCatching { ZoneId.of(timeZone) }.getOrDefault(ZoneId.systemDefault())
    val start = Instant.ofEpochMilli(startTimeMs).atZone(zone)
    val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    if (isAllDay) return "${start.toLocalDate().format(dateFormat)} - All-day"
    val timeFormat = DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm a", Locale.getDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(zone)
    return "${start.toLocalDate().format(dateFormat)} - ${start.toLocalTime().format(timeFormat)} to ${end.toLocalTime().format(timeFormat)}"
}

private fun String.safeQrFilename(): String =
    lowercase().replace(Regex("[^a-z0-9._-]+"), "-").trim('-').take(80).ifBlank { "dotcal-event-qr" }
