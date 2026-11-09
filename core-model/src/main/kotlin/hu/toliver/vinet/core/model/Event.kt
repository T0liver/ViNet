package hu.toliver.vinet.core.model

enum class UserFeedback {
    NONE,
    CORRECT,
    WRONG,
    UNSURE,
}

data class Event(
    val id: String,
    val sessionId: String,
    val timestampUtcMs: Long,
    val lat: Double?,
    val lon: Double?,
    val accuracyM: Float?,
    val speedMps: Float?,
    val bearingDeg: Float?,
    val classId: Int,
    val className: String,
    val confidence: Float,
    val trackId: String,
    val box: NormalizedBox,
    val imagePath: String?,
    val thumbPath: String?,
    val plotName: String?,
    val userFeedback: UserFeedback,
    val packId: String,
    val modelVersion: String,
)
