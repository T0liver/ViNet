package hu.toliver.vinet.scan

import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.location.IdleLocationSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveReplaySessionPorts @Inject constructor(
    private val cameraFrameSource: CameraFrameSource,
) : ReplaySessionPorts {
    private val idleLocation = IdleLocationSource()

    override fun plan(cameraPermissionGranted: Boolean): ScanSessionPlan {
        val decision = cameraStartDecision(cameraPermissionGranted)
        return ScanSessionPlan(
            sessionStarts = decision.sessionStarts,
            missingReplayFile = null,
            requiresCameraPermission = true,
            frameSource = cameraFrameSource,
            locationSource = idleLocation,
            showDebugHud = false,
        )
    }
}
