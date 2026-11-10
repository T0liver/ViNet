package hu.toliver.vinet.scan

/**
 * Shared preview visibility for live scan and replay [FrameSource] sessions.
 * Hiding the preview hides the overlay; the foreground service keeps running.
 */
object ScanPreviewControl {
    fun showCameraPreview(previewVisible: Boolean, tractorMode: TractorDisplayMode): Boolean =
        previewVisible && tractorMode != TractorDisplayMode.ScreenOff

    fun showDetectionOverlay(previewVisible: Boolean): Boolean = previewVisible

    fun applyDimScrim(tractorMode: TractorDisplayMode): Boolean =
        tractorMode == TractorDisplayMode.Dim

    fun blankScreenForTractor(tractorMode: TractorDisplayMode): Boolean =
        tractorMode == TractorDisplayMode.ScreenOff
}
