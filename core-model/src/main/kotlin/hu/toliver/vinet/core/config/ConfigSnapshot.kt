package hu.toliver.vinet.core.config

/**
 * Immutable settings passed across modules (AD-11). Names are fixed; values are tuned on device later.
 * [padValue] and letterbox live on [hu.toliver.vinet.core.pack.ModelPack] only.
 */
data class ConfigSnapshot(
    val maxTrackAge: Int,
    val minTrackFrames: Int,
    val perClassMinScore: Map<Int, Float>,
    val mergeDistanceM: Float,
    val mergeWindowS: Float,
    val minSpeedMps: Float,
    val segmentLengthM: Float,
    val mountSide: MountSide,
    val mountOffsetM: Float,
    val maxAccuracyM: Float,
    val weakAccuracyM: Float,
    val bestFrameWeights: BestFrameWeights,
    /** Maximum shutter time in nanoseconds. `0` means the cap is not set. */
    val exposureCapNs: Long,
    /** Lens focus distance in diopters (Camera2 `LENS_FOCUS_DISTANCE`). `0` means unset. */
    val focusDistance: Float,
) {
    companion object {
        const val EXPOSURE_CAP_NAME = "exposure-cap"
        const val FOCUS_DISTANCE_NAME = "focus-distance"

        /** Placeholder snapshot for schema and wiring; not field-tuned. */
        fun uninitialized(): ConfigSnapshot =
            ConfigSnapshot(
                maxTrackAge = 0,
                minTrackFrames = 0,
                perClassMinScore = emptyMap(),
                mergeDistanceM = 0f,
                mergeWindowS = 0f,
                minSpeedMps = 0f,
                segmentLengthM = 0f,
                mountSide = MountSide.LEFT,
                mountOffsetM = 0f,
                maxAccuracyM = 0f,
                weakAccuracyM = 0f,
                bestFrameWeights = BestFrameWeights(
                    confidence = 0f,
                    boxArea = 0f,
                    fullyInside = 0f,
                    sharpness = 0f,
                ),
                exposureCapNs = 0L,
                focusDistance = 0f,
            )
    }
}

enum class MountSide {
    LEFT,
    RIGHT,
}

data class BestFrameWeights(
    val confidence: Float,
    val boxArea: Float,
    val fullyInside: Float,
    val sharpness: Float,
)
