package hu.toliver.vinet.ui

import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException

private const val MARK_ASSET = "vinet_c.svg"

@Composable
fun LeafMark(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pictureDrawable = remember(MARK_ASSET) {
        try {
            val svg = SVG.getFromAsset(context.assets, MARK_ASSET)
            android.graphics.drawable.PictureDrawable(svg.renderToPicture())
        } catch (_: SVGParseException) {
            null
        } catch (_: IOException) {
            null
        }
    }

    if (pictureDrawable != null) {
        AndroidView(
            modifier = modifier.size(size),
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setImageDrawable(pictureDrawable)
                }
            },
        )
    }
}
