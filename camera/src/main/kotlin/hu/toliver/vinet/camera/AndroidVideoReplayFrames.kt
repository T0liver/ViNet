package hu.toliver.vinet.camera

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import hu.toliver.vinet.core.model.Frame

/**
 * Samples a bounded number of upright frames from a video file on device.
 */
class AndroidVideoReplayFrames(
    private val videoPath: String,
    private val maxFrames: Int = 120,
    private val stepUs: Long = 200_000L,
) : ReplayFrameProvider {
    override fun loadFrames(): List<Frame> {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoPath)
        } catch (_: RuntimeException) {
            return emptyList()
        }
        return try {
            buildList {
                var timeUs = 0L
                repeat(maxFrames) {
                    val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                        ?: return@buildList
                    add(bitmap.toUprightFrame(timeUs))
                    bitmap.recycle()
                    timeUs += stepUs
                }
            }
        } finally {
            retriever.release()
        }
    }

    private fun Bitmap.toUprightFrame(timeUs: Long): Frame {
        val width = width
        val height = height
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        val rgb = ByteArray(width * height * 3)
        for (index in pixels.indices) {
            val color = pixels[index]
            rgb[index * 3] = ((color shr 16) and 0xFF).toByte()
            rgb[index * 3 + 1] = ((color shr 8) and 0xFF).toByte()
            rgb[index * 3 + 2] = (color and 0xFF).toByte()
        }
        return Frame(
            width = width,
            height = height,
            rotationDegrees = 0,
            timestampNs = timeUs * 1_000L,
            pixels = rgb,
        )
    }
}
