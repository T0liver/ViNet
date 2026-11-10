package hu.toliver.vinet.devtools

/**
 * Debug HUD figures shown during capture (FPS, inference latency, temperature).
 * Open-track count waits for Events and place.
 */
data class DebugHudSnapshot(
    val fps: Float,
    val inferenceLatencyMs: Float,
    val temperatureCelsius: Float,
)

object DebugHudFormatter {
    fun lines(snapshot: DebugHudSnapshot): List<String> =
        listOf(
            "FPS: ${formatOneDecimal(snapshot.fps)}",
            "Inference latency: ${formatOneDecimal(snapshot.inferenceLatencyMs)} ms",
            "Temperature: ${formatZeroDecimal(snapshot.temperatureCelsius)} °C",
        )

    private fun formatOneDecimal(value: Float): String {
        val scaled = (value * 10f).toInt()
        val whole = scaled / 10
        val frac = kotlin.math.abs(scaled % 10)
        return "$whole.$frac"
    }

    private fun formatZeroDecimal(value: Float): String = value.toInt().toString()
}

class DebugHudSampler(
    private val temperatureCelsius: () -> Float,
    private val windowFrames: Int = 30,
) {
    private val frameTimesNs = ArrayDeque<Long>()
    private var lastLatencyMs: Float = 0f

    fun recordFrame(frameElapsedRealtimeNs: Long) {
        frameTimesNs.addLast(frameElapsedRealtimeNs)
        while (frameTimesNs.size > windowFrames) {
            frameTimesNs.removeFirst()
        }
    }

    fun recordInference(latencyMs: Float) {
        lastLatencyMs = latencyMs
    }

    fun snapshot(): DebugHudSnapshot {
        val fps =
            if (frameTimesNs.size < 2) {
                0f
            } else {
                val spanNs = frameTimesNs.last() - frameTimesNs.first()
                if (spanNs <= 0) 0f else (frameTimesNs.size - 1) * 1_000_000_000f / spanNs
            }
        return DebugHudSnapshot(
            fps = fps,
            inferenceLatencyMs = lastLatencyMs,
            temperatureCelsius = temperatureCelsius(),
        )
    }
}
