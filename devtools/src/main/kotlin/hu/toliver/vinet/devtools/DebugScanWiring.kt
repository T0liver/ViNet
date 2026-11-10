package hu.toliver.vinet.devtools

import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.camera.VideoReplayFrameSource
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.core.port.LocationSource
import hu.toliver.vinet.inference.FakeDetector
import hu.toliver.vinet.location.GpxReplayLocationSource
import hu.toliver.vinet.location.IdleLocationSource

/**
 * Selects debug capture ports for the single foreground-service pipeline (AD-1).
 */
object DebugScanWiring {
    fun detector(pack: ModelPack): Detector = FakeDetector(pack)

    fun frameSource(
        settings: DebugReplaySettings,
        liveCamera: CameraFrameSource,
    ): FrameSource {
        if (!settings.developerToolsEnabled || settings.captureMode != ScanCaptureMode.VideoReplay) {
            return liveCamera
        }
        return VideoReplayFrameSource(settings.videoPath)
    }

    fun locationSource(settings: DebugReplaySettings): LocationSource {
        if (!settings.developerToolsEnabled || settings.captureMode != ScanCaptureMode.VideoReplay) {
            return IdleLocationSource()
        }
        if (settings.gpxPath.isBlank()) {
            return IdleLocationSource()
        }
        return GpxReplayLocationSource(settings.gpxPath)
    }

    fun hudSampler(
        thermalStatusReader: ThermalStatusReader,
    ): DebugHudSampler = DebugHudSampler(temperatureCelsius = { thermalStatusReader.readCelsius() })
}
