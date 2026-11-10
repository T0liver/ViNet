package hu.toliver.vinet.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import hu.toliver.vinet.scan.DeveloperReplayControls
import hu.toliver.vinet.scan.ScanViewModel
import hu.toliver.vinet.ui.ViNetColors

@Composable
fun DebugScanDeveloperSlot(
    viewModel: ScanViewModel,
    developerControls: DeveloperReplayControls,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = viewModel::toggleDeveloperTools,
            colors = ButtonDefaults.buttonColors(
                containerColor = ViNetColors.SurfaceRaised,
                contentColor = ViNetColors.Ink,
            ),
        ) {
            Text(if (developerControls.developerToolsEnabled) "Developer tools on" else "Developer tools off")
        }
        if (developerControls.developerToolsEnabled) {
            Button(
                onClick = viewModel::selectBundledReplay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.SurfaceRaised,
                    contentColor = ViNetColors.Ink,
                ),
            ) {
                Text("Use video + GPX replay")
            }
            Button(
                onClick = viewModel::selectLiveCamera,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.SurfaceRaised,
                    contentColor = ViNetColors.Ink,
                ),
            ) {
                Text("Use live camera")
            }
        }
    }
}
