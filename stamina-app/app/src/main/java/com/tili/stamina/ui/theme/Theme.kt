package com.tili.stamina.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StaminaDarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Zinc950,
    primaryContainer = Emerald950,
    onPrimaryContainer = Emerald400,
    secondary = Emerald400,
    onSecondary = Zinc950,
    surface = Zinc900,
    onSurface = White90,
    onSurfaceVariant = White60,
    background = Zinc950,
    onBackground = White90,
    outline = Zinc800,
    surfaceVariant = Zinc800
)

@Composable
fun StaminaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StaminaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
