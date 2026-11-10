package hu.toliver.vinet.scan

import android.app.Application
import androidx.camera.core.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.core.port.FrameSource
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ScanViewModel @Inject constructor(
    application: Application,
    private val scanStateRepository: ScanStateRepository,
    private val frameSource: FrameSource,
    private val replaySessionPorts: ReplaySessionPorts,
    private val developerReplayControls: DeveloperReplayControls,
) : AndroidViewModel(application) {
    val uiState: StateFlow<ScanUiState> = scanStateRepository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScanUiState(),
    )

    fun onStartClicked(cameraPermissionGranted: Boolean) {
        val plan = replaySessionPorts.plan(cameraPermissionGranted)
        if (!plan.sessionStarts) {
            plan.missingReplayFile?.let { scanStateRepository.showMissingReplayFile(it) }
            if (plan.requiresCameraPermission && !cameraPermissionGranted) {
                scanStateRepository.showCameraDenied()
            }
            return
        }
        scanStateRepository.clearCameraDenied()
        scanStateRepository.clearMissingReplayFile()
        ScanForegroundService.start(getApplication())
    }

    fun noteCameraDenied() {
        scanStateRepository.noteCameraDenied()
    }

    fun clearCameraDenied() {
        scanStateRepository.clearCameraDenied()
    }

    fun stopScan() {
        ScanForegroundService.stop(getApplication())
    }

    fun attachPreview(provider: Preview.SurfaceProvider?, rotation: Int) {
        (frameSource as? CameraFrameSource)?.setSurfaceProvider(provider, rotation)
    }

    fun togglePreviewVisible() {
        val visible = scanStateRepository.state.value.previewVisible
        scanStateRepository.setPreviewVisible(!visible)
    }

    fun cycleTractorMode() {
        scanStateRepository.cycleTractorMode()
    }

    fun developerControls(): DeveloperReplayControls = developerReplayControls

    fun toggleDeveloperTools() {
        developerReplayControls.toggleDeveloperTools()
    }

    fun selectBundledReplay() {
        developerReplayControls.selectVideoReplay(
            videoPath = DEFAULT_DEBUG_VIDEO_PATH,
            gpxPath = DEFAULT_DEBUG_GPX_PATH,
        )
    }

    fun selectLiveCamera() {
        developerReplayControls.selectLiveCamera()
    }

    companion object {
        const val DEFAULT_DEBUG_VIDEO_PATH = "/sdcard/ViNet/debug/stand-in.mp4"
        const val DEFAULT_DEBUG_GPX_PATH = "/sdcard/ViNet/debug/stand-in.gpx"
    }
}
