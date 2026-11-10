package hu.toliver.vinet.inference.litert

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.InferenceBackend
import hu.toliver.vinet.inference.preprocess.ManifestPreprocess

/**
 * Runs one upright still through manifest preprocess and a LiteRT session.
 * An empty detection list is a gap; this does not invent a box (AD-4).
 */
class StandInStillScan(
    override val pack: ModelPack,
    private val session: LiteRtSession,
) : Detector, InferenceBackend {
    override fun detect(frame: Frame): List<Detection> = run(frame)

    override fun run(frame: Frame): List<Detection> {
        require(pack.backend == "litert") { "stand-in backend is ${pack.backend}" }
        val input = ManifestPreprocess.toModelInput(frame, pack)
        val expected = pack.inputWidth * pack.inputHeight * 3
        require(input.size == expected) { "preprocessed ${input.size} floats, model expects $expected" }
        return StandInDecoder.decode(session.run(input), frame, pack)
    }

    override fun close() {
        session.close()
    }
}
