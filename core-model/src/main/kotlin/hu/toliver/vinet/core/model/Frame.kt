package hu.toliver.vinet.core.model

/**
 * One upright camera frame. Pixel buffer is owned by the producer until the analyzer returns.
 */
data class Frame(
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    /** Elapsed-realtime nanoseconds (`SystemClock.elapsedRealtimeNanos()` timebase). */
    val timestampNs: Long,
    val pixels: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Frame) return false
        return width == other.width &&
            height == other.height &&
            rotationDegrees == other.rotationDegrees &&
            timestampNs == other.timestampNs &&
            pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + rotationDegrees
        result = 31 * result + timestampNs.hashCode()
        result = 31 * result + (pixels?.contentHashCode() ?: 0)
        return result
    }
}
