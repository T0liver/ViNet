package hu.toliver.vinet.camera

import hu.toliver.vinet.core.model.Frame

interface ReplayFrameProvider {
    fun loadFrames(): List<Frame>
}

/** Deterministic frames for JVM tests and bench replay without a decoder. */
class ListedReplayFrameProvider(
    private val frames: List<Frame>,
) : ReplayFrameProvider {
    override fun loadFrames(): List<Frame> = frames
}
