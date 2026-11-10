package hu.toliver.vinet.inference

import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackManifestParser
import hu.toliver.vinet.inference.litert.JvmLiteRtSession
import hu.toliver.vinet.inference.litert.StandInStillScan
import hu.toliver.vinet.inference.parity.StandInParityCheck
import hu.toliver.vinet.inference.parity.StandInParitySet
import hu.toliver.vinet.inference.preprocess.ManifestPreprocess
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StandInParityTest {
    private lateinit var pack: ModelPack
    private lateinit var model: ByteArray
    private lateinit var scan: StandInStillScan
    private lateinit var samples: List<StandInParityCheck.Sample>

    @Before
    fun loadStandIn() {
        val dir = standInDir()
        pack = PackManifestParser.parse(dir.resolve("manifest.json").readText())
        model = dir.resolve(pack.modelFileName).readBytes()
        scan = StandInStillScan(pack, JvmLiteRtSession(model))
        samples = StandInParitySet.samples(referenceText())
    }

    @After
    fun closeScan() {
        scan.close()
    }

    @Test
    fun standInSetMatchesReference() {
        assertEquals(emptyList<String>(), StandInParityCheck.check(scan, samples))
    }

    @Test
    fun colorOrderSwapFailsTheCheck() {
        val swapped = pack.copy(inputFormat = pack.inputFormat.copy(colorOrder = "BGR"))
        StandInStillScan(swapped, JvmLiteRtSession(model)).use { swappedScan ->
            val failures = StandInParityCheck.check(swappedScan, samples)
            assertTrue(failures.contains("red-square: preprocess mismatch"))
            assertTrue(failures.contains("yellow-wide: preprocess mismatch"))
            assertTrue(failures.contains("gradient: preprocess mismatch"))
            assertFalse(failures.contains("solid-white: preprocess mismatch"))
        }
    }

    @Test
    fun letterboxMismatchFailsTheCheck() {
        val stretched = pack.copy(inputFormat = pack.inputFormat.copy(letterbox = false))
        StandInStillScan(stretched, JvmLiteRtSession(model)).use { stretchedScan ->
            val failures = StandInParityCheck.check(stretchedScan, samples)
            assertTrue(failures.any { it.startsWith("wide-bright:") })
            assertTrue(failures.any { it.startsWith("yellow-wide:") })
            assertTrue(failures.any { it.startsWith("gradient:") })
        }
    }

    @Test
    fun rotationMismatchFailsTheCheck() {
        val rotated = samples.map { sample ->
            if (sample.name == "gradient") {
                sample.copy(frame = rotateClockwise(sample.frame))
            } else {
                sample
            }
        }
        val failures = StandInParityCheck.check(scan, rotated)
        assertTrue(failures.contains("gradient: preprocess mismatch"))
    }

    @Test
    fun normalizationMismatchFailsTheCheck() {
        val failures = StandInParityCheck.compare(scan, samples) { frame, modelPack ->
            val normalized = ManifestPreprocess.toModelInput(frame, modelPack)
            FloatArray(normalized.size) { index -> normalized[index] * 255f }
        }
        assertTrue(failures.contains("solid-white: preprocess mismatch"))
        assertTrue(failures.contains("red-square: preprocess mismatch"))
        assertTrue(failures.contains("gradient: preprocess mismatch"))
    }

    private fun rotateClockwise(frame: Frame): Frame {
        val width = frame.width
        val height = frame.height
        val pixels = requireNotNull(frame.pixels)
        val rotated = ByteArray(pixels.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val destinationX = height - 1 - y
                val destinationY = x
                val source = (y * width + x) * 3
                val destination = (destinationY * height + destinationX) * 3
                rotated[destination] = pixels[source]
                rotated[destination + 1] = pixels[source + 1]
                rotated[destination + 2] = pixels[source + 2]
            }
        }
        return frame.copy(width = height, height = width, pixels = rotated)
    }

    private fun referenceText(): String {
        val candidates = listOf(
            File("src/debug/assets/stand-in-parity/reference.txt"),
            File("inference/src/debug/assets/stand-in-parity/reference.txt"),
        )
        val file = candidates.firstOrNull { it.isFile }
            ?: error("stand-in parity reference is missing")
        return file.readText()
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
