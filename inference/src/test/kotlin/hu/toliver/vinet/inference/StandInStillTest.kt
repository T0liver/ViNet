package hu.toliver.vinet.inference

import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.overlay.overlayBoxesForFrame
import hu.toliver.vinet.core.pack.PackManifestParser
import hu.toliver.vinet.inference.litert.JvmLiteRtSession
import hu.toliver.vinet.inference.litert.StandInStillScan
import hu.toliver.vinet.inference.preprocess.ManifestPreprocess
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StandInStillTest {
    private lateinit var scan: StandInStillScan

    @Before
    fun loadStandIn() {
        val dir = standInDir()
        val manifest = PackManifestParser.parse(dir.resolve("manifest.json").readText())
        val model = dir.resolve(manifest.modelFileName).readBytes()
        scan = StandInStillScan(manifest, JvmLiteRtSession(model))
    }

    @After
    fun closeScan() {
        scan.close()
    }

    @Test
    fun labeledStillShowsManifestClassScoreAndColor() {
        val frame = rgbFrame(width = 8, height = 8, fill = 255, timestampNs = 22L)

        val detections = scan.detect(frame)
        val boxes = overlayBoxesForFrame(detections, scan.pack)

        assertEquals(2, detections.size)
        assertEquals(2, boxes.size)
        boxes.forEach { box ->
            assertEquals("Downy mildew", box.className)
            assertEquals("#2E7D32", box.colorHex)
            assertEquals(1f, box.detection.score, 1e-5f)
            assertEquals(22L, box.detection.frameTimestampNs)
        }
        assertEquals(0.15f, boxes[0].detection.box.left, 1e-5f)
        assertEquals(0.20f, boxes[0].detection.box.top, 1e-5f)
        assertEquals(0.45f, boxes[0].detection.box.right, 1e-5f)
        assertEquals(0.55f, boxes[0].detection.box.bottom, 1e-5f)
        assertEquals(0.50f, boxes[1].detection.box.left, 1e-5f)
        assertEquals(0.30f, boxes[1].detection.box.top, 1e-5f)
        assertEquals(0.85f, boxes[1].detection.box.right, 1e-5f)
        assertEquals(0.75f, boxes[1].detection.box.bottom, 1e-5f)
    }

    @Test
    fun blankStillReturnsNoDetectionsAndNoDrawnBox() {
        val frame = rgbFrame(width = 8, height = 8, fill = 0, timestampNs = 9L)

        val detections = scan.detect(frame)
        val boxes = overlayBoxesForFrame(detections, scan.pack)

        assertTrue(detections.isEmpty())
        assertTrue(boxes.isEmpty())
    }

    @Test
    fun letterboxPadAndRgbComeFromTheManifest() {
        val wide = rgbFrame(width = 8, height = 4, fill = 0, timestampNs = 1L)
        val input = ManifestPreprocess.toModelInput(wide, scan.pack)
        val pad = 114f / 255f
        assertEquals(pad, input[0], 1e-5f)
        assertEquals(pad, input[1], 1e-5f)
        assertEquals(pad, input[2], 1e-5f)

        val red = ByteArray(8 * 8 * 3)
        red[0] = 255.toByte()
        val colored = Frame(8, 8, 0, 2L, red)
        val rgb = ManifestPreprocess.toModelInput(colored, scan.pack)
        assertEquals(1f, rgb[0], 1e-5f)
        assertEquals(0f, rgb[1], 1e-5f)
        assertEquals(0f, rgb[2], 1e-5f)
    }

    private fun rgbFrame(width: Int, height: Int, fill: Int, timestampNs: Long): Frame {
        val pixels = ByteArray(width * height * 3) { fill.toByte() }
        return Frame(width, height, 0, timestampNs, pixels)
    }

    private fun standInDir(): File {
        val candidates = listOf(
            File("src/debug/assets/stand-in"),
            File("inference/src/debug/assets/stand-in"),
        )
        return candidates.firstOrNull { it.isDirectory }
            ?: error("debug stand-in pack is missing")
    }
}
