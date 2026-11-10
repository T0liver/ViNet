package hu.toliver.vinet.inference.preprocess

import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The one preprocess for live frames, stills, and later parity (AD-3).
 * Reads size, color order, normalization, letterbox, and pad from the pack manifest.
 * Frames are already upright; this does not rotate again.
 */
object ManifestPreprocess {
    fun toModelInput(frame: Frame, pack: ModelPack): FloatArray {
        val pixels = requireNotNull(frame.pixels) { "frame has no pixels" }
        require(frame.width > 0 && frame.height > 0)
        require(pixels.size == frame.width * frame.height * 3) {
            "expected ${frame.width * frame.height * 3} RGB bytes, got ${pixels.size}"
        }
        val format = pack.inputFormat
        require(format.normalize == "0-1") {
            "unsupported normalize ${format.normalize}"
        }
        require(format.colorOrder == "RGB" || format.colorOrder == "BGR") {
            "unsupported colorOrder ${format.colorOrder}"
        }
        require(format.padValue in 0..255) { "padValue out of range" }
        val dstW = pack.inputWidth
        val dstH = pack.inputHeight
        require(dstW > 0 && dstH > 0)
        val out = FloatArray(dstW * dstH * 3)
        val pad = format.padValue / 255f
        val swap = format.colorOrder == "BGR"
        if (!format.letterbox) {
            resizeInto(pixels, frame.width, frame.height, out, dstW, 0, 0, dstW, dstH, swap)
        } else {
            for (i in out.indices) out[i] = pad
            val scale = min(dstW.toFloat() / frame.width, dstH.toFloat() / frame.height)
            val resizedW = (frame.width * scale).roundToInt().coerceIn(1, dstW)
            val resizedH = (frame.height * scale).roundToInt().coerceIn(1, dstH)
            val padX = (dstW - resizedW) / 2
            val padY = (dstH - resizedH) / 2
            resizeInto(
                pixels,
                frame.width,
                frame.height,
                out,
                dstW,
                padX,
                padY,
                resizedW,
                resizedH,
                swap,
            )
        }
        return out
    }

    private fun resizeInto(
        pixels: ByteArray,
        srcW: Int,
        srcH: Int,
        out: FloatArray,
        dstW: Int,
        originX: Int,
        originY: Int,
        resizedW: Int,
        resizedH: Int,
        swapRb: Boolean,
    ) {
        for (y in 0 until resizedH) {
            val srcY = if (resizedH == 1) 0f else (y + 0.5f) * srcH / resizedH - 0.5f
            for (x in 0 until resizedW) {
                val srcX = if (resizedW == 1) 0f else (x + 0.5f) * srcW / resizedW - 0.5f
                val sample = bilinear(pixels, srcW, srcH, srcX, srcY)
                val dst = ((originY + y) * dstW + (originX + x)) * 3
                if (swapRb) {
                    out[dst] = sample[2]
                    out[dst + 1] = sample[1]
                    out[dst + 2] = sample[0]
                } else {
                    out[dst] = sample[0]
                    out[dst + 1] = sample[1]
                    out[dst + 2] = sample[2]
                }
            }
        }
    }

    private fun bilinear(pixels: ByteArray, width: Int, height: Int, srcX: Float, srcY: Float): FloatArray {
        val x0 = kotlin.math.floor(srcX).toInt()
        val y0 = kotlin.math.floor(srcY).toInt()
        val x1 = x0 + 1
        val y1 = y0 + 1
        val wx = srcX - x0
        val wy = srcY - y0
        val rgb = FloatArray(3)
        for (channel in 0 until 3) {
            val p00 = texel(pixels, width, height, x0, y0, channel)
            val p10 = texel(pixels, width, height, x1, y0, channel)
            val p01 = texel(pixels, width, height, x0, y1, channel)
            val p11 = texel(pixels, width, height, x1, y1, channel)
            val top = p00 + (p10 - p00) * wx
            val bottom = p01 + (p11 - p01) * wx
            rgb[channel] = top + (bottom - top) * wy
        }
        return rgb
    }

    private fun texel(pixels: ByteArray, width: Int, height: Int, x: Int, y: Int, channel: Int): Float {
        val cx = x.coerceIn(0, width - 1)
        val cy = y.coerceIn(0, height - 1)
        return (pixels[(cy * width + cx) * 3 + channel].toInt() and 0xFF) / 255f
    }
}
