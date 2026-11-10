package hu.toliver.vinet.inference

import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackClass
import hu.toliver.vinet.core.pack.PackInputFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeDetectorTest {
    private val pack = ModelPack(
        packId = "fake-detector-test",
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
            PackClass(0, "Scripted leaf class", "#4B0082", 0.42f),
        ),
        minTrackFrames = 3,
        notes = "",
    )

    @Test
    fun returnsExactlyOneBoxPerFrame() {
        val detector = FakeDetector(pack)
        val frame = Frame(640, 480, 0, 99L)

        val detections = detector.detect(frame)

        assertEquals(1, detections.size)
        assertEquals(0, detections[0].classId)
        assertEquals(0.85f, detections[0].score, 0.0001f)
        assertEquals(99L, detections[0].frameTimestampNs)
    }
}
