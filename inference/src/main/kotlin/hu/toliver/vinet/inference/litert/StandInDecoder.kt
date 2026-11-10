package hu.toliver.vinet.inference.litert

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.model.NormalizedBox
import hu.toliver.vinet.core.pack.ModelPack

/**
 * Decoder for `outputFormat` `standin-score-box`.
 *
 * The stand-in graph writes rows of `[score, classId, left, top, right, bottom]`.
 * Score is the mean of the preprocessed frame when that mean is above 0.5, and 0 otherwise.
 * A non-positive score is a gap: this returns no detection for that row (AD-4).
 */
object StandInDecoder {
    const val OUTPUT_FORMAT = "standin-score-box"
    const val STRIDE = 6

    fun decode(output: FloatArray, frame: Frame, pack: ModelPack): List<Detection> {
        require(pack.outputFormat == OUTPUT_FORMAT) {
            "unsupported outputFormat ${pack.outputFormat}"
        }
        require(output.size % STRIDE == 0) { "stand-in output length ${output.size}" }
        val detections = ArrayList<Detection>(output.size / STRIDE)
        var index = 0
        while (index < output.size) {
            val score = output[index]
            if (score > 0f) {
                detections += Detection(
                    classId = output[index + 1].toInt(),
                    score = score,
                    box = NormalizedBox(
                        left = output[index + 2],
                        top = output[index + 3],
                        right = output[index + 4],
                        bottom = output[index + 5],
                    ),
                    frameTimestampNs = frame.timestampNs,
                )
            }
            index += STRIDE
        }
        return detections
    }
}
