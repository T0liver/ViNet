package hu.toliver.vinet.core.model

data class Track(
    val trackId: String,
    val classId: Int,
    val lastBox: NormalizedBox?,
    val hitFrameCount: Int,
    val lastDetectionTimestampNs: Long,
)
