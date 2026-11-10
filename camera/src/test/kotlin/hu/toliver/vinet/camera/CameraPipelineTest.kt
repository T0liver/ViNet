package hu.toliver.vinet.camera

import android.hardware.camera2.CaptureRequest
import androidx.camera.core.ImageAnalysis
import hu.toliver.vinet.core.config.ConfigSnapshot
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraPipelineTest {
    @Test
    fun keepsOnlyTheLatestFrame() {
        val policy = LatestFramePolicy<String>()
        assertNull(policy.offer("old"))
        assertEquals("old", policy.offer("newer"))
        assertEquals("newer", policy.offer("latest"))
        assertEquals("latest", policy.take())
        assertNull(policy.take())
        val snapshot = ConfigSnapshot.uninitialized()
        assertEquals(
            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
            analysisConfig(snapshot).backpressureStrategy,
        )
        assertNotEquals(
            ImageAnalysis.STRATEGY_BLOCK_PRODUCER,
            analysisConfig(snapshot).backpressureStrategy,
        )
    }

    @Test
    fun stabilizationThatCropsOrWarpsIsOff() {
        val applied = RecordingControl()
        applyCameraControls(ConfigSnapshot.uninitialized().copy(exposureCapNs = 1L, focusDistance = 1f), applied)
        assertEquals(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF, applied.videoStabilization)
        assertEquals(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF, applied.opticalStabilization)
        assertNotEquals(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON, applied.videoStabilization)
        assertEquals(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST, applied.backpressure)
    }

    @Test
    fun readsExposureCapAndFocusDistanceFromTheSnapshot() {
        val snapshot = ConfigSnapshot.uninitialized().copy(exposureCapNs = 4_000_000L, focusDistance = 1.75f)
        val applied = RecordingControl()
        applyCameraControls(snapshot, applied)
        assertEquals(4_000_000L, applied.exposureCapNs)
        assertEquals(1.75f, applied.focusDistance, 0.0001f)
        assertEquals(CaptureRequest.CONTROL_AE_MODE_OFF, applied.aeMode)
        assertEquals(CaptureRequest.CONTROL_AF_MODE_OFF, applied.afMode)
    }

    @Test
    fun sensorRotationIsAppliedOnce() {
        val rgb = byteArrayOf(
            1, 0, 0, 2, 0, 0,
            3, 0, 0, 4, 0, 0,
        )
        val once = applySensorRotationOnce(2, 2, 90, rgb)
        assertEquals(2, once.width)
        assertEquals(2, once.height)
        assertArrayEquals(
            byteArrayOf(
                3, 0, 0, 1, 0, 0,
                4, 0, 0, 2, 0, 0,
            ),
            once.rgb,
        )
        val uprightAgain = applySensorRotationOnce(once.width, once.height, 0, once.rgb)
        assertArrayEquals(once.rgb, uprightAgain.rgb)
        val rotatedTwice = applySensorRotationOnce(once.width, once.height, 90, once.rgb)
        assertFalse(rotatedTwice.rgb.contentEquals(once.rgb))

        val frame = toUprightFrame(SensorFrameBuffer(2, 2, 90, rgb, 11L))
        assertEquals(0, frame.rotationDegrees)
        assertEquals(2, frame.width)
        assertArrayEquals(once.rgb, frame.pixels)
    }

    @Test(timeout = 20_000)
    fun collectorFinishesBeforeTheBufferIsReleased() {
        val source = CameraFrameSource()
        val seen = AtomicBoolean(false)
        val failure = java.util.concurrent.atomic.AtomicReference<Throwable>()
        val scope = CoroutineScope(Dispatchers.Default)
        val buffer = SensorFrameBuffer(
            width = 2,
            height = 2,
            rotationDegrees = 0,
            rgb = ByteArray(12),
            timestampNs = 3L,
        )
        val job = scope.launch {
            source.frames.collect { frame ->
                try {
                    assertFalse(buffer.released)
                    assertEquals(0, frame.rotationDegrees)
                    seen.set(true)
                } catch (error: Throwable) {
                    failure.set(error)
                }
            }
        }
        try {
            source.deliver(buffer)
            failure.get()?.let { throw it }
            assertTrue(seen.get())
            assertTrue(buffer.released)
        } finally {
            job.cancel()
            scope.cancel()
        }
    }

    @Test
    fun rgbaDropsAlpha() {
        val rgba = byteArrayOf(9, 8, 7, 255.toByte(), 1, 2, 3, 255.toByte())
        val rgb = rgbaToRgb(rgba, width = 2, height = 1, rowStride = 8, pixelStride = 4)
        assertArrayEquals(byteArrayOf(9, 8, 7, 1, 2, 3), rgb)
    }
}

private class RecordingControl : CaptureControl {
    var backpressure: Int = -1
    var videoStabilization: Int = -1
    var opticalStabilization: Int = -1
    var exposureCapNs: Long = -1
    var focusDistance: Float = -1f
    var aeMode: Int = -1
    var afMode: Int = -1

    override fun backpressure(strategy: Int) {
        backpressure = strategy
    }

    override fun videoStabilization(mode: Int) {
        videoStabilization = mode
    }

    override fun opticalStabilization(mode: Int) {
        opticalStabilization = mode
    }

    override fun manualExposure(timeNs: Long, aeModeOff: Int) {
        exposureCapNs = timeNs
        aeMode = aeModeOff
    }

    override fun manualFocus(diopters: Float, afModeOff: Int) {
        focusDistance = diopters
        afMode = afModeOff
    }
}
