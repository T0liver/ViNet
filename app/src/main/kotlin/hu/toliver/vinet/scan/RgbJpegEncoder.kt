package hu.toliver.vinet.scan

import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.model.NormalizedBox
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

data class EncodedJpegs(
    val fullJpeg: ByteArray,
    val cropThumbnail: ByteArray,
)

private const val THUMBNAIL_LONG_SIDE = 96
private const val JPEG_QUALITY = 80

fun encodeRgbFrame(frame: Frame, crop: NormalizedBox): EncodedJpegs {
    val pixels = requireNotNull(frame.pixels) { "frame has no pixels" }
    require(pixels.size == frame.width * frame.height * 3)
    val full = encodeRgbJpeg(pixels, frame.width, frame.height, JPEG_QUALITY)
    val cropped = cropRgb(pixels, frame.width, frame.height, crop)
    val thumb = scaleLongSide(cropped, THUMBNAIL_LONG_SIDE)
    val thumbnail = encodeRgbJpeg(thumb.rgb, thumb.width, thumb.height, JPEG_QUALITY)
    return EncodedJpegs(fullJpeg = full, cropThumbnail = thumbnail)
}

private data class RgbImage(val width: Int, val height: Int, val rgb: ByteArray)

private fun cropRgb(rgb: ByteArray, width: Int, height: Int, box: NormalizedBox): RgbImage {
    val left = (box.left * width).toInt().coerceIn(0, width - 1)
    val top = (box.top * height).toInt().coerceIn(0, height - 1)
    val right = (box.right * width).roundToInt().coerceIn(left + 1, width)
    val bottom = (box.bottom * height).roundToInt().coerceIn(top + 1, height)
    val cropW = right - left
    val cropH = bottom - top
    val out = ByteArray(cropW * cropH * 3)
    for (y in 0 until cropH) {
        val srcRow = (top + y) * width + left
        val dstRow = y * cropW
        System.arraycopy(rgb, srcRow * 3, out, dstRow * 3, cropW * 3)
    }
    return RgbImage(cropW, cropH, out)
}

private fun scaleLongSide(image: RgbImage, longSide: Int): RgbImage {
    val longest = maxOf(image.width, image.height)
    if (longest <= longSide) return image
    val scale = longSide.toFloat() / longest
    val dstW = maxOf(1, (image.width * scale).roundToInt())
    val dstH = maxOf(1, (image.height * scale).roundToInt())
    val out = ByteArray(dstW * dstH * 3)
    for (y in 0 until dstH) {
        val srcY = y * image.height / dstH
        for (x in 0 until dstW) {
            val srcX = x * image.width / dstW
            val src = (srcY * image.width + srcX) * 3
            val dst = (y * dstW + x) * 3
            out[dst] = image.rgb[src]
            out[dst + 1] = image.rgb[src + 1]
            out[dst + 2] = image.rgb[src + 2]
        }
    }
    return RgbImage(dstW, dstH, out)
}

internal fun encodeRgbJpeg(rgb: ByteArray, width: Int, height: Int, quality: Int): ByteArray {
    val yPlane = IntArray(width * height)
    val cbPlane = IntArray(width * height)
    val crPlane = IntArray(width * height)
    for (i in 0 until width * height) {
        val r = rgb[i * 3].toInt() and 0xFF
        val g = rgb[i * 3 + 1].toInt() and 0xFF
        val b = rgb[i * 3 + 2].toInt() and 0xFF
        yPlane[i] = (0.299 * r + 0.587 * g + 0.114 * b).roundToInt().coerceIn(0, 255)
        cbPlane[i] = (128 - 0.168736 * r - 0.331264 * g + 0.5 * b).roundToInt().coerceIn(0, 255)
        crPlane[i] = (128 + 0.5 * r - 0.418688 * g - 0.081312 * b).roundToInt().coerceIn(0, 255)
    }
    val quantY = scaleQuant(LUMA_QUANT, quality)
    val quantC = scaleQuant(CHROMA_QUANT, quality)
    val bits = BitWriter()
    var dcY = 0
    var dcCb = 0
    var dcCr = 0
    val blocksX = (width + 7) / 8
    val blocksY = (height + 7) / 8
    for (by in 0 until blocksY) {
        for (bx in 0 until blocksX) {
            dcY = writeBlock(bits, yPlane, width, height, bx, by, quantY, dcY, DC_LUMA, AC_LUMA)
            dcCb = writeBlock(bits, cbPlane, width, height, bx, by, quantC, dcCb, DC_LUMA, AC_LUMA)
            dcCr = writeBlock(bits, crPlane, width, height, bx, by, quantC, dcCr, DC_LUMA, AC_LUMA)
        }
    }
    bits.pad()
    return jpegContainer(width, height, quantY, quantC, bits.bytes)
}

