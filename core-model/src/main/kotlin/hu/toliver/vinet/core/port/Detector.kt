package hu.toliver.vinet.core.port

import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack

interface Detector : AutoCloseable {
    val pack: ModelPack

    /** Synchronous per-frame inference; an empty list is a gap (AD-4). */
    fun detect(frame: Frame): List<Detection>
}
