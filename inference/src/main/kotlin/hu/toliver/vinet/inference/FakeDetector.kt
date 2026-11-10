package hu.toliver.vinet.inference

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.model.NormalizedBox
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector

/**
 * Scripted detector for debug scan (AD-1). Returns exactly one box per frame.
 */
class FakeDetector(
    override val pack: ModelPack,
    private val scriptedClassId: Int = pack.classes.firstOrNull()?.id ?: 0,
    private val scriptedScore: Float = 0.85f,
    private val scriptedBox: NormalizedBox = NormalizedBox(0.22f, 0.28f, 0.58f, 0.62f),
) : Detector {

    override fun detect(frame: Frame): List<Detection> {
        return listOf(
            Detection(
                classId = scriptedClassId,
                score = scriptedScore,
                box = scriptedBox,
                frameTimestampNs = frame.timestampNs,
            ),
        )
    }

    override fun close() = Unit
}
