package com.dotfield.dotcal.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

internal enum class OnboardingPage {
    Welcome,
    CalendarPermission,
    Notifications,
    Contacts,
    Ready,
}

internal val onboardingPages = listOf(
    OnboardingPage.Welcome,
    OnboardingPage.CalendarPermission,
    OnboardingPage.Notifications,
    OnboardingPage.Contacts,
    OnboardingPage.Ready,
)

@Composable
internal fun OnboardingScreen(
    page: OnboardingPage,
    pageIndex: Int,
    pageCount: Int,
    palette: DotCalPalette,
    hasCalendarPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasContactsPermission: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    val copy = onboardingCopy(
        page = page,
        hasCalendarPermission = hasCalendarPermission,
        hasNotificationPermission = hasNotificationPermission,
        hasContactsPermission = hasContactsPermission,
    )
    val colors = remember(palette) { onboardingColors(palette) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        val compactHeight = maxHeight < 720.dp
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(46.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primaryText)
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onSkip) {
                    Text("Skip", color = colors.secondaryText, fontFamily = mono, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(if (compactHeight) 20.dp else 28.dp))
            OnboardingHero(
                page = page,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactHeight) 120.dp else 132.dp),
            )
            Spacer(modifier = Modifier.height(if (compactHeight) 24.dp else 34.dp))
            Text(
                text = copy.label,
                color = colors.accent,
                fontFamily = mono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = copy.title,
                color = colors.primaryText,
                fontFamily = mono,
                fontSize = if (compactHeight) 22.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compactHeight) 26.sp else 28.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))
            Text(
                text = copy.description,
                color = colors.secondaryText,
                fontFamily = mono,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                maxLines = if (compactHeight) 3 else 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            OnboardingProgress(pageIndex = pageIndex, pageCount = pageCount, colors = colors)
            Spacer(modifier = Modifier.height(if (compactHeight) 14.dp else 20.dp))
            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().height(if (compactHeight) 48.dp else 52.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
            ) {
                Text(copy.primaryLabel, fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            if (page != OnboardingPage.Welcome && page != OnboardingPage.Ready) {
                TextButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth().height(if (compactHeight) 44.dp else 48.dp),
                ) {
                    Text("Not Now", color = colors.secondaryText, fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                Spacer(modifier = Modifier.height(if (compactHeight) 44.dp else 48.dp))
            }
        }
    }
}

private data class OnboardingCopy(
    val label: String,
    val title: String,
    val description: String,
    val primaryLabel: String,
)

private data class OnboardingColors(
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val accent: Color,
    val onAccent: Color,
    val glow: Color,
    val shadow: Color,
    val line: Color,
    val isDark: Boolean,
)

private fun onboardingCopy(
    page: OnboardingPage,
    hasCalendarPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasContactsPermission: Boolean,
): OnboardingCopy {
    return when (page) {
        OnboardingPage.Welcome -> OnboardingCopy(
            label = "DOTCAL",
            title = "DotCal",
            description = "A focused calendar for events, tasks, reminders, birthdays, and widgets.",
            primaryLabel = "Continue",
        )
        OnboardingPage.CalendarPermission -> OnboardingCopy(
            label = "OPTIONAL",
            title = "Calendar Access",
            description = "Connect device calendars for local CalendarProvider sync. Local DotCal events still work without it.",
            primaryLabel = if (hasCalendarPermission) "Continue" else "Allow Calendar",
        )
        OnboardingPage.Notifications -> OnboardingCopy(
            label = "OPTIONAL",
            title = "Reminders",
            description = "Allow notifications so event and task reminders can appear at the scheduled time.",
            primaryLabel = if (hasNotificationPermission) "Continue" else "Allow Reminders",
        )
        OnboardingPage.Contacts -> OnboardingCopy(
            label = "OPTIONAL",
            title = "Birthdays",
            description = "Allow contacts to import birthdays as read-only yearly events. You can skip this now.",
            primaryLabel = if (hasContactsPermission) "Continue" else "Allow Contacts",
        )
        OnboardingPage.Ready -> OnboardingCopy(
            label = "READY",
            title = "You're all set",
            description = "Your calendar is ready.",
            primaryLabel = "Start",
        )
    }
}

private fun onboardingColors(palette: DotCalPalette): OnboardingColors {
    return if (palette.isDark) {
        OnboardingColors(
            background = palette.background,
            surface = palette.dialogSurface,
            elevatedSurface = palette.eventCardSurface,
            primaryText = palette.primaryText,
            secondaryText = palette.secondaryText,
            mutedText = palette.dimText,
            accent = palette.accent,
            onAccent = palette.onAccent,
            glow = palette.accent.copy(alpha = 0.22f),
            shadow = Color.Black.copy(alpha = 0.55f),
            line = palette.line,
            isDark = true,
        )
    } else {
        OnboardingColors(
            background = palette.background,
            surface = palette.dialogSurface,
            elevatedSurface = palette.eventCardSurface,
            primaryText = palette.primaryText,
            secondaryText = palette.secondaryText,
            mutedText = palette.dimText,
            accent = palette.accent,
            onAccent = palette.onAccent,
            glow = palette.accent.copy(alpha = 0.13f),
            shadow = Color(0xFF111827).copy(alpha = 0.13f),
            line = palette.line,
            isDark = false,
        )
    }
}

@Composable
private fun OnboardingHero(page: OnboardingPage, colors: OnboardingColors, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .then(modifier)
            .heightIn(min = 110.dp, max = 150.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (page) {
                OnboardingPage.Welcome -> drawFlatWelcomeHero(colors)
                OnboardingPage.CalendarPermission -> drawFlatCalendarAccessHero(colors)
                OnboardingPage.Notifications -> drawFlatReminderHero(colors)
                OnboardingPage.Contacts -> drawFlatBirthdayHero(colors)
                OnboardingPage.Ready -> drawFlatReadyHero(colors)
            }
        }
    }
}

