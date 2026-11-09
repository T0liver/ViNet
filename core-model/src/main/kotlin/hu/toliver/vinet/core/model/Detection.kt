package hu.toliver.vinet.core.model

data class Detection(
    val classId: Int,
    val score: Float,
    val box: NormalizedBox,
    /** Elapsed-realtime nanoseconds on the camera sensor timebase. */
    val frameTimestampNs: Long,
)
