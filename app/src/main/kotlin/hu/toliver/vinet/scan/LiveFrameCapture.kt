package hu.toliver.vinet.scan

import hu.toliver.vinet.core.model.Event
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.model.NormalizedBox
import hu.toliver.vinet.core.overlay.OverlayBox
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import javax.inject.Inject
import javax.inject.Singleton

fun interface EventWriter {
    fun write(event: Event)
}

/** Live capture keeps JPEG bytes in memory and does not write an event. */
object NoEventWriter : EventWriter {
    override fun write(event: Event) {
        error("live capture must not write an event")
    }
}

interface CaptureByteSink {
    fun hold(fullJpeg: ByteArray, cropThumbnail: ByteArray)
}

@Singleton
class LiveCaptureMemory @Inject constructor() : CaptureByteSink {
    @Volatile var fullJpeg: ByteArray? = null
        private set

    @Volatile var cropThumbnail: ByteArray? = null
        private set

    override fun hold(fullJpeg: ByteArray, cropThumbnail: ByteArray) {
        this.fullJpeg = fullJpeg.copyOf()
        this.cropThumbnail = cropThumbnail.copyOf()
    }
}

/**
 * Encodes the upright frame before the caller releases the camera buffer.
 * [events] is not called.
 */
@Suppress("UNUSED_PARAMETER")
fun encodeBeforeRelease(
    frame: Frame,
    crop: NormalizedBox,
    sink: CaptureByteSink,
    events: EventWriter,
    release: () -> Unit = {},
) {
    val encoded = encodeRgbFrame(frame, crop)
    sink.hold(encoded.fullJpeg, encoded.cropThumbnail)
    release()
}

fun onLiveFrame(
    frame: Frame,
    detector: Detector,
    pack: ModelPack,
    sink: CaptureByteSink,
    events: EventWriter,
    publishOverlay: (List<OverlayBox>) -> Unit,
) {
    val published = runScanPublishStep(detector, frame, pack)
    if (frame.pixels != null) {
        val crop = published.rawDetections.firstOrNull()?.box
            ?: NormalizedBox(0.25f, 0.25f, 0.75f, 0.75f)
        encodeBeforeRelease(frame, crop, sink, events)
    }
    publishOverlay(published.overlayBoxes)
}
