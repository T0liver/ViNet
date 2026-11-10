package hu.toliver.vinet.devtools

import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.port.FrameSource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/** Emits upright placeholder frames on a fixed tick for scripted scan (debug). */
class ScriptedFrameSource(
    private val intervalMs: Long = 200L,
    private val width: Int = 640,
    private val height: Int = 480,
) : FrameSource {
    override val frames: Flow<Frame> = flow {
        var tick = 0L
        while (currentCoroutineContext().isActive) {
            emit(
                Frame(
                    width = width,
                    height = height,
                    rotationDegrees = 0,
                    timestampNs = tick,
                    pixels = null,
                ),
            )
            tick += intervalMs * 1_000_000
            delay(intervalMs)
        }
    }
}
