package hu.toliver.vinet.camera

/** Copies packed RGBA (alpha ignored) into RGB bytes, three per pixel. */
fun rgbaToRgb(
    rgba: ByteArray,
    width: Int,
    height: Int,
    rowStride: Int,
    pixelStride: Int,
): ByteArray {
    require(width > 0 && height > 0)
    require(pixelStride >= 3)
    require(rowStride >= width * pixelStride)
    val rgb = ByteArray(width * height * 3)
    for (y in 0 until height) {
        val row = y * rowStride
        for (x in 0 until width) {
            val src = row + x * pixelStride
            val dst = (y * width + x) * 3
            rgb[dst] = rgba[src]
            rgb[dst + 1] = rgba[src + 1]
            rgb[dst + 2] = rgba[src + 2]
        }
    }
    return rgb
}
