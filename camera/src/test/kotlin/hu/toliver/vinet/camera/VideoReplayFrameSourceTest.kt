package hu.toliver.vinet.camera

import hu.toliver.vinet.core.model.Frame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VideoReplayFrameSourceTest {
    @Test(timeout = 5_000)
    fun videoReplayEmitsFramesFromProvider() = runBlocking {
        val provider =
            ListedReplayFrameProvider(
                listOf(
                    Frame(
                        width = 16,
                        height = 12,
                        rotationDegrees = 0,
                        timestampNs = 4L,
                        pixels = ByteArray(16 * 12 * 3),
                    ),
                ),
            )
        val source = VideoReplayFrameSource("field.mp4", provider, frameIntervalMs = 1L)
        val frameDeferred = async { source.frames.first() }
        source.start()
        val frame = withTimeout(1_000) { frameDeferred.await() }
        source.stop()
        assertEquals(16, frame.width)
        assertNotNull(frame.pixels)
    }
}
