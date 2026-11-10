package hu.toliver.vinet.scan

import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.camera.VideoReplayFrameSource
import hu.toliver.vinet.devtools.DebugReplayPreferences
import hu.toliver.vinet.devtools.ScanCaptureMode
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugReplaySessionPortsTest {
    @Test
    fun debugReplaySessionPlanSkipsCameraPermission() {
        val video = File.createTempFile("clip", ".mp4")
        val gpx = File.createTempFile("track", ".gpx")
        gpx.writeText(
            """<gpx><trk><trkseg><trkpt lat="1" lon="2"><time>2026-06-01T10:00:00Z</time></trkpt></trkseg></trk></gpx>""",
        )
        video.writeBytes(byteArrayOf(0x00))
        val preferences = DebugReplayPreferences()
        preferences.update {
            it.copy(
                developerToolsEnabled = true,
                captureMode = ScanCaptureMode.VideoReplay,
                videoPath = video.absolutePath,
                gpxPath = gpx.absolutePath,
            )
        }
        val ports = DebugReplaySessionPorts(CameraFrameSource(), preferences)
        val plan = ports.plan(cameraPermissionGranted = false)
        assertFalse(plan.requiresCameraPermission)
        assertTrue(plan.sessionStarts)
        assertTrue(plan.frameSource is VideoReplayFrameSource)
        video.delete()
        gpx.delete()
    }
}
