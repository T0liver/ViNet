package hu.toliver.vinet.devtools

import java.io.File

enum class ScanCaptureMode {
    LiveCamera,
    VideoReplay,
}

data class DebugReplaySettings(
    val developerToolsEnabled: Boolean = false,
    val captureMode: ScanCaptureMode = ScanCaptureMode.LiveCamera,
    val videoPath: String = "",
    val gpxPath: String = "",
)

data class ReplayValidation(
    val sessionStarts: Boolean,
    val missingFileName: String?,
)

fun validateReplayFiles(settings: DebugReplaySettings): ReplayValidation {
    if (settings.captureMode != ScanCaptureMode.VideoReplay) {
        return ReplayValidation(sessionStarts = true, missingFileName = null)
    }
    if (settings.videoPath.isBlank()) {
        return ReplayValidation(sessionStarts = false, missingFileName = "(video not set)")
    }
    if (settings.videoPath.isNotBlank() && !File(settings.videoPath).isFile) {
        return ReplayValidation(sessionStarts = false, missingFileName = settings.videoPath)
    }
    if (settings.gpxPath.isNotBlank() && !File(settings.gpxPath).isFile) {
        return ReplayValidation(sessionStarts = false, missingFileName = settings.gpxPath)
    }
    return ReplayValidation(sessionStarts = true, missingFileName = null)
}

fun replayRequiresCameraPermission(settings: DebugReplaySettings): Boolean =
    !settings.developerToolsEnabled ||
        settings.captureMode != ScanCaptureMode.VideoReplay
