package hu.toliver.vinet.ui.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.toliver.vinet.core.overlay.OverlayBox
import hu.toliver.vinet.ui.ViNetColors

@Composable
fun DetectionOverlayCanvas(
    overlayBoxes: List<OverlayBox>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = ViNetColors.OnVine, fontSize = 14.sp)
    val strokePx = with(LocalDensity.current) { 2.dp.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        overlayBoxes.forEach { box ->
            val color = parseManifestColor(box.colorHex)
            val left = box.detection.box.left * w
            val top = box.detection.box.top * h
            val boxW = (box.detection.box.right - box.detection.box.left) * w
            val boxH = (box.detection.box.bottom - box.detection.box.top) * h
            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(boxW, boxH),
                style = Stroke(width = strokePx),
            )
            val label = "${box.className} ${"%.2f".format(box.detection.score)}"
            val textLayout = textMeasurer.measure(label, labelStyle)
            val padding = 4.dp.toPx()
            val labelTop = (top - textLayout.size.height - padding).coerceAtLeast(0f)
            drawRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(left, labelTop),
                size = Size(textLayout.size.width.toFloat() + padding * 2, textLayout.size.height.toFloat() + padding),
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(left + padding, labelTop + padding / 2),
            )
        }
    }
}

fun parseManifestColor(hex: String): Color {
    val normalized = hex.removePrefix("#")
    require(normalized.length == 6 || normalized.length == 8) { "Expected #RRGGBB manifest color" }
    val argb = if (normalized.length == 6) {
        0xFF000000 or normalized.toLong(16)
    } else {
        normalized.toLong(16)
    }
    return Color(argb)
}
