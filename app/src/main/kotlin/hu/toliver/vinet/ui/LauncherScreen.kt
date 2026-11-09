package hu.toliver.vinet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import hu.toliver.vinet.R
import hu.toliver.vinet.core.ViNetProduct

private val FrauncesWordmark = FontFamily(
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
)

private val WordmarkStyle = TextStyle(
    fontFamily = FrauncesWordmark,
    fontSize = 36.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 40.sp,
    letterSpacing = (-0.02).em,
)

@Composable
fun LauncherScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ViNetColors.Surface),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(ViNetColors.SurfaceRaised, RoundedCornerShape(16.dp))
                    .padding(12.dp),
            ) {
                LeafMark(size = 108.dp)
            }
            Text(
                text = ViNetProduct.DISPLAY_NAME,
                style = WordmarkStyle,
                color = ViNetColors.Vine,
            )
        }
    }
}
