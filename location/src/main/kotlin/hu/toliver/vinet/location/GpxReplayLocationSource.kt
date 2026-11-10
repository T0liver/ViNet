package hu.toliver.vinet.location

import android.os.SystemClock
import hu.toliver.vinet.core.model.GeoFix
import hu.toliver.vinet.core.port.LocationSource
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Replays fixes from a GPX file. Emits raw trkpt positions; it does not compute a canopy point.
 */
class GpxReplayLocationSource(
    private val gpxPath: String,
    private val clockNs: () -> Long = { SystemClock.elapsedRealtimeNanos() },
    private val readText: (String) -> String = { path -> File(path).readText() },
) : LocationSource {
    private val running = AtomicBoolean(false)
    private val mutableFixes = MutableSharedFlow<GeoFix>(extraBufferCapacity = 64)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null

    override val fixes: Flow<GeoFix> = mutableFixes.asSharedFlow()

    override fun start() {
        if (!running.compareAndSet(false, true)) return
        val points = GpxParser.parseTrackPoints(readText(gpxPath))
        val mapped = mapGpxToElapsedRealtime(points, sessionStartElapsedRealtimeNs = clockNs())
        playbackJob = scope.launch {
            for ((targetElapsedNs, point) in mapped) {
                if (!isActive || !running.get()) return@launch
                mutableFixes.emit(
                    GeoFix(
                        lat = point.lat,
                        lon = point.lon,
                        accuracyM = 3f,
                        speedMps = 1.2f,
                        bearingDeg = 90f,
                        elapsedRealtimeNs = targetElapsedNs,
                        timeUtcMs = point.timeUtcMs,
                    ),
                )
            }
        }
    }

    override fun stop() {
        if (!running.compareAndSet(true, false)) return
        playbackJob?.cancel()
        playbackJob = null
    }

    fun close() {
        stop()
        scope.cancel()
    }
}
