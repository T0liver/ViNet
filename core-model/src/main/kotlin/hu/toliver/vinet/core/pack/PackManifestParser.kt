package hu.toliver.vinet.core.pack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PackManifestParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(manifestJson: String): ModelPack {
        val document = json.decodeFromString(ManifestDocument.serializer(), manifestJson)
        require(document.inputSize.size == 2) {
            "manifest inputSize must have width and height"
        }
        val classes = document.classes.map { entry ->
            PackClass(
                id = entry.id,
                name = entry.name,
                color = entry.color,
                minScore = entry.minScore,
            )
        }
        return ModelPack(
            packId = document.packId,
            modelVersion = document.modelVersion,
            family = document.family,
            tier = document.tier,
            modelFileName = document.file,
            sha256 = document.sha256,
            backend = document.backend,
            inputWidth = document.inputSize[0],
            inputHeight = document.inputSize[1],
            inputFormat = PackInputFormat(
                colorOrder = document.inputFormat.colorOrder,
                normalize = document.inputFormat.normalize,
                letterbox = document.inputFormat.letterbox,
                padValue = document.inputFormat.padValue,
            ),
            outputFormat = document.outputFormat,
            classes = classes,
            minTrackFrames = document.minTrackFrames,
            notes = document.notes,
        )
    }

    @Serializable
    private data class ManifestDocument(
        val packId: String,
        val modelVersion: String,
        val family: String,
        val tier: String,
        val file: String,
        val sha256: String,
        val backend: String,
        val inputSize: List<Int>,
        val inputFormat: InputFormatDocument,
        val outputFormat: String,
        val classes: List<ClassDocument>,
        val minTrackFrames: Int,
        val notes: String = "",
    )

    @Serializable
    private data class InputFormatDocument(
        val colorOrder: String,
        val normalize: String,
        val letterbox: Boolean,
        val padValue: Int,
    )

    @Serializable
    private data class ClassDocument(
        val id: Int,
        val name: String,
        val color: String,
        @SerialName("minScore") val minScore: Float,
    )
}
