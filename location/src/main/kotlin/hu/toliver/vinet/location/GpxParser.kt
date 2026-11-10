package hu.toliver.vinet.location

import java.time.Instant

data class GpxTrackPoint(
    val lat: Double,
    val lon: Double,
    val timeUtcMs: Long,
)

/**
 * Minimal GPX trkpt parser for replay (JVM-testable, no Android APIs).
 */
object GpxParser {
    private val trkptPattern =
        Regex(
            """<trkpt\s+lat="([^"]+)"\s+lon="([^"]+)"[^>]*>[\s\S]*?<time>([^<]+)</time>""",
            RegexOption.IGNORE_CASE,
        )

    fun parseTrackPoints(gpxXml: String): List<GpxTrackPoint> =
        trkptPattern.findAll(gpxXml).map { match ->
            val lat = match.groupValues[1].toDouble()
            val lon = match.groupValues[2].toDouble()
            val utcMs = Instant.parse(match.groupValues[3].trim()).toEpochMilli()
            GpxTrackPoint(lat = lat, lon = lon, timeUtcMs = utcMs)
        }.toList()
}
