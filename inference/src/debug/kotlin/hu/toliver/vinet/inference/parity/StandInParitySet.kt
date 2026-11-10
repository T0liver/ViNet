package hu.toliver.vinet.inference.parity

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.model.NormalizedBox

/**
 * Fixed upright stills for the stand-in parity check.
 * Pixels stay in this debug source set, so the release build does not ship them.
 */
object StandInParitySet {
    private data class Image(
        val name: String,
        val width: Int,
        val height: Int,
        val timestampNs: Long,
        val pixels: ByteArray,
    )

    private fun images(): List<Image> = listOf(
        solid("solid-white", 8, 8, 101L, 255, 255, 255),
        solid("solid-black", 8, 8, 102L, 0, 0, 0),
        solid("wide-bright", 8, 2, 103L, 255, 255, 255),
        solid("red-square", 8, 8, 104L, 255, 0, 0),
        solid("yellow-wide", 8, 3, 105L, 255, 255, 0),
        gradient("gradient", 6, 5, 106L),
    )

    fun samples(referenceText: String): List<StandInParityCheck.Sample> {
        val images = images().associateBy { it.name }
        val samples = mutableListOf<StandInParityCheck.Sample>()
        var name: String? = null
        var inputSha256: String? = null
        val detections = mutableListOf<Detection>()

        fun finish() {
            val current = name ?: return
            val image = images[current] ?: error("unknown parity image $current")
            samples += StandInParityCheck.Sample(
                name = current,
                frame = frame(image),
                inputSha256 = requireNotNull(inputSha256),
                detections = detections.toList(),
            )
            name = null
            inputSha256 = null
            detections.clear()
        }

        for (raw in referenceText.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) {
                if (line.isEmpty()) finish()
                continue
            }
            if (line.startsWith("d ")) {
                val image = images[name] ?: error("detection before image name")
                val parts = line.removePrefix("d ").split(" ")
                require(parts.size == 6) { "bad detection line: $line" }
                detections += Detection(
                    classId = parts[0].toInt(),
                    score = parts[1].toFloat(),
                    box = NormalizedBox(
                        left = parts[2].toFloat(),
                        top = parts[3].toFloat(),
                        right = parts[4].toFloat(),
                        bottom = parts[5].toFloat(),
                    ),
                    frameTimestampNs = image.timestampNs,
                )
            } else {
                finish()
                val parts = line.split(" ")
                require(parts.size == 2) { "bad reference line: $line" }
                name = parts[0]
                inputSha256 = parts[1]
            }
        }
        finish()
        require(samples.map { it.name } == images().map { it.name }) {
            "reference images ${samples.map { it.name }} do not match the stand-in set"
        }
        return samples
    }

    private fun frame(image: Image): Frame = Frame(
        width = image.width,
        height = image.height,
        rotationDegrees = 0,
        timestampNs = image.timestampNs,
        pixels = image.pixels,
    )

    private fun solid(
        name: String,
        width: Int,
        height: Int,
        timestampNs: Long,
        red: Int,
        green: Int,
        blue: Int,
    ): Image {
        val pixels = ByteArray(width * height * 3)
        var index = 0
        repeat(width * height) {
            pixels[index++] = red.toByte()
            pixels[index++] = green.toByte()
            pixels[index++] = blue.toByte()
        }
        return Image(name, width, height, timestampNs, pixels)
    }

    private fun gradient(name: String, width: Int, height: Int, timestampNs: Long): Image {
        val pixels = ByteArray(width * height * 3)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = (y * width + x) * 3
                pixels[index] = (30 + x * 37 + y * 3).coerceIn(0, 255).toByte()
                pixels[index + 1] = (20 + y * 41).coerceIn(0, 255).toByte()
                pixels[index + 2] = (180 - x * 23 + y).coerceIn(0, 255).toByte()
            }
        }
        return Image(name, width, height, timestampNs, pixels)
    }
}
