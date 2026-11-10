package hu.toliver.vinet.scan

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.model.NormalizedBox
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackClass
import hu.toliver.vinet.core.pack.PackInputFormat
import hu.toliver.vinet.core.port.Detector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanPublishStepTest {
    private val pack = ModelPack(
        packId = "publish-test",
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
            PackClass(0, "Scripted grape leaf", "#4B0082", 0.42f),
        ),
        minTrackFrames = 3,
        notes = "",
    )

    @Test
    fun servicePublishStepExposesOneScriptedBox() {
        val detector = object : Detector {
            override val pack: ModelPack = this@ScanPublishStepTest.pack

            override fun detect(frame: Frame): List<Detection> =
                listOf(
                    Detection(
                        classId = 0,
                        score = 0.85f,
                        box = NormalizedBox(0.1f, 0.1f, 0.4f, 0.4f),
                        frameTimestampNs = frame.timestampNs,
                    ),
                )

            override fun close() = Unit
        }
        val frame = Frame(640, 480, 0, 7L)

        val result = runScanPublishStep(detector, frame, pack)

        assertEquals(1, result.rawDetections.size)
        assertEquals(1, result.overlayBoxes.size)
        assertEquals("Scripted grape leaf", result.overlayBoxes[0].className)
        assertEquals(0.85f, result.overlayBoxes[0].detection.score, 0.0001f)
    }

    @Test
    fun servicePublishStepHidesBoxBelowThreshold() {
        val detector = object : Detector {
            override val pack: ModelPack = this@ScanPublishStepTest.pack

            override fun detect(frame: Frame): List<Detection> =
                listOf(
                    Detection(
                        classId = 0,
                        score = 0.1f,
                        box = NormalizedBox(0.1f, 0.1f, 0.4f, 0.4f),
                        frameTimestampNs = frame.timestampNs,
                    ),
                )

            override fun close() = Unit
        }

        val result = runScanPublishStep(detector, Frame(640, 480, 0, 1L), pack)

        assertEquals(1, result.rawDetections.size)
        assertTrue(result.overlayBoxes.isEmpty())
    }
}
