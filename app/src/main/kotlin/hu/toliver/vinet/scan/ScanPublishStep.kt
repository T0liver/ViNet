package hu.toliver.vinet.scan

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.overlay.OverlayBox
import hu.toliver.vinet.core.overlay.overlayBoxesForFrame
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector

/** One capture-pipeline detect step; only the service runs this (AD-1). */
data class ScanPublishResult(
    val rawDetections: List<Detection>,
    val overlayBoxes: List<OverlayBox>,
)

fun runScanPublishStep(detector: Detector, frame: Frame, pack: ModelPack): ScanPublishResult {
    val detections = detector.detect(frame)
    return ScanPublishResult(
        rawDetections = detections,
        overlayBoxes = overlayBoxesForFrame(detections, pack),
    )
}
