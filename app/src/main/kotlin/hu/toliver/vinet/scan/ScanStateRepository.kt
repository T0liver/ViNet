package hu.toliver.vinet.scan

import hu.toliver.vinet.core.overlay.OverlayBox
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ScanUiState(
    val isRunning: Boolean = false,
    val statusLabel: String = "Stopped",
    val overlayBoxes: List<OverlayBox> = emptyList(),
)

@Singleton
class ScanStateRepository @Inject constructor() {
    private val mutableState = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = mutableState.asStateFlow()

    fun onServiceRunning(overlayBoxes: List<OverlayBox>) {
        mutableState.value = ScanUiState(
            isRunning = true,
            statusLabel = "Running",
            overlayBoxes = overlayBoxes,
        )
    }

    fun onServiceStopped() {
        mutableState.value = ScanUiState(
            isRunning = false,
            statusLabel = "Stopped",
            overlayBoxes = emptyList(),
        )
    }

    fun updateOverlay(overlayBoxes: List<OverlayBox>) {
        val current = mutableState.value
        if (!current.isRunning) return
        mutableState.value = current.copy(overlayBoxes = overlayBoxes)
    }
}
