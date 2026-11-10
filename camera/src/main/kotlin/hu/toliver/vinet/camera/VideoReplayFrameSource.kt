package hu.toliver.vinet.camera

import android.os.SystemClock
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.port.FrameSource
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * [FrameSource] that replays frames from a video file. On device, [defaultProviderFactory]
 * decodes the file; tests inject [ReplayFrameProvider] directly.
 */
class VideoReplayFrameSource(
    val videoPath: String,
    private val frameProvider: ReplayFrameProvider,
    private val frameIntervalMs: Long = 100L,
) : FrameSource {
    constructor(
        videoPath: String,
        providerFactory: (String) -> ReplayFrameProvider = defaultProviderFactory,
        frameIntervalMs: Long = 100L,
    ) : this(videoPath, providerFactory(videoPath), frameIntervalMs)

    private val running = AtomicBoolean(false)
    private val mutableFrames = MutableSharedFlow<Frame>(extraBufferCapacity = 4)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null
    private val loadedFrames = frameProvider.loadFrames()

    override val frames: Flow<Frame> = mutableFrames.asSharedFlow()

    fun start() {
        if (!running.compareAndSet(false, true)) return
        if (loadedFrames.isEmpty()) return
        playbackJob = scope.launch {
            var index = 0
            while (isActive && running.get()) {
                val frame = loadedFrames[index % loadedFrames.size]
                mutableFrames.emit(frame.copy(timestampNs = SystemClock.elapsedRealtimeNanos()))
                index++
                delay(frameIntervalMs)
            }
        }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        playbackJob?.cancel()
        playbackJob = null
    }

    fun close() {
        stop()
        scope.cancel()
    }

    companion object {
        val defaultProviderFactory: (String) -> ReplayFrameProvider = { path ->
            AndroidVideoReplayFrames(path)
        }
    }
}
