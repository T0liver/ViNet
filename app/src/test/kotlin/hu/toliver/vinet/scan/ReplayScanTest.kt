package hu.toliver.vinet.scan

import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.camera.ListedReplayFrameProvider
import hu.toliver.vinet.camera.VideoReplayFrameSource
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.overlay.OverlayBox
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackClass
import hu.toliver.vinet.core.pack.PackInputFormat
import hu.toliver.vinet.devtools.DebugReplaySettings
import hu.toliver.vinet.devtools.ScanCaptureMode
import hu.toliver.vinet.devtools.validateReplayFiles
import hu.toliver.vinet.inference.FakeDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayScanTest {
    private val pack = ModelPack(
        packId = "replay-app-test",
        modelVersion = "0",
        family = "test",
        tier = "N",
        modelFileName = "model.tflite",
        sha256 = "00",
        backend = "litert",
        inputWidth = 16,
        inputHeight = 16,
        inputFormat = PackInputFormat("RGB", "0-1", true, 114),
        outputFormat = "yolo26-end2end",
        classes = listOf(PackClass(0, "Replay grape leaf", "#4B0082", 0.42f)),
        minTrackFrames = 3,
        notes = "",
    )

    @Test
    fun debugVideoReplayUsesServicePublishPathAndLabelsBoxes() {
        val sink = MemorySink()
        val events = CountingEventWriter()
        var boxes = emptyList<OverlayBox>()
        val frame =
            Frame(width = 8, height = 8, rotationDegrees = 0, timestampNs = 8L, pixels = ByteArray(8 * 8 * 3))
        onLiveFrame(frame, FakeDetector(pack), pack, sink, events) { boxes = it }
        assertEquals(1, boxes.size)
        assertEquals("Replay grape leaf", boxes[0].className)
        assertEquals(0, events.writes)
        assertTrue(sink.fullJpeg != null)
    }

    @Test
    fun missingReplayFileMessageNamesTheFileAndSessionDoesNotStart() {
        val message = missingReplayFileMessage("/data/missing.gpx")
        assertEquals("Missing replay file: /data/missing.gpx", message)
        val repo = ScanStateRepository()
        repo.showMissingReplayFile("/data/missing.gpx")
        assertFalse(repo.state.value.isRunning)
        assertEquals("/data/missing.gpx", repo.state.value.missingReplayFile)
        val validation =
            validateReplayFiles(
                DebugReplaySettings(
                    developerToolsEnabled = true,
                    captureMode = ScanCaptureMode.VideoReplay,
                    gpxPath = "/data/missing.gpx",
                ),
            )
        assertFalse(validation.sessionStarts)
    }

    @Test
    fun replayStillUsesPreviewHideAndScreenOffNotification() {
        val repo = ScanStateRepository()
        repo.onServiceRunning(emptyList())
        repo.setPreviewVisible(false)
        assertTrue(repo.state.value.isRunning)
        assertFalse(ScanPreviewControl.showDetectionOverlay(repo.state.value.previewVisible))
        val content =
            ScanSessionNotification.content(
                sessionRunning = true,
                tractorMode = TractorDisplayMode.ScreenOff,
            )
        assertEquals("Screen off. Scanning continues.", content.bodyText)
        assertTrue(content.includesStopAction)
    }

    @Test
    fun listedVideoReplayFrameSourceMatchesEncoderExpectations() {
        val source =
            VideoReplayFrameSource(
                "field.mp4",
                ListedReplayFrameProvider(
                    listOf(
                        Frame(
                            width = 12,
                            height = 10,
                            rotationDegrees = 0,
                            timestampNs = 2L,
                            pixels = ByteArray(12 * 10 * 3),
                        ),
                    ),
                ),
            )
        assertEquals("field.mp4", source.videoPath)
    }
}
