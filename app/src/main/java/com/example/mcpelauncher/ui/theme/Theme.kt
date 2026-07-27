package com.example.mcpelauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val BlockColorScheme = darkColorScheme(
    primary = GrassGreen,
    onPrimary = OffWhite,
    secondary = GoldOre,
    onSecondary = Obsidian,
    tertiary = DiamondCyan,
    background = DeepSlate,
    onBackground = OffWhite,
    surface = Obsidian,
    onSurface = OffWhite,
    surfaceVariant = StoneGray,
    outline = DirtBrown,
)

val BlockTitleStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    letterSpacing = 1.5.sp
)

@Composable
fun BlockLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BlockColorScheme,
        typography = MaterialTheme.typography.copy(
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
        ),
        content = content
    )
}
