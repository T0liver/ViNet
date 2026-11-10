package hu.toliver.vinet.camera

import android.util.Log
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import hu.toliver.vinet.core.config.ConfigSnapshot
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.port.FrameSource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runInterruptible

/**
 * Live [FrameSource]. Analysis keeps only the latest frame (AD-4). Each frame is upright (AD-3).
 * The collector runs before [SensorFrameBuffer.release], so the service can encode first (AD-5).
 */
class CameraFrameSource(
    private val session: CameraSession = IdleCameraSession,
) : FrameSource {
    private val queue = SynchronousQueue<PendingFrame>(true)
    private val latest = LatestFramePolicy<SensorFrameBuffer>()

    override val frames: Flow<Frame> = flow {
        while (currentCoroutineContext().isActive) {
            val pending = runInterruptible(Dispatchers.IO) {
                queue.poll(HANDOFF_WAIT_MS, TimeUnit.MILLISECONDS)
            } ?: continue
            try {
                emit(pending.frame)
            } finally {
                pending.done.countDown()
            }
        }
    }

    fun start(owner: LifecycleOwner, snapshot: ConfigSnapshot) {
        session.open(owner, snapshot, ::deliver)
    }

    fun stop() {
        session.close()
    }

    fun setSurfaceProvider(provider: Preview.SurfaceProvider?, rotation: Int) {
        session.setSurfaceProvider(provider, rotation)
    }

    /**
     * Hands [buffer] to [frames] and releases it after the collector returns.
     * A buffer that is still waiting is dropped so only the latest is analyzed.
     */
    fun deliver(buffer: SensorFrameBuffer) {
        val dropped = latest.offer(buffer)
        if (dropped != null && dropped !== buffer) {
            dropped.release()
        }
        val current = latest.take() ?: return
        try {
            val frame = toUprightFrame(current)
            val done = CountDownLatch(1)
            val accepted = queue.offer(PendingFrame(frame, done), HANDOFF_WAIT_MS, TimeUnit.MILLISECONDS)
            if (accepted) {
                done.await(HANDOFF_WAIT_MS, TimeUnit.MILLISECONDS)
            } else {
                Log.w(TAG, "Dropped a camera frame because the pipeline was not collecting")
            }
        } finally {
            current.release()
        }
    }

    private class PendingFrame(
        val frame: Frame,
        val done: CountDownLatch,
    )

    private companion object {
        const val TAG = "CameraFrameSource"
        const val HANDOFF_WAIT_MS = 10_000L
    }
}
