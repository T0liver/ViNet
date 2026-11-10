package hu.toliver.vinet.scan

import hu.toliver.vinet.ui.ViNetRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseScanExclusionTest {
    @Test
    fun releaseReplayControlsStayOff() {
        assertFalse(OffDeveloperReplayControls.developerToolsEnabled)
        OffDeveloperReplayControls.toggleDeveloperTools()
        OffDeveloperReplayControls.selectVideoReplay("/sdcard/ViNet/debug/stand-in.mp4", "/sdcard/ViNet/debug/stand-in.gpx")
        OffDeveloperReplayControls.selectLiveCamera()
        assertFalse(OffDeveloperReplayControls.developerToolsEnabled)
    }

    @Test
    fun releaseHudRecordsNothing() {
        NoScanHudRecorder.recordFrame(1L)
        NoScanHudRecorder.recordInference(12f)
        assertTrue(NoScanHudRecorder.hudLines().isEmpty())
    }

    @Test
    fun scanStaysTheStartDestinationWithoutABundledModel() {
        assertEquals("scan", ViNetRoutes.SCAN)
    }
}
