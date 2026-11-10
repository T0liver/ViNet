package hu.toliver.vinet.scan

import hu.toliver.vinet.devtools.DebugReplayPreferences
import hu.toliver.vinet.devtools.ScanCaptureMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugDeveloperReplayControls @Inject constructor(
    private val preferences: DebugReplayPreferences,
) : DeveloperReplayControls {
    override val developerToolsEnabled: Boolean
        get() = preferences.current().developerToolsEnabled

    override fun toggleDeveloperTools() {
        preferences.update { it.copy(developerToolsEnabled = !it.developerToolsEnabled) }
    }

    override fun selectVideoReplay(videoPath: String, gpxPath: String) {
        preferences.update {
            it.copy(
                captureMode = ScanCaptureMode.VideoReplay,
                videoPath = videoPath,
                gpxPath = gpxPath,
            )
        }
    }

    override fun selectLiveCamera() {
        preferences.update { it.copy(captureMode = ScanCaptureMode.LiveCamera) }
    }
}
