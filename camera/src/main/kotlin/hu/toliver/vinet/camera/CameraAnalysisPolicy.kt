package hu.toliver.vinet.camera

import android.hardware.camera2.CaptureRequest
import androidx.camera.core.ImageAnalysis
import hu.toliver.vinet.core.config.ConfigSnapshot

/**
 * Capture settings for the live camera. Preferences are not read here; the snapshot is passed in (AD-11).
 */
data class AnalysisConfig(
    val backpressureStrategy: Int,
    val videoStabilizationMode: Int,
    val opticalStabilizationMode: Int,
    val exposureCapNs: Long,
    val focusDistance: Float,
    val aeModeOff: Int,
    val afModeOff: Int,
)

fun analysisConfig(snapshot: ConfigSnapshot): AnalysisConfig =
    AnalysisConfig(
        backpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
        videoStabilizationMode = CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF,
        opticalStabilizationMode = CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF,
        exposureCapNs = snapshot.exposureCapNs,
        focusDistance = snapshot.focusDistance,
        aeModeOff = CaptureRequest.CONTROL_AE_MODE_OFF,
        afModeOff = CaptureRequest.CONTROL_AF_MODE_OFF,
    )

/**
 * Keeps the newest frame and drops anything still waiting. CameraX is set to the same policy.
 */
class LatestFramePolicy<T> {
    private var pending: T? = null

    /** @return the previous pending frame, which the caller must drop. */
    fun offer(frame: T): T? {
        val dropped = pending
        pending = frame
        return dropped
    }

    fun take(): T? = pending.also { pending = null }
}

interface CaptureControl {
    fun backpressure(strategy: Int)
    fun videoStabilization(mode: Int)
    fun opticalStabilization(mode: Int)
    fun manualExposure(timeNs: Long, aeModeOff: Int)
    fun manualFocus(diopters: Float, afModeOff: Int)
}

fun applyCameraControls(snapshot: ConfigSnapshot, control: CaptureControl) {
    val config = analysisConfig(snapshot)
    control.backpressure(config.backpressureStrategy)
    control.videoStabilization(config.videoStabilizationMode)
    control.opticalStabilization(config.opticalStabilizationMode)
    if (config.exposureCapNs > 0L) {
        control.manualExposure(config.exposureCapNs, config.aeModeOff)
    }
    if (config.focusDistance > 0f) {
        control.manualFocus(config.focusDistance, config.afModeOff)
    }
}
