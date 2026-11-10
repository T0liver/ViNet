package hu.toliver.vinet.scan

import android.provider.Settings
import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.camera.SensorFrameBuffer
import hu.toliver.vinet.core.config.ConfigSnapshot
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.overlay.OverlayBox
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackClass
import hu.toliver.vinet.core.pack.PackInputFormat
import hu.toliver.vinet.inference.FakeDetector
import hu.toliver.vinet.ui.scan.fittedContentRect
import hu.toliver.vinet.inference.preprocess.ManifestPreprocess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveCameraScanTest {
    private val pack = ModelPack(
        packId = "live-test",
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
        classes = listOf(PackClass(0, "Scripted grape leaf", "#4B0082", 0.42f)),
        minTrackFrames = 3,
        notes = "",
    )

    @Test
    fun exposureCapAndFocusDistanceNamesAreRead() {
        val seen = mutableListOf<String>()
        val snapshot = loadConfigSnapshot(
            object : ConfigPreferenceReader {
                override fun getLong(name: String): Long? {
                    seen += name
                    return if (name == "exposure-cap") 5_000_000L else null
                }

                override fun getFloat(name: String): Float? {
                    seen += name
                    return if (name == "focus-distance") 1.5f else null
                }
            },
        )
        assertEquals(listOf("exposure-cap", "focus-distance"), seen)
        assertEquals(5_000_000L, snapshot.exposureCapNs)
        assertEquals(1.5f, snapshot.focusDistance, 0.0001f)
        assertEquals("exposure-cap", ConfigSnapshot.EXPOSURE_CAP_NAME)
        assertEquals("focus-distance", ConfigSnapshot.FOCUS_DISTANCE_NAME)
    }

    @Test
    fun deniedCameraPermissionDoesNotStartAndUsesTheRequiredCopy() {
        var started = false
        val decision = cameraStartDecision(cameraPermissionGranted = false)
        if (decision.sessionStarts) started = true
        assertFalse(started)
        assertFalse(decision.sessionStarts)
        assertEquals("Camera is required to scan the canopy.", decision.message)
        assertEquals("Camera is required to scan the canopy.", CAMERA_REQUIRED_MESSAGE)
        assertTrue(decision.openSystemSettings)
        val request = cameraPermissionSettings("hu.toliver.vinet")
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, request.action)
        assertEquals("android.settings.APPLICATION_DETAILS_SETTINGS", request.action)
        assertEquals("package:hu.toliver.vinet", request.dataUri)

        val state = ScanStateRepository()
        state.showCameraDenied()
        assertEquals("Camera is required to scan the canopy.", state.state.value.cameraDeniedMessage)
        assertFalse(state.state.value.isRunning)
    }

    @Test
    fun overlayUsesTheFittedPreviewOfThatFrame() {
        val fitted = fittedContentRect(viewWidth = 100f, viewHeight = 200f, frameWidth = 100, frameHeight = 50)
        assertEquals(0f, fitted.left, 0.01f)
        assertEquals(75f, fitted.top, 0.01f)
        assertEquals(100f, fitted.width, 0.01f)
        assertEquals(50f, fitted.height, 0.01f)
        val full = fittedContentRect(100f, 200f, 0, 0)
        assertEquals(0f, full.left, 0.01f)
        assertEquals(200f, full.height, 0.01f)
    }

    @Test
    fun grantedCameraPermissionStarts() {
        val decision = cameraStartDecision(cameraPermissionGranted = true)
        assertTrue(decision.sessionStarts)
        assertNull(decision.message)
        assertFalse(decision.openSystemSettings)
    }

    @Test
    fun scriptedFrameStillPublishesOneBoxAndWritesNoEvent() {
        val events = CountingEventWriter()
        val sink = MemorySink()
        var boxes = emptyList<OverlayBox>()
        onLiveFrame(
            frame = Frame(width = 640, height = 480, rotationDegrees = 0, timestampNs = 7L, pixels = null),
            detector = FakeDetector(pack),
            pack = pack,
            sink = sink,
            events = events,
        ) { boxes = it }
        assertEquals(1, boxes.size)
        assertEquals("Scripted grape leaf", boxes[0].className)
        assertEquals(0, events.writes)
        assertNull(sink.fullJpeg)
    }

    @Test(timeout = 20_000)
    fun encodeBeforeReleaseProducesJpegThumbAndWritesNoEvent() {
        val source = CameraFrameSource()
        val events = CountingEventWriter()
        val sink = MemorySink()
        val width = 32
        val height = 24
        val rgb = ByteArray(width * height * 3)
        for (pixel in 0 until width * height) {
            rgb[pixel * 3] = 220.toByte()
            rgb[pixel * 3 + 1] = 10
            rgb[pixel * 3 + 2] = 10
        }
        val buffer = SensorFrameBuffer(
            width = width,
            height = height,
            rotationDegrees = 0,
            rgb = rgb,
            timestampNs = 99L,
        )
        var failure: Throwable? = null
        var sawBoxes = false
        val scope = CoroutineScope(Dispatchers.Default)
        val job = scope.launch {
            source.frames.collect { frame ->
                try {
                    assertFalse(buffer.released)
                    assertEquals(0, frame.rotationDegrees)
                    assertEquals(width, frame.width)
                    assertEquals(height, frame.height)
                    val modelInput = ManifestPreprocess.toModelInput(frame, pack)
                    assertEquals(pack.inputWidth * pack.inputHeight * 3, modelInput.size)
                    onLiveFrame(frame, FakeDetector(pack), pack, sink, events) { boxes ->
                        assertEquals(99L, boxes.single().detection.frameTimestampNs)
                        assertEquals("Scripted grape leaf", boxes.single().className)
                        sawBoxes = true
                    }
                } catch (error: Throwable) {
                    failure = error
                }
            }
        }
        try {
            source.deliver(buffer)
        } finally {
            job.cancel()
            scope.cancel()
        }
        failure?.let { throw it }
        assertTrue(sawBoxes)
        assertTrue(buffer.released)
        assertEquals(0, events.writes)
        val full = requireNotNull(sink.fullJpeg)
        val thumb = requireNotNull(sink.cropThumbnail)
        val fullSize = jpegSize(full)
        val thumbSize = jpegSize(thumb)
        assertEquals(width, fullSize.first)
        assertEquals(height, fullSize.second)
        assertTrue(thumbSize.first < fullSize.first || thumbSize.second < fullSize.second)
        assertFalse(full.contentEquals(thumb))
    }

    private fun jpegSize(jpeg: ByteArray): Pair<Int, Int> {
        assertTrue(jpeg.size > 10)
        assertEquals(0xFF.toByte(), jpeg[0])
        assertEquals(0xD8.toByte(), jpeg[1])
        assertEquals(0xFF.toByte(), jpeg[jpeg.lastIndex - 1])
        assertEquals(0xD9.toByte(), jpeg.last())
        var index = 2
        while (index < jpeg.size - 9) {
            if (jpeg[index] == 0xFF.toByte() && jpeg[index + 1] == 0xC0.toByte()) {
                val height = ((jpeg[index + 5].toInt() and 0xFF) shl 8) or (jpeg[index + 6].toInt() and 0xFF)
                val width = ((jpeg[index + 7].toInt() and 0xFF) shl 8) or (jpeg[index + 8].toInt() and 0xFF)
                return width to height
            }
            index++
        }
        error("JPEG has no SOF0 marker")
    }
}

internal class CountingEventWriter : EventWriter {
    var writes: Int = 0
    override fun write(event: hu.toliver.vinet.core.model.Event) {
        writes++
    }
}

internal class MemorySink : CaptureByteSink {
    var fullJpeg: ByteArray? = null
    var cropThumbnail: ByteArray? = null
    override fun hold(fullJpeg: ByteArray, cropThumbnail: ByteArray) {
        this.fullJpeg = fullJpeg
        this.cropThumbnail = cropThumbnail
    }
}
