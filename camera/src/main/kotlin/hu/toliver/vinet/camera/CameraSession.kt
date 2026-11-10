package hu.toliver.vinet.camera

import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import hu.toliver.vinet.core.config.ConfigSnapshot
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface CameraSession {
    fun open(owner: LifecycleOwner, snapshot: ConfigSnapshot, onBuffer: (SensorFrameBuffer) -> Unit)
    fun setSurfaceProvider(provider: Preview.SurfaceProvider?, rotation: Int)
    fun close()
}

object IdleCameraSession : CameraSession {
    override fun open(owner: LifecycleOwner, snapshot: ConfigSnapshot, onBuffer: (SensorFrameBuffer) -> Unit) = Unit
    override fun setSurfaceProvider(provider: Preview.SurfaceProvider?, rotation: Int) = Unit
    override fun close() = Unit
}

/**
 * CameraX analysis plus preview. Rotation is applied once in [toUprightFrame], not here.
 */
class CameraXSession(
    private val context: Context,
) : CameraSession {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val mainExecutor = Executor { command ->
        Handler(Looper.getMainLooper()).post(command)
    }
    private var provider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var analysis: ImageAnalysis? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null
    private var targetRotation: Int = Surface.ROTATION_0

    override fun setSurfaceProvider(provider: Preview.SurfaceProvider?, rotation: Int) {
        surfaceProvider = provider
        targetRotation = rotation
        preview?.targetRotation = rotation
        analysis?.targetRotation = rotation
        preview?.setSurfaceProvider(provider)
    }

    override fun open(owner: LifecycleOwner, snapshot: ConfigSnapshot, onBuffer: (SensorFrameBuffer) -> Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                bind(future.get(), owner, snapshot, onBuffer)
            } catch (error: Exception) {
                Log.e(TAG, "Camera session did not start", error)
            }
        }, mainExecutor)
    }

    override fun close() {
        mainExecutor.execute {
            provider?.unbindAll()
        }
    }

    private fun bind(
        cameraProvider: ProcessCameraProvider,
        owner: LifecycleOwner,
        snapshot: ConfigSnapshot,
        onBuffer: (SensorFrameBuffer) -> Unit,
    ) {
        provider = cameraProvider
        val analysisBuilder = ImageAnalysis.Builder()
            .setTargetRotation(targetRotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        val previewBuilder = Preview.Builder()
            .setTargetRotation(targetRotation)
        applyCameraControls(snapshot, CameraXCaptureControl(analysisBuilder, previewBuilder))
        val analysisUseCase = analysisBuilder.build()
        val previewUseCase = previewBuilder.build()
        analysis = analysisUseCase
        preview = previewUseCase
        surfaceProvider?.let { previewUseCase.setSurfaceProvider(it) }
        analysisUseCase.setAnalyzer(cameraExecutor) { image ->
            val buffer = image.toSensorBuffer()
            try {
                onBuffer(buffer)
            } finally {
                if (!buffer.released) buffer.release()
            }
        }
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            owner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            previewUseCase,
            analysisUseCase,
        )
    }

    private companion object {
        const val TAG = "CameraXSession"
    }
}

private class CameraXCaptureControl(
    private val analysisBuilder: ImageAnalysis.Builder,
    private val previewBuilder: Preview.Builder,
) : CaptureControl {
    private val analysis = Camera2Interop.Extender(analysisBuilder)
    private val preview = Camera2Interop.Extender(previewBuilder)

    override fun backpressure(strategy: Int) {
        analysisBuilder.setBackpressureStrategy(strategy)
    }

    override fun videoStabilization(mode: Int) {
        analysis.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, mode)
        preview.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, mode)
    }

    override fun opticalStabilization(mode: Int) {
        analysis.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, mode)
        preview.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, mode)
    }

    override fun manualExposure(timeNs: Long, aeModeOff: Int) {
        analysis.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, timeNs)
        analysis.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, aeModeOff)
        preview.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, timeNs)
        preview.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, aeModeOff)
    }

    override fun manualFocus(diopters: Float, afModeOff: Int) {
        analysis.setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, diopters)
        analysis.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, afModeOff)
        preview.setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, diopters)
        preview.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, afModeOff)
    }
}

private fun androidx.camera.core.ImageProxy.toSensorBuffer(): SensorFrameBuffer {
    val plane = planes[0]
    val buffer = plane.buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val rgb = rgbaToRgb(
        rgba = bytes,
        width = width,
        height = height,
        rowStride = plane.rowStride,
        pixelStride = plane.pixelStride,
    )
    val image = this
    return SensorFrameBuffer(
        width = width,
        height = height,
        rotationDegrees = imageInfo.rotationDegrees,
        rgb = rgb,
        timestampNs = imageInfo.timestamp,
        onRelease = { image.close() },
    )
}
