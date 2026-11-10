package hu.toliver.vinet.camera

/**
 * Sensor-oriented RGB frame. [release] returns the camera buffer; call it only after encode.
 */
class SensorFrameBuffer(
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val rgb: ByteArray,
    val timestampNs: Long,
    private val onRelease: () -> Unit = {},
) {
    var released: Boolean = false
        private set

    fun release() {
        if (released) return
        released = true
        onRelease()
    }
}
