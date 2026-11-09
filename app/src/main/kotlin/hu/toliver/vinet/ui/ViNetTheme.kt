package hu.toliver.vinet.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ViNetLightColors = lightColorScheme(
    background = ViNetColors.Surface,
    surface = ViNetColors.Surface,
    onBackground = ViNetColors.Ink,
    onSurface = ViNetColors.Ink,
    primary = ViNetColors.Vine,
    onPrimary = ViNetColors.OnVine,
)

@Composable
fun ViNetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ViNetLightColors,
        content = content,
    )
}
