package hu.toliver.vinet.scan

import hu.toliver.vinet.devtools.DebugHudFormatter
import hu.toliver.vinet.devtools.DebugHudSampler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugScanHudRecorder @Inject constructor(
    private val sampler: DebugHudSampler,
) : ScanHudRecorder {
    override fun recordFrame(frameElapsedRealtimeNs: Long) {
        sampler.recordFrame(frameElapsedRealtimeNs)
    }

    override fun recordInference(latencyMs: Float) {
        sampler.recordInference(latencyMs)
    }

    override fun hudLines(): List<String> = DebugHudFormatter.lines(sampler.snapshot())
}
