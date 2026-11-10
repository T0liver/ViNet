package hu.toliver.vinet.scan

import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.camera.VideoReplayFrameSource
import hu.toliver.vinet.devtools.DebugReplayPreferences
import hu.toliver.vinet.devtools.DebugScanWiring
import hu.toliver.vinet.devtools.ScanCaptureMode
import hu.toliver.vinet.devtools.validateReplayFiles
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugReplaySessionPorts @Inject constructor(
    private val cameraFrameSource: CameraFrameSource,
    private val preferences: DebugReplayPreferences,
) : ReplaySessionPorts {
    override fun plan(cameraPermissionGranted: Boolean): ScanSessionPlan {
        val settings = preferences.current()
        val replayValidation = validateReplayFiles(settings)
        val replayActive =
            settings.developerToolsEnabled &&
                settings.captureMode == ScanCaptureMode.VideoReplay
        val requiresCamera = !replayActive
        val cameraAllowed =
            !requiresCamera || cameraStartDecision(cameraPermissionGranted).sessionStarts
        val sessionStarts = replayValidation.sessionStarts && cameraAllowed
        val frameSource =
            if (replayActive) {
                VideoReplayFrameSource(settings.videoPath)
            } else {
                cameraFrameSource
            }
        return ScanSessionPlan(
            sessionStarts = sessionStarts,
            missingReplayFile = replayValidation.missingFileName,
            requiresCameraPermission = requiresCamera,
            frameSource = frameSource,
            locationSource = DebugScanWiring.locationSource(settings),
            showDebugHud = settings.developerToolsEnabled,
        )
    }
}
