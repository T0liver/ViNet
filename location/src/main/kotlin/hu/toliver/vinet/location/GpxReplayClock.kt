package hu.toliver.vinet.location

/**
 * Maps GPX wall times to elapsed-realtime nanoseconds for replay (AD-13).
 * No canopy offset is applied here.
 */
fun mapGpxToElapsedRealtime(
    points: List<GpxTrackPoint>,
    sessionStartElapsedRealtimeNs: Long,
): List<Pair<Long, GpxTrackPoint>> {
    if (points.isEmpty()) return emptyList()
    val baseUtcMs = points.first().timeUtcMs
    return points.map { point ->
        val deltaMs = point.timeUtcMs - baseUtcMs
        val elapsedNs = sessionStartElapsedRealtimeNs + deltaMs * 1_000_000L
        elapsedNs to point
    }
}
