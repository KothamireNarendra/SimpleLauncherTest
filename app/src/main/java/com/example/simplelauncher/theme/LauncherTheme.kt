package com.example.simplelauncher

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val launcher_red = Color(0xFFE30425)
private val launcher_white = Color.White
private val launcher_purple_700 = Color(0xFF720D5D)
private val launcher_purple_800 = Color(0xFF5D1049)
private val launcher_purple_900 = Color(0xFF4E0D3A)

val launcherColors = lightColors(
    primary = launcher_purple_800,
    secondary = launcher_red,
    surface = launcher_purple_900,
    onSurface = launcher_white,
    primaryVariant = launcher_purple_700
)

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = launcherColors) {
        content()
    }
}
