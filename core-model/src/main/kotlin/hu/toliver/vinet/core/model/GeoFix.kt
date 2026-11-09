package hu.toliver.vinet.core.model

data class GeoFix(
    val lat: Double,
    val lon: Double,
    val accuracyM: Float,
    val speedMps: Float,
    val bearingDeg: Float,
    /** Same elapsed-realtime nanosecond timebase as [Detection.frameTimestampNs]. */
    val elapsedRealtimeNs: Long,
    /** UTC epoch milliseconds for wall-clock display and export. */
    val timeUtcMs: Long,
)
