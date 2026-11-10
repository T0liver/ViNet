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
    val cameraDeniedMessage: String? = null,
    val missingReplayFile: String? = null,
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val previewVisible: Boolean = true,
    val tractorMode: TractorDisplayMode = TractorDisplayMode.DayScreen,
    val debugHudLines: List<String> = emptyList(),
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
        val denied = mutableState.value.cameraDeniedMessage
        mutableState.value = ScanUiState(
            isRunning = false,
            statusLabel = "Stopped",
            cameraDeniedMessage = denied,
        )
    }

    fun setPreviewVisible(visible: Boolean) {
        mutableState.value = mutableState.value.copy(previewVisible = visible)
    }

    fun cycleTractorMode() {
        val current = mutableState.value
        mutableState.value = current.copy(tractorMode = current.tractorMode.next())
    }

    fun noteCameraDenied() {
        if (mutableState.value.isRunning) return
        showCameraDenied()
    }

    fun showCameraDenied() {
        mutableState.value = mutableState.value.copy(
            isRunning = false,
            statusLabel = "Stopped",
            cameraDeniedMessage = CAMERA_REQUIRED_MESSAGE,
            overlayBoxes = emptyList(),
        )
    }

    fun clearCameraDenied() {
        if (mutableState.value.cameraDeniedMessage == null) return
        mutableState.value = mutableState.value.copy(cameraDeniedMessage = null)
    }

    fun showMissingReplayFile(fileName: String) {
        mutableState.value = mutableState.value.copy(
            isRunning = false,
            statusLabel = "Stopped",
            missingReplayFile = fileName,
            overlayBoxes = emptyList(),
        )
    }

    fun clearMissingReplayFile() {
        if (mutableState.value.missingReplayFile == null) return
        mutableState.value = mutableState.value.copy(missingReplayFile = null)
    }

    fun updateDebugHud(lines: List<String>) {
        val current = mutableState.value
        if (!current.isRunning && lines.isEmpty()) return
        mutableState.value = current.copy(debugHudLines = lines)
    }

    fun updateOverlay(overlayBoxes: List<OverlayBox>, frameWidth: Int, frameHeight: Int) {
        val current = mutableState.value
        if (!current.isRunning) return
        mutableState.value = current.copy(
            overlayBoxes = overlayBoxes,
            frameWidth = frameWidth,
            frameHeight = frameHeight,
        )
    }
}
