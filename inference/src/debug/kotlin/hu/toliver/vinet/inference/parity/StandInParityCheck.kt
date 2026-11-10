package hu.toliver.vinet.inference.parity

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.inference.litert.StandInStillScan
import hu.toliver.vinet.inference.preprocess.ManifestPreprocess
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/**
 * Compares one stand-in run to a reference run.
 *
 * The tensor digest is the preprocess from the LiteRT story ([ManifestPreprocess]).
 * A letterbox, color-order, normalization, or rotation change breaks that digest.
 * This type lives in the debug source set and is not in the release build.
 */
object StandInParityCheck {
    data class Sample(
        val name: String,
        val frame: Frame,
        val inputSha256: String,
        val detections: List<Detection>,
    )

    fun check(scan: StandInStillScan, samples: List<Sample>): List<String> =
        compare(scan, samples, ManifestPreprocess::toModelInput)

    internal fun compare(
        scan: StandInStillScan,
        samples: List<Sample>,
        preprocess: (Frame, ModelPack) -> FloatArray,
    ): List<String> {
        val failures = ArrayList<String>()
        for (sample in samples) {
            val input = preprocess(sample.frame, scan.pack)
            if (digestFloats(input) != sample.inputSha256) {
                failures += "${sample.name}: preprocess mismatch"
            }
            if (!sameDetections(scan.detect(sample.frame), sample.detections)) {
                failures += "${sample.name}: detection mismatch"
            }
        }
        return failures
    }

    private fun digestFloats(values: FloatArray): String {
        val buffer = ByteBuffer.allocate(values.size * Float.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
        values.forEach { buffer.putFloat(it) }
        val digest = MessageDigest.getInstance("SHA-256").digest(buffer.array())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun sameDetections(actual: List<Detection>, expected: List<Detection>): Boolean {
        if (actual.size != expected.size) return false
        for (index in actual.indices) {
            val left = actual[index]
            val right = expected[index]
            if (left.classId != right.classId) return false
            if (left.frameTimestampNs != right.frameTimestampNs) return false
            if (!close(left.score, right.score)) return false
            if (!close(left.box.left, right.box.left)) return false
            if (!close(left.box.top, right.box.top)) return false
            if (!close(left.box.right, right.box.right)) return false
            if (!close(left.box.bottom, right.box.bottom)) return false
        }
        return true
    }

    private fun close(left: Float, right: Float): Boolean = kotlin.math.abs(left - right) <= 1e-4f
}
