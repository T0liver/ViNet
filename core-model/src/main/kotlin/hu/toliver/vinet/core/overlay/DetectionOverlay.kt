package hu.toliver.vinet.core.overlay

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.pack.PackClass

/**
 * One detection eligible for on-screen overlay (manifest class color and label).
 * Empty when score is below that class's manifest [PackClass.minScore].
 */
data class OverlayBox(
    val detection: Detection,
    val className: String,
    val colorHex: String,
)

/**
 * Maps detections to overlay boxes using manifest classes and per-class minScore (AD-3).
 * Does not invent boxes when [detections] is empty.
 */
fun overlayBoxesForFrame(detections: List<Detection>, pack: ModelPack): List<OverlayBox> {
    if (detections.isEmpty()) return emptyList()
    val classById = pack.classes.associateBy { it.id }
    return detections.mapNotNull { detection ->
        val packClass = classById[detection.classId] ?: return@mapNotNull null
        if (detection.score < packClass.minScore) return@mapNotNull null
        OverlayBox(
            detection = detection,
            className = packClass.name,
            colorHex = packClass.color,
        )
    }
}
