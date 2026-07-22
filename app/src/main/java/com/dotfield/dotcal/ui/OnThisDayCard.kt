package com.dotfield.dotcal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotfield.dotcal.data.insights.OnThisDayMemory

private const val ON_THIS_DAY_MAX_VISIBLE = 3

/**
 * "On This Day" memory card. Surfaces past events sharing today's month/day. Rendered at the top
 * of Day and Agenda views only when [memories] is non-empty. Free feature — no paywall gate.
 */
@Composable
internal fun OnThisDayCard(
    memories: List<OnThisDayMemory>,
    palette: DotCalPalette,
    onMemoryClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (memories.isEmpty()) return
    val cardShape = RoundedCornerShape(16.dp)
    val visible = memories.take(ON_THIS_DAY_MAX_VISIBLE)
    val overflow = memories.size - visible.size
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, cardShape)
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "ON THIS DAY",
                color = palette.accent,
                fontFamily = mono,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.weight(1f),
            )
            OnThisDayDismissButton(tint = palette.secondaryText, onClick = onDismiss)
        }
        Spacer(modifier = Modifier.height(10.dp))
        visible.forEachIndexed { index, memory ->
            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
            OnThisDayRow(
                memory = memory,
                palette = palette,
                onClick = { onMemoryClick(memory.eventId) },
            )
        }
        if (overflow > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "+$overflow more",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun OnThisDayRow(
    memory: OnThisDayMemory,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                memory.title,
                color = palette.primaryText,
                fontFamily = mono,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                memory.subtitle,
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        EventCardChevron(tint = palette.eventCardChevron)
    }
}

@Composable
private fun OnThisDayDismissButton(tint: Color, onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(7.dp),
    ) {
        val strokeWidth = 1.8.dp.toPx()
        drawLine(tint, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = strokeWidth)
        drawLine(tint, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = strokeWidth)
    }
}
