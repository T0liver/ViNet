package hu.toliver.vinet.camera

import hu.toliver.vinet.core.model.Frame

/**
 * One sensor-rotation step. The published [Frame.rotationDegrees] is 0 so downstream does not rotate again.
 */
fun toUprightFrame(buffer: SensorFrameBuffer): Frame {
    val upright = applySensorRotationOnce(
        width = buffer.width,
        height = buffer.height,
        rotationDegrees = buffer.rotationDegrees,
        rgb = buffer.rgb,
    )
    return Frame(
        width = upright.width,
        height = upright.height,
        rotationDegrees = 0,
        timestampNs = buffer.timestampNs,
        pixels = upright.rgb,
    )
}

data class UprightRgb(
    val width: Int,
    val height: Int,
    val rgb: ByteArray,
)

fun applySensorRotationOnce(
    width: Int,
    height: Int,
    rotationDegrees: Int,
    rgb: ByteArray,
): UprightRgb {
    require(width > 0 && height > 0)
    require(rgb.size == width * height * 3) {
        "expected ${width * height * 3} RGB bytes, got ${rgb.size}"
    }
    return when (rotationDegrees) {
        0 -> UprightRgb(width, height, rgb.copyOf())
        90 -> rotate(width, height, rgb, destWidth = height, destHeight = width) { x, y, _, srcHeight ->
            val destX = srcHeight - 1 - y
            val destY = x
            destX to destY
        }
        180 -> rotate(width, height, rgb, destWidth = width, destHeight = height) { x, y, srcWidth, srcHeight ->
            val destX = srcWidth - 1 - x
            val destY = srcHeight - 1 - y
            destX to destY
        }
        270 -> rotate(width, height, rgb, destWidth = height, destHeight = width) { x, y, srcWidth, _ ->
            val destX = y
            val destY = srcWidth - 1 - x
            destX to destY
        }
        else -> throw IllegalArgumentException("sensor rotation must be 0, 90, 180, or 270")
    }
}

private fun rotate(
    width: Int,
    height: Int,
    rgb: ByteArray,
    destWidth: Int,
    destHeight: Int,
    destOf: (x: Int, y: Int, srcWidth: Int, srcHeight: Int) -> Pair<Int, Int>,
): UprightRgb {
    val out = ByteArray(destWidth * destHeight * 3)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val (destX, destY) = destOf(x, y, width, height)
            val src = (y * width + x) * 3
            val dst = (destY * destWidth + destX) * 3
            out[dst] = rgb[src]
            out[dst + 1] = rgb[src + 1]
            out[dst + 2] = rgb[src + 2]
        }
    }
    return UprightRgb(destWidth, destHeight, out)
}