private fun writeBlock(
    bits: BitWriter,
    plane: IntArray,
    width: Int,
    height: Int,
    blockX: Int,
    blockY: Int,
    quant: IntArray,
    prevDc: Int,
    dcTable: HuffmanTable,
    acTable: HuffmanTable,
): Int {
    val samples = DoubleArray(64)
    for (y in 0 until 8) {
        val srcY = (blockY * 8 + y).coerceAtMost(height - 1)
        for (x in 0 until 8) {
            val srcX = (blockX * 8 + x).coerceAtMost(width - 1)
            samples[y * 8 + x] = plane[srcY * width + srcX] - 128.0
        }
    }
    val quantized = IntArray(64)
    for (v in 0 until 8) {
        for (u in 0 until 8) {
            var sum = 0.0
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    sum += samples[y * 8 + x] * COS[u][x] * COS[v][y]
                }
            }
            val cu = if (u == 0) INV_SQRT2 else 1.0
            val cv = if (v == 0) INV_SQRT2 else 1.0
            val coeff = (cu * cv * 0.25) * sum
            quantized[v * 8 + u] = (coeff / quant[v * 8 + u]).roundToInt().coerceIn(-2047, 2047)
        }
    }
    val zigzag = IntArray(64) { quantized[ZIGZAG[it]] }
    val diff = zigzag[0] - prevDc
    writeCoefficient(bits, diff, dcTable)
    var run = 0
    for (i in 1 until 64) {
        val value = zigzag[i]
        if (value == 0) {
            run++
        } else {
            while (run >= 16) {
                bits.writeCode(acTable, 0xF0)
                run -= 16
            }
            val cat = category(value)
            bits.writeCode(acTable, (run shl 4) or cat)
            bits.writeBits(amplitude(value, cat), cat)
            run = 0
        }
    }
    if (run > 0) bits.writeCode(acTable, 0x00)
    return zigzag[0]
}

private fun writeCoefficient(bits: BitWriter, value: Int, table: HuffmanTable) {
    val cat = category(value)
    bits.writeCode(table, cat)
    bits.writeBits(amplitude(value, cat), cat)
}

private fun category(value: Int): Int {
    var remaining = abs(value)
    var bits = 0
    while (remaining > 0) {
        remaining = remaining shr 1
        bits++
    }
    return bits
}

private fun amplitude(value: Int, cat: Int): Int {
    if (cat == 0) return 0
    return if (value < 0) value + (1 shl cat) - 1 else value
}

private class BitWriter {
    private val out = ArrayList<Byte>()
    private var acc = 0
    private var n = 0
    val bytes: ByteArray
        get() = out.toByteArray()

    fun writeCode(table: HuffmanTable, symbol: Int) {
        val code = table.code(symbol)
        writeBits(code.first, code.second)
    }

    fun writeBits(value: Int, length: Int) {
        var remaining = length
        while (remaining > 0) {
            val bit = (value shr (remaining - 1)) and 1
            acc = (acc shl 1) or bit
            n++
            if (n == 8) flushByte()
            remaining--
        }
    }

    fun pad() {
        if (n == 0) return
        acc = (acc shl (8 - n)) or ((1 shl (8 - n)) - 1)
        n = 8
        flushByte()
    }

    private fun flushByte() {
        out.add(acc.toByte())
        if (acc == 0xFF) out.add(0)
        acc = 0
        n = 0
    }
}

private class HuffmanTable(bits: IntArray, values: IntArray) {
    private val codes = HashMap<Int, Pair<Int, Int>>()

    init {
        var code = 0
        var index = 0
        for (length in 1..16) {
            repeat(bits[length - 1]) {
                codes[values[index++]] = code to length
                code++
            }
            code = code shl 1
        }
    }

    fun code(symbol: Int): Pair<Int, Int> = codes[symbol]
        ?: throw IllegalArgumentException("missing huffman symbol $symbol")
}

private fun scaleQuant(base: IntArray, quality: Int): IntArray {
    val q = quality.coerceIn(1, 100)
    val scale = if (q < 50) 5000 / q else 200 - q * 2
    return IntArray(64) { index ->
        ((base[index] * scale + 50) / 100).coerceIn(1, 255)
    }
}