@Composable
private fun OnboardingProgress(pageIndex: Int, pageCount: Int, colors: OnboardingColors) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pageIndex + 1}",
                color = colors.accent,
                fontFamily = mono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = " / $pageCount",
                color = colors.primaryText,
                fontFamily = mono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            repeat(pageCount) { index ->
                val active = index == pageIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (active) 16.dp else 5.dp)
                        .height(5.dp)
                        .background(if (active) colors.accent else colors.mutedText.copy(alpha = if (colors.isDark) 0.55f else 0.75f)),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatWelcomeHero(colors: OnboardingColors) {
    val calendarWidth = size.minDimension * 0.441f
    val calendarHeight = calendarWidth * 0.86f
    val left = (size.width - calendarWidth) / 2f
    val top = size.height * 0.22f
    drawFlatCalendarPage(colors, Offset(left, top), calendarWidth, calendarHeight, dotGrid = true)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatCalendarAccessHero(colors: OnboardingColors) {
    val side = size.minDimension * 0.441f
    val calendarHeight = side * 0.86f
    val left = (size.width - side) / 2f
    val top = size.height * 0.22f
    drawRect(
        color = colors.background,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(side, calendarHeight),
    )
    drawRect(colors.primaryText, Offset(left, top), androidx.compose.ui.geometry.Size(side, calendarHeight), style = Stroke(2.dp.toPx()))
    drawRect(colors.accent, Offset(left, top), androidx.compose.ui.geometry.Size(side, calendarHeight * 0.3f))
    drawCircle(colors.accent, 4.dp.toPx(), Offset(left + side * 0.5f, top + calendarHeight * 0.58f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatReminderHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.5f, size.height * 0.42f)
    drawCircle(colors.primaryText, size.minDimension * 0.168f, c, style = Stroke(2.dp.toPx()))
    drawRect(
        colors.accent,
        Offset(c.x - 5.dp.toPx(), c.y + size.minDimension * 0.15f),
        androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx()),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatBirthdayHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.5f, size.height * 0.3f)
    val radius = size.minDimension * 0.105f
    drawCircle(colors.primaryText, radius, c, style = Stroke(2.dp.toPx()))
    drawCircle(colors.accent, 4.5.dp.toPx(), Offset(c.x + radius * 1.45f, c.y - radius * 0.95f))
    drawArc(
        color = colors.primaryText,
        startAngle = 205f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(c.x - radius * 1.62f, c.y + radius * 1.72f),
        size = androidx.compose.ui.geometry.Size(radius * 3.24f, radius * 1.72f),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatReadyHero(colors: OnboardingColors) {
    val side = size.minDimension * 0.441f
    val left = (size.width - side) / 2f
    val top = size.height * 0.22f
    drawRect(colors.accent, Offset(left, top), androidx.compose.ui.geometry.Size(side, side), style = Stroke(2.dp.toPx()))
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(left + side * 0.24f, top + side * 0.52f)
        lineTo(left + side * 0.43f, top + side * 0.71f)
        lineTo(left + side * 0.78f, top + side * 0.31f)
    }
    drawPath(path, colors.accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatCalendarPage(
    colors: OnboardingColors,
    topLeft: Offset,
    width: Float,
    height: Float,
    dotGrid: Boolean,
) {
    drawRect(colors.background, topLeft, androidx.compose.ui.geometry.Size(width, height))
    val borderColor = if (colors.isDark) Color(0xFF171717) else Color(0xFF101010)
    drawRect(borderColor, topLeft, androidx.compose.ui.geometry.Size(width, height), style = Stroke(1.5.dp.toPx()))
    drawRect(colors.accent, topLeft, androidx.compose.ui.geometry.Size(width, height * 0.3f))
    val pinWidth = 4.dp.toPx()
    val pinHeight = 14.dp.toPx()
    listOf(0.28f, 0.72f).forEach { xFactor ->
        drawRect(
            colors.primaryText,
            Offset(topLeft.x + width * xFactor - pinWidth / 2f, topLeft.y - pinHeight * 0.65f),
            androidx.compose.ui.geometry.Size(pinWidth, pinHeight),
        )
    }
    if (dotGrid) {
        val gapX = width * 0.26f
        val startX = topLeft.x + (width - gapX * 2f) / 2f
        val startY = topLeft.y + height * 0.52f
        val gapY = height * 0.24f
        repeat(2) { row ->
            repeat(3) { col ->
                val active = row == 1 && col == 1
                val inactiveColor = if (colors.isDark) {
                    Color(0xFF303030)
                } else {
                    Color(0xFF4A4A4A)
                }
                drawCircle(
                    if (active) colors.accent else inactiveColor,
                    if (active) 4.dp.toPx() else 3.dp.toPx(),
                    Offset(startX + col * gapX, startY + row * gapY),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeroAtmosphere(colors: OnboardingColors) {
    val center = Offset(size.width * 0.52f, size.height * 0.47f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colors.glow, Color.Transparent),
            center = center,
            radius = size.minDimension * 0.52f,
        ),
        radius = size.minDimension * 0.52f,
        center = center,
    )
    repeat(5) { index ->
        val x = size.width * (0.18f + index * 0.16f)
        val y = size.height * (0.22f + (index % 3) * 0.18f)
        drawCircle(colors.accent.copy(alpha = 0.75f - index * 0.08f), radius = 2.2.dp.toPx(), center = Offset(x, y))
    }
    repeat(3) { index ->
        val x = size.width * (0.22f + index * 0.28f)
        val y = size.height * (0.74f - index * 0.08f)
        drawCircle(colors.mutedText.copy(alpha = 0.35f), radius = 1.6.dp.toPx(), center = Offset(x, y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalendarHero(colors: OnboardingColors) {
    drawLeafCluster(colors, Offset(size.width * 0.15f, size.height * 0.68f), -1f)
    drawLeafCluster(colors, Offset(size.width * 0.85f, size.height * 0.68f), 1f)
    drawSoftShadow(colors, Offset(size.width * 0.5f, size.height * 0.78f), size.width * 0.34f, size.height * 0.055f)

    val left = size.width * 0.19f
    val top = size.height * 0.28f
    val width = size.width * 0.56f
    val height = size.height * 0.42f

    val standPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(left + width - 12.dp.toPx(), top + 10.dp.toPx())
        lineTo(left + width + 24.dp.toPx(), top + height - 4.dp.toPx())
        lineTo(left + width, top + height)
        close()
    }
    drawPath(standPath, colors.elevatedSurface)
    drawPath(standPath, colors.line.copy(alpha = 0.55f), style = Stroke(1.dp.toPx()))

    drawRoundRect(
        color = colors.line.copy(alpha = 0.4f),
        topLeft = Offset(left + 3.dp.toPx(), top - 5.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(width - 6.dp.toPx(), height),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )
    drawRoundRect(
        color = colors.elevatedSurface,
        topLeft = Offset(left + 6.dp.toPx(), top - 2.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(width - 12.dp.toPx(), height),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )

    drawRoundRect(colors.surface, Offset(left, top), androidx.compose.ui.geometry.Size(width, height), CornerRadius(16.dp.toPx(), 16.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.55f), Offset(left, top), androidx.compose.ui.geometry.Size(width, height), CornerRadius(16.dp.toPx(), 16.dp.toPx()), style = Stroke(1.dp.toPx()))

    drawRoundRect(
        brush = Brush.verticalGradient(listOf(colors.accent, Color(0xFFFF2A22))),
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(width, height * 0.24f),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(colors.accent, Color(0xFFFF2A22))),
        topLeft = Offset(left, top + height * 0.12f),
        size = androidx.compose.ui.geometry.Size(width, height * 0.12f)
    )

    val ringCount = 5
    val ringWidth = 10.dp.toPx()
    val ringHeight = 18.dp.toPx()
    repeat(ringCount) { i ->
        val x = left + width * (0.16f + i * 0.17f)
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = 2.dp.toPx(),
            center = Offset(x, top + height * 0.08f)
        )
        drawArc(
            color = colors.primaryText.copy(alpha = 0.85f),
            startAngle = 180f,
            sweepAngle = 200f,
            useCenter = false,
            topLeft = Offset(x - ringWidth / 2f, top - 10.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(ringWidth, ringHeight),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    val colStep = width * 0.17f
    val rowStep = height * 0.16f
    val gridStartX = left + width * 0.16f
    val gridStartY = top + height * 0.35f
    val cellW = colStep * 0.65f
    val cellH = rowStep * 0.65f

    repeat(3) { row ->
        repeat(4) { col ->
            val x = gridStartX + col * colStep
            val y = gridStartY + row * rowStep
            val active = row == 2 && col == 2

            drawRoundRect(
                color = if (active) colors.accent else colors.line.copy(alpha = if (colors.isDark) 0.6f else 0.4f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(cellW, cellH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            if (active) {
                val checkPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x + cellW * 0.25f, y + cellH * 0.5f)
                    lineTo(x + cellW * 0.45f, y + cellH * 0.7f)
                    lineTo(x + cellW * 0.75f, y + cellH * 0.3f)
                }
                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            } else if (row == 0 && col == 2) {
                val checkPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x + cellW * 0.25f, y + cellH * 0.5f)
                    lineTo(x + cellW * 0.45f, y + cellH * 0.7f)
                    lineTo(x + cellW * 0.75f, y + cellH * 0.3f)
                }
                drawPath(
                    path = checkPath,
                    color = colors.accent.copy(alpha = 0.55f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            } else if (row == 1 && col == 1) {
                val checkPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x + cellW * 0.25f, y + cellH * 0.5f)
                    lineTo(x + cellW * 0.45f, y + cellH * 0.7f)
                    lineTo(x + cellW * 0.75f, y + cellH * 0.3f)
                }
                drawPath(
                    path = checkPath,
                    color = colors.secondaryText.copy(alpha = 0.45f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            } else if (row == 1 && col == 3) {
                drawCircle(
                    color = colors.accent.copy(alpha = 0.85f),
                    radius = 3.dp.toPx(),
                    center = Offset(x + cellW / 2f, y + cellH / 2f)
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalendarHubHero(colors: OnboardingColors) {
    val center = Offset(size.width * 0.5f, size.height * 0.48f)
    drawConnectionDots(colors, center, Offset(size.width * 0.22f, size.height * 0.28f))
    drawConnectionDots(colors, center, Offset(size.width * 0.78f, size.height * 0.3f))
    drawConnectionDots(colors, center, Offset(size.width * 0.23f, size.height * 0.68f))
    drawConnectionDots(colors, center, Offset(size.width * 0.76f, size.height * 0.64f))

    drawFloatingCard(colors, Offset(size.width * 0.06f, size.height * 0.2f), size.width * 0.34f, "calendar", "Team Meeting", "10:00 AM")
    drawFloatingCard(colors, Offset(size.width * 0.61f, size.height * 0.22f), size.width * 0.34f, "calendar", "Project Review", "2:30 PM")
    drawFloatingCard(colors, Offset(size.width * 0.08f, size.height * 0.62f), size.width * 0.36f, "calendar", "Dinner with Alex", "7:00 PM")
    drawFloatingCard(colors, Offset(size.width * 0.58f, size.height * 0.58f), size.width * 0.34f, "grid")

    drawSoftShadow(colors, Offset(center.x, size.height * 0.76f), size.width * 0.22f, size.height * 0.04f)
    drawRoundRect(colors.surface, Offset(center.x - size.width * 0.18f, center.y - size.height * 0.16f), androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.32f), CornerRadius(22.dp.toPx(), 22.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.5f), Offset(center.x - size.width * 0.18f, center.y - size.height * 0.16f), androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.32f), CornerRadius(22.dp.toPx(), 22.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawRoundRect(colors.accent, Offset(center.x - size.width * 0.18f, center.y - size.height * 0.16f), androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.09f), CornerRadius(22.dp.toPx(), 22.dp.toPx()))
    repeat(3) { i ->
        val x = center.x - size.width * 0.08f + i * size.width * 0.08f
        drawLine(colors.primaryText.copy(alpha = 0.35f), Offset(x, center.y - size.height * 0.19f), Offset(x, center.y - size.height * 0.105f), strokeWidth = 3.dp.toPx())
    }
    repeat(3) { row ->
        repeat(3) { col ->
            val x = center.x - size.width * 0.09f + col * size.width * 0.09f
            val y = center.y - size.height * 0.02f + row * size.height * 0.075f
            drawRoundRect(if (row == 1 && col == 1) colors.accent else colors.line, Offset(x, y), androidx.compose.ui.geometry.Size(14.dp.toPx(), 14.dp.toPx()), CornerRadius(4.dp.toPx(), 4.dp.toPx()))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawReminderHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.52f, size.height * 0.44f)
    repeat(4) { index ->
        drawCircle(colors.accent.copy(alpha = 0.06f - index * 0.01f), radius = size.minDimension * (0.2f + index * 0.09f), center = c, style = Stroke(1.dp.toPx()))
    }
    drawLeafCluster(colors, Offset(size.width * 0.13f, size.height * 0.7f), -1f)
    drawLeafCluster(colors, Offset(size.width * 0.88f, size.height * 0.64f), 1f)
    drawFloatingCard(colors, Offset(size.width * 0.05f, size.height * 0.18f), size.width * 0.35f, "bell", "Meeting", "in 10 min")
    drawFloatingCard(colors, Offset(size.width * 0.58f, size.height * 0.66f), size.width * 0.36f, "check", "Buy groceries", "Today, 6:00 PM")
    drawSoftShadow(colors, Offset(c.x, size.height * 0.72f), size.width * 0.25f, size.height * 0.05f)
    drawCircle(colors.accent.copy(alpha = 0.34f), size.width * 0.19f, c)
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFF6A60), colors.accent, Color(0xFFD82018))),
        topLeft = Offset(c.x - size.width * 0.15f, c.y - size.height * 0.1f),
        size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.26f),
        cornerRadius = CornerRadius(90.dp.toPx(), 90.dp.toPx()),
    )
    drawRoundRect(Color(0xFFFF6A60), Offset(c.x - size.width * 0.05f, c.y - size.height * 0.15f), androidx.compose.ui.geometry.Size(size.width * 0.1f, size.height * 0.06f), CornerRadius(12.dp.toPx(), 12.dp.toPx()))
    drawRoundRect(colors.accent, Offset(c.x - size.width * 0.2f, c.y + size.height * 0.12f), androidx.compose.ui.geometry.Size(size.width * 0.4f, size.height * 0.06f), CornerRadius(26.dp.toPx(), 26.dp.toPx()))
    drawCircle(Color(0xFFD82018), 10.dp.toPx(), Offset(c.x, c.y + size.height * 0.19f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBirthdayHero(colors: OnboardingColors) {
    val cardLeft = size.width * 0.18f
    val cardTop = size.height * 0.36f
    drawSoftShadow(colors, Offset(size.width * 0.5f, size.height * 0.76f), size.width * 0.32f, size.height * 0.055f)
    drawRoundRect(colors.surface, Offset(cardLeft, cardTop), androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.25f), CornerRadius(26.dp.toPx(), 26.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.75f), Offset(cardLeft, cardTop), androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.25f), CornerRadius(26.dp.toPx(), 26.dp.toPx()), style = Stroke(1.dp.toPx()))

    val avatarCenter = Offset(cardLeft + size.width * 0.12f, cardTop + size.height * 0.11f)
    drawCircle(colors.accent.copy(alpha = 0.85f), 22.dp.toPx(), avatarCenter)
    drawCircle(colors.surface, 8.dp.toPx(), avatarCenter - Offset(0f, 3.dp.toPx()))
    drawRoundRect(colors.surface, Offset(avatarCenter.x - 9.dp.toPx(), avatarCenter.y + 2.dp.toPx()), androidx.compose.ui.geometry.Size(18.dp.toPx(), 11.dp.toPx()), CornerRadius(6.dp.toPx(), 6.dp.toPx()))

    val nativeCanvas = drawContext.canvas.nativeCanvas
    val textStartX = cardLeft + size.width * 0.22f
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.primaryText.toArgb()
        textSize = 10.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.secondaryText.toArgb()
        textSize = 8.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.accent.toArgb()
        textSize = 9.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    nativeCanvas.drawText("Alex Smith", textStartX, cardTop + size.height * 0.08f, namePaint)
    nativeCanvas.drawText("Birthday", textStartX, cardTop + size.height * 0.14f, subtitlePaint)
    nativeCanvas.drawText("May 20", textStartX, cardTop + size.height * 0.21f, datePaint)

    drawCake(colors, Offset(cardLeft + size.width * 0.44f, cardTop - size.height * 0.06f))
    drawGift(colors, Offset(cardLeft + size.width * 0.55f, cardTop + size.height * 0.15f))
    drawLockBadge(colors, Offset(cardLeft + size.width * 0.58f, cardTop + size.height * 0.21f))
    drawLeafCluster(colors, Offset(size.width * 0.18f, size.height * 0.68f), -1f)
    drawLeafCluster(colors, Offset(size.width * 0.82f, size.height * 0.65f), 1f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawReadyHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.5f, size.height * 0.47f)
    repeat(5) { index ->
        drawCircle(colors.accent.copy(alpha = 0.1f - index * 0.014f), size.minDimension * (0.18f + index * 0.075f), c)
    }
    drawCircle(
        brush = Brush.verticalGradient(listOf(Color(0xFFFF7168), colors.accent, Color(0xFFD71912))),
        radius = size.minDimension * 0.18f,
        center = c,
    )
    val checkPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(c.x - 28.dp.toPx(), c.y - 2.dp.toPx())
        lineTo(c.x - 7.dp.toPx(), c.y + 19.dp.toPx())
        lineTo(c.x + 28.dp.toPx(), c.y - 19.dp.toPx())
    }
    drawPath(
        path = checkPath,
        color = Color.White,
        style = Stroke(width = 7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    )
    repeat(8) { index ->
        val angle = index * 0.78f
        val x = c.x + kotlin.math.cos(angle) * size.width * 0.33f
        val y = c.y + kotlin.math.sin(angle) * size.height * 0.27f
        if (index % 2 == 0) {
            drawCircle(colors.accent.copy(alpha = 0.8f), 2.5.dp.toPx(), Offset(x, y))
        } else {
            val starPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(x, y - 6.dp.toPx())
                lineTo(x + 2.dp.toPx(), y - 2.dp.toPx())
                lineTo(x + 6.dp.toPx(), y)
                lineTo(x + 2.dp.toPx(), y + 2.dp.toPx())
                lineTo(x, y + 6.dp.toPx())
                lineTo(x - 2.dp.toPx(), y + 2.dp.toPx())
                lineTo(x - 6.dp.toPx(), y)
                lineTo(x - 2.dp.toPx(), y - 2.dp.toPx())
                close()
            }
            drawPath(starPath, colors.accent.copy(alpha = 0.65f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFloatingCard(
    colors: OnboardingColors,
    topLeft: Offset,
    width: Float,
    type: String,
    titleText: String = "",
    subtitleText: String = ""
) {
    val height = width * 0.42f
    drawRoundRect(colors.shadow, topLeft + Offset(0f, 6.dp.toPx()), androidx.compose.ui.geometry.Size(width, height), CornerRadius(12.dp.toPx(), 12.dp.toPx()))
    drawRoundRect(colors.surface, topLeft, androidx.compose.ui.geometry.Size(width, height), CornerRadius(12.dp.toPx(), 12.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.7f), topLeft, androidx.compose.ui.geometry.Size(width, height), CornerRadius(12.dp.toPx(), 12.dp.toPx()), style = Stroke(1.dp.toPx()))
    val dotCenter = topLeft + Offset(15.dp.toPx(), height * 0.5f)
    if (type == "grid") {
        val gridStart = topLeft + Offset(14.dp.toPx(), height * 0.22f)
        val gridWidth = width - 28.dp.toPx()
        val gridHeight = height * 0.56f
        repeat(3) { row ->
            repeat(4) { col ->
                val dotX = gridStart.x + col * (gridWidth / 3f)
                val dotY = gridStart.y + row * (gridHeight / 2f)
                drawCircle(
                    color = if (row == 1 && col == 2) colors.accent else colors.line.copy(alpha = if (colors.isDark) 0.8f else 0.6f),
                    radius = 2.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
    } else {
        when (type) {
            "calendar" -> drawMiniCalendarIcon(colors, dotCenter)
            "bell" -> drawMiniBellIcon(colors, dotCenter)
            "check" -> drawMiniCheckIcon(colors, dotCenter)
            else -> drawCircle(colors.accent, 5.dp.toPx(), dotCenter)
        }
        if (titleText.isNotEmpty()) {
            val nativeCanvas = drawContext.canvas.nativeCanvas
            val textStartX = topLeft.x + 29.dp.toPx()
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.primaryText.toArgb()
                textSize = 9.sp.toPx()
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.secondaryText.toArgb()
                textSize = 7.5.sp.toPx()
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            nativeCanvas.drawText(titleText, textStartX, topLeft.y + height * 0.44f, titlePaint)
            if (subtitleText.isNotEmpty()) {
                nativeCanvas.drawText(subtitleText, textStartX, topLeft.y + height * 0.78f, subtitlePaint)
            }
        } else {
            drawRoundRect(colors.primaryText.copy(alpha = if (colors.isDark) 0.8f else 0.55f), topLeft + Offset(30.dp.toPx(), height * 0.28f), androidx.compose.ui.geometry.Size(width * 0.42f, 4.dp.toPx()), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
            drawRoundRect(colors.secondaryText.copy(alpha = 0.55f), topLeft + Offset(30.dp.toPx(), height * 0.54f), androidx.compose.ui.geometry.Size(width * 0.32f, 4.dp.toPx()), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConnectionDots(colors: OnboardingColors, start: Offset, end: Offset) {
    repeat(9) { index ->
        val t = index / 8f
        val x = start.x + (end.x - start.x) * t
        val y = start.y + (end.y - start.y) * t
        drawCircle(if (index % 2 == 0) colors.accent.copy(alpha = 0.7f) else colors.line, 1.8.dp.toPx(), Offset(x, y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSoftShadow(colors: OnboardingColors, center: Offset, width: Float, height: Float) {
    drawOval(colors.shadow, topLeft = Offset(center.x - width / 2f, center.y - height / 2f), size = androidx.compose.ui.geometry.Size(width, height))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCake(colors: OnboardingColors, topLeft: Offset) {
    drawRoundRect(colors.accent, topLeft + Offset(0f, 18.dp.toPx()), androidx.compose.ui.geometry.Size(58.dp.toPx(), 26.dp.toPx()), CornerRadius(8.dp.toPx(), 8.dp.toPx()))
    drawRoundRect(colors.surface, topLeft + Offset(7.dp.toPx(), 8.dp.toPx()), androidx.compose.ui.geometry.Size(44.dp.toPx(), 14.dp.toPx()), CornerRadius(6.dp.toPx(), 6.dp.toPx()))
    repeat(3) { index ->
        val x = topLeft.x + 15.dp.toPx() + index * 14.dp.toPx()
        drawLine(colors.accent, Offset(x, topLeft.y + 2.dp.toPx()), Offset(x, topLeft.y + 11.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawCircle(Color(0xFFFFD166), 2.dp.toPx(), Offset(x, topLeft.y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGift(colors: OnboardingColors, topLeft: Offset) {
    drawCircle(colors.shadow, 24.dp.toPx(), topLeft + Offset(22.dp.toPx(), 24.dp.toPx()))
    drawRoundRect(colors.elevatedSurface, topLeft, androidx.compose.ui.geometry.Size(44.dp.toPx(), 44.dp.toPx()), CornerRadius(14.dp.toPx(), 14.dp.toPx()))
    drawLine(colors.accent, topLeft + Offset(22.dp.toPx(), 4.dp.toPx()), topLeft + Offset(22.dp.toPx(), 39.dp.toPx()), strokeWidth = 4.dp.toPx())
    drawLine(colors.accent, topLeft + Offset(8.dp.toPx(), 18.dp.toPx()), topLeft + Offset(36.dp.toPx(), 18.dp.toPx()), strokeWidth = 4.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniCalendarIcon(colors: OnboardingColors, center: Offset) {
    val s = 14.dp.toPx()
    val left = center.x - s / 2f
    val top = center.y - s / 2f
    drawRoundRect(colors.accent, Offset(left, top), androidx.compose.ui.geometry.Size(s, s), CornerRadius(3.dp.toPx(), 3.dp.toPx()))
    drawRoundRect(Color.White.copy(alpha = 0.95f), Offset(left + 3.dp.toPx(), top + 5.dp.toPx()), androidx.compose.ui.geometry.Size(8.dp.toPx(), 6.dp.toPx()), CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()))
    drawCircle(colors.accent, 1.2.dp.toPx(), Offset(left + 6.dp.toPx(), top + 8.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniBellIcon(colors: OnboardingColors, center: Offset) {
    drawCircle(colors.accent, 8.dp.toPx(), center)
    drawRoundRect(Color.White, Offset(center.x - 4.5.dp.toPx(), center.y - 4.dp.toPx()), androidx.compose.ui.geometry.Size(9.dp.toPx(), 9.dp.toPx()), CornerRadius(7.dp.toPx(), 7.dp.toPx()))
    drawRoundRect(Color.White, Offset(center.x - 7.dp.toPx(), center.y + 3.dp.toPx()), androidx.compose.ui.geometry.Size(14.dp.toPx(), 3.dp.toPx()), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    drawCircle(Color.White, 1.8.dp.toPx(), Offset(center.x, center.y + 8.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniCheckIcon(colors: OnboardingColors, center: Offset) {
    drawCircle(colors.accent, 8.dp.toPx(), center)
    drawLine(Color.White, center + Offset(-4.dp.toPx(), 0f), center + Offset(-1.dp.toPx(), 4.dp.toPx()), strokeWidth = 2.dp.toPx())
    drawLine(Color.White, center + Offset(-1.dp.toPx(), 4.dp.toPx()), center + Offset(5.dp.toPx(), -5.dp.toPx()), strokeWidth = 2.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLockBadge(colors: OnboardingColors, center: Offset) {
    drawCircle(colors.shadow, 24.dp.toPx(), center + Offset(0f, 5.dp.toPx()))
    drawCircle(colors.elevatedSurface, 23.dp.toPx(), center)
    drawCircle(colors.accent, 15.dp.toPx(), center)
    drawRoundRect(Color.White, Offset(center.x - 6.dp.toPx(), center.y - 1.dp.toPx()), androidx.compose.ui.geometry.Size(12.dp.toPx(), 10.dp.toPx()), CornerRadius(3.dp.toPx(), 3.dp.toPx()))
    drawArc(Color.White, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(center.x - 7.dp.toPx(), center.y - 10.dp.toPx()), size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 14.dp.toPx()), style = Stroke(2.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeafCluster(
    colors: OnboardingColors,
    origin: Offset,
    direction: Float
) {
    val leafColor = if (direction < 0) {
        colors.accent.copy(alpha = 0.35f)
    } else {
        colors.mutedText.copy(alpha = 0.35f)
    }

    val leafCount = 4
    repeat(leafCount) { index ->
        val angle = if (direction < 0) {
            -45f + index * 20f
        } else {
            15f + index * 20f
        }
        val distance = (index * 8).dp.toPx()
        val leafX = origin.x + direction * distance
        val leafY = origin.y - (index * 6).dp.toPx()

        rotate(degrees = angle, pivot = Offset(leafX, leafY)) {
            drawOval(
                color = leafColor,
                topLeft = Offset(leafX - 7.dp.toPx(), leafY - 18.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 36.dp.toPx())
            )
        }
    }

    val accentAngle = if (direction < 0) -25f else 25f
    val mainLeafX = origin.x + direction * 10.dp.toPx()
    val mainLeafY = origin.y + 4.dp.toPx()
    rotate(degrees = accentAngle, pivot = Offset(mainLeafX, mainLeafY)) {
        drawOval(
            color = colors.accent.copy(alpha = 0.85f),
            topLeft = Offset(mainLeafX - 9.dp.toPx(), mainLeafY - 22.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 44.dp.toPx())
        )
    }
}
