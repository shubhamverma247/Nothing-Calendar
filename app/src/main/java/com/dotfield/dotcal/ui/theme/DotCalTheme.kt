package com.dotfield.dotcal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NRed = Color(0xFFFF3B30)
val NBlack = Color(0xFF000000)
val NWhite = Color(0xFFFFFFFF)
val NGray = Color(0xFF666666)
val NLine = Color(0xFF1A1A1A)
val NDim = Color(0xFF333333)

private val colors = darkColorScheme(
    primary = NRed,
    background = NBlack,
    surface = NBlack,
    onPrimary = NBlack,
    onBackground = NWhite,
    onSurface = NWhite,
)

@Composable
fun DotCalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
