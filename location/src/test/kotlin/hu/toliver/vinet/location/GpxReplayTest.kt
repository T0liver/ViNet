package hu.toliver.vinet.location

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxReplayTest {
    private val sampleGpx = """
        <?xml version="1.0"?>
        <gpx>
          <trk>
            <trkseg>
              <trkpt lat="47.4979" lon="19.0402">
                <time>2026-06-01T10:00:00Z</time>
              </trkpt>
              <trkpt lat="47.4981" lon="19.0404">
                <time>2026-06-01T10:00:02Z</time>
              </trkpt>
            </trkseg>
          </trk>
        </gpx>
    """.trimIndent()

    @Test
    fun gpxReplayEmitsBothTimestampsWithoutCanopyOffset() {
        val fixes = mutableListOf<hu.toliver.vinet.core.model.GeoFix>()
        val source =
            GpxReplayLocationSource(
                gpxPath = "ignored",
                clockNs = { 1_000L },
                readText = { sampleGpx },
            )
        runBlocking {
            val collect =
                async(start = CoroutineStart.UNDISPATCHED) {
                    source.fixes.take(2).toList()
                }
            source.start()
            fixes.addAll(withTimeout(2_000) { collect.await() })
            source.stop()
        }
        assertEquals(2, fixes.size)
        assertEquals(47.4979, fixes[0].lat, 0.0001)
        assertEquals(19.0402, fixes[0].lon, 0.0001)
        assertEquals(1_000L, fixes[0].elapsedRealtimeNs)
        assertTrue(fixes[0].timeUtcMs > 0)
        assertEquals(2_000_001_000L, fixes[1].elapsedRealtimeNs)
        assertTrue(fixes[1].timeUtcMs > fixes[0].timeUtcMs)
    }

    @Test
    fun gpxParserReadsTrackPoints() {
        val points = GpxParser.parseTrackPoints(sampleGpx)
        assertEquals(2, points.size)
        assertEquals(47.4981, points[1].lat, 0.0001)
    }
}
