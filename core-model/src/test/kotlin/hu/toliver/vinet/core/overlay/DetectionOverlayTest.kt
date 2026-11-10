package hu.toliver.vinet.core.overlay

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.NormalizedBox
import hu.toliver.vinet.core.pack.PackClass
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackInputFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectionOverlayTest {
    private val pack = ModelPack(
        packId = "overlay-test",
        modelVersion = "0",
        family = "test",
        tier = "N",
        modelFileName = "model.tflite",
        sha256 = "00",
        backend = "litert",
        inputWidth = 640,
        inputHeight = 640,
        inputFormat = PackInputFormat("RGB", "0-1", true, 114),
        outputFormat = "yolo26-end2end",
        classes = listOf(
            PackClass(0, "Overlay test class", "#4B0082", 0.42f),
        ),
        minTrackFrames = 3,
        notes = "",
    )

    @Test
    fun detectionBelowManifestMinScoreProducesNoDrawnBox() {
        val below = Detection(
            classId = 0,
            score = 0.41f,
            box = NormalizedBox(0.2f, 0.2f, 0.5f, 0.5f),
            frameTimestampNs = 1L,
        )

        val boxes = overlayBoxesForFrame(listOf(below), pack)

        assertTrue(boxes.isEmpty())
    }

    @Test
    fun detectionAtManifestMinScoreIsDrawn() {
        val atThreshold = Detection(
            classId = 0,
            score = 0.42f,
            box = NormalizedBox(0.2f, 0.2f, 0.5f, 0.5f),
            frameTimestampNs = 1L,
        )

        val boxes = overlayBoxesForFrame(listOf(atThreshold), pack)

        assertEquals(1, boxes.size)
        assertEquals("Overlay test class", boxes[0].className)
        assertEquals("#4B0082", boxes[0].colorHex)
    }

    @Test
    fun emptyDetectionListDoesNotInventBoxes() {
        assertTrue(overlayBoxesForFrame(emptyList(), pack).isEmpty())
    }
}
