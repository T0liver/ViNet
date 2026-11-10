package hu.toliver.vinet.devtools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DebugReplayHudTest {
    @Test
    fun missingReplayFileIsNamedAndSessionDoesNotStart() {
        val validation =
            validateReplayFiles(
                DebugReplaySettings(
                    developerToolsEnabled = true,
                    captureMode = ScanCaptureMode.VideoReplay,
                    videoPath = "/tmp/missing-clip.mp4",
                    gpxPath = "",
                ),
            )
        assertFalse(validation.sessionStarts)
        assertEquals("/tmp/missing-clip.mp4", validation.missingFileName)
    }

    @Test
    fun debugHudShowsThreeLines() {
        val lines =
            DebugHudFormatter.lines(
                DebugHudSnapshot(fps = 12.3f, inferenceLatencyMs = 18.6f, temperatureCelsius = 38f),
            )
        assertEquals(3, lines.size)
        assertEquals("FPS: 12.3", lines[0])
        assertEquals("Inference latency: 18.6 ms", lines[1])
        assertEquals("Temperature: 38 °C", lines[2])
    }

    @Test
    fun hudSamplerTracksLatencyAndFps() {
        val sampler = DebugHudSampler(temperatureCelsius = { 41f })
        sampler.recordFrame(0L)
        sampler.recordFrame(100_000_000L)
        sampler.recordInference(12.5f)
        val snapshot = sampler.snapshot()
        assertEquals(12.5f, snapshot.inferenceLatencyMs, 0.01f)
        assertEquals(41f, snapshot.temperatureCelsius, 0.01f)
        assertEquals(10f, snapshot.fps, 0.5f)
    }

    @Test
    fun replaySkipsCameraPermissionWhenDeveloperReplayIsActive() {
        assertFalse(
            replayRequiresCameraPermission(
                DebugReplaySettings(
                    developerToolsEnabled = true,
                    captureMode = ScanCaptureMode.VideoReplay,
                    videoPath = "clip.mp4",
                ),
            ),
        )
    }
}
