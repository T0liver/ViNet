package hu.toliver.vinet.core.pack

data class ModelPack(
    val packId: String,
    val modelVersion: String,
    val family: String,
    val tier: String,
    val modelFileName: String,
    val sha256: String,
    val backend: String,
    val inputWidth: Int,
    val inputHeight: Int,
    val inputFormat: PackInputFormat,
    val outputFormat: String,
    val classes: List<PackClass>,
    val minTrackFrames: Int,
    val notes: String,
)

data class PackClass(
    val id: Int,
    val name: String,
    val color: String,
    val minScore: Float,
)

data class PackInputFormat(
    val colorOrder: String,
    val normalize: String,
    val letterbox: Boolean,
    val padValue: Int,
)
