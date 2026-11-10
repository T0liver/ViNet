package hu.toliver.vinet.scan

import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.core.port.LocationSource

data class ScanSessionPlan(
    val sessionStarts: Boolean,
    val missingReplayFile: String?,
    val requiresCameraPermission: Boolean,
    val frameSource: FrameSource,
    val locationSource: LocationSource,
    val showDebugHud: Boolean,
)

interface ReplaySessionPorts {
    fun plan(cameraPermissionGranted: Boolean): ScanSessionPlan
}
