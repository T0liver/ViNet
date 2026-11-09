package hu.toliver.vinet.core.port

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack

/**
 * Inference entry behind bundled runtimes (AD-10). Implementations live in `:inference`.
 */
interface InferenceBackend : AutoCloseable {
    val pack: ModelPack

    fun run(frame: Frame): List<Detection>
}
