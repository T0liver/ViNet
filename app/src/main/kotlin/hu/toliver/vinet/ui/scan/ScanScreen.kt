package hu.toliver.vinet.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.toliver.vinet.scan.ScanViewModel
import hu.toliver.vinet.ui.ViNetColors

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ViNetColors.Surface),
    ) {
        DetectionOverlayCanvas(
            overlayBoxes = uiState.overlayBoxes,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Session status: ${uiState.statusLabel}",
                color = ViNetColors.Ink,
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = viewModel::startScan,
                enabled = !uiState.isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.Vine,
                    contentColor = ViNetColors.OnVine,
                ),
            ) {
                Text("Start")
            }
            Button(
                onClick = viewModel::stopScan,
                enabled = uiState.isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.Vine,
                    contentColor = ViNetColors.OnVine,
                ),
            ) {
                Text("Stop")
            }
        }
    }
}
