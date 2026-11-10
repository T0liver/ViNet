package hu.toliver.vinet.scan

interface ScanHudRecorder {
    fun recordFrame(frameElapsedRealtimeNs: Long)

    fun recordInference(latencyMs: Float)

    fun hudLines(): List<String>
}

object NoScanHudRecorder : ScanHudRecorder {
    override fun recordFrame(frameElapsedRealtimeNs: Long) = Unit

    override fun recordInference(latencyMs: Float) = Unit

    override fun hudLines(): List<String> = emptyList()
}
