package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = SingularityCyan,
    secondary = AccretionPurple,
    tertiary = NeonViolet,
    background = DeepSpaceBlack,
    surface = NebulaCard,
    onPrimary = DeepSpaceBlack,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = StarWhite,
    onSurface = StarWhite,
    surfaceVariant = NebulaCard,
    onSurfaceVariant = MutedDust
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium cosmic dark mode
    dynamicColor: Boolean = false, // Preserve original brand colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