private fun jpegContainer(
    width: Int,
    height: Int,
    quantY: IntArray,
    quantC: IntArray,
    entropy: ByteArray,
): ByteArray {
    val out = ArrayList<Byte>(entropy.size + 512)
    fun mark(vararg bytes: Int) {
        bytes.forEach { out.add(it.toByte()) }
    }
    fun word(value: Int) {
        out.add((value shr 8).toByte())
        out.add((value and 0xFF).toByte())
    }
    mark(0xFF, 0xD8)
    mark(0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00)
    writeQuant(out, 0, quantY)
    writeQuant(out, 1, quantC)
    mark(0xFF, 0xC0)
    word(17)
    mark(8)
    word(height)
    word(width)
    mark(3, 1, 0x11, 0, 2, 0x11, 1, 3, 0x11, 1)
    writeHuffman(out, 0, DC_LUMA_BITS, DC_LUMA_VALUES)
    writeHuffman(out, 0x10, AC_LUMA_BITS, AC_LUMA_VALUES)
    mark(0xFF, 0xDA)
    word(12)
    mark(3, 1, 0x00, 2, 0x00, 3, 0x00, 0, 63, 0)
    entropy.forEach { out.add(it) }
    mark(0xFF, 0xD9)
    return out.toByteArray()
}

private fun writeQuant(out: ArrayList<Byte>, id: Int, natural: IntArray) {
    out.add(0xFF.toByte())
    out.add(0xDB.toByte())
    out.add(0)
    out.add(67)
    out.add(id.toByte())
    for (i in 0 until 64) out.add(natural[ZIGZAG[i]].toByte())
}

private fun writeHuffman(out: ArrayList<Byte>, classAndId: Int, bits: IntArray, values: IntArray) {
    out.add(0xFF.toByte())
    out.add(0xC4.toByte())
    val length = 2 + 1 + 16 + values.size
    out.add((length shr 8).toByte())
    out.add((length and 0xFF).toByte())
    out.add(classAndId.toByte())
    bits.forEach { out.add(it.toByte()) }
    values.forEach { out.add(it.toByte()) }
}

private val INV_SQRT2 = 1.0 / kotlin.math.sqrt(2.0)
private val COS: Array<DoubleArray> = Array(8) { u ->
    DoubleArray(8) { x -> cos(((2 * x + 1) * u * PI) / 16.0) }
}

private val ZIGZAG = intArrayOf(
    0, 1, 8, 16, 9, 2, 3, 10,
    17, 24, 32, 25, 18, 11, 4, 5,
    12, 19, 26, 33, 40, 48, 41, 34,
    27, 20, 13, 6, 7, 14, 21, 28,
    35, 42, 49, 56, 57, 50, 43, 36,
    29, 22, 15, 23, 30, 37, 44, 51,
    58, 59, 52, 45, 38, 31, 39, 46,
    53, 60, 61, 54, 47, 55, 62, 63,
)

private val LUMA_QUANT = intArrayOf(
    16, 11, 10, 16, 24, 40, 51, 61,
    12, 12, 14, 19, 26, 58, 60, 55,
    14, 13, 16, 24, 40, 57, 69, 56,
    14, 17, 22, 29, 51, 87, 80, 62,
    18, 22, 37, 56, 68, 109, 103, 77,
    24, 35, 55, 64, 81, 104, 113, 92,
    49, 64, 78, 87, 103, 121, 120, 101,
    72, 92, 95, 98, 112, 100, 103, 99,
)

private val CHROMA_QUANT = intArrayOf(
    17, 18, 24, 47, 99, 99, 99, 99,
    18, 21, 26, 66, 99, 99, 99, 99,
    24, 26, 56, 99, 99, 99, 99, 99,
    47, 66, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99,
)

private val DC_LUMA_BITS = intArrayOf(0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0)
private val DC_LUMA_VALUES = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
private val AC_LUMA_BITS = intArrayOf(0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7D)
private val AC_LUMA_VALUES = intArrayOf(
    0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07,
    0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xA1, 0x08, 0x23, 0x42, 0xB1, 0xC1, 0x15, 0x52, 0xD1, 0xF0,
    0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0A, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x25, 0x26, 0x27, 0x28,
    0x29, 0x2A, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49,
    0x4A, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
    0x6A, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89,
    0x8A, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7,
    0xA8, 0xA9, 0xAA, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xC2, 0xC3, 0xC4, 0xC5,
    0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xE1, 0xE2,
    0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8,
    0xF9, 0xFA,
)

private val DC_LUMA = HuffmanTable(DC_LUMA_BITS, DC_LUMA_VALUES)
private val AC_LUMA = HuffmanTable(AC_LUMA_BITS, AC_LUMA_VALUES)
