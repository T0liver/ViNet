package hu.toliver.vinet.devtools

import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.inference.FakeDetector

/** Selects scripted capture ports for debug builds (AD-1). */
object DebugScanWiring {
    fun detector(pack: ModelPack): Detector = FakeDetector(pack)

    fun frameSource(pack: ModelPack): FrameSource =
        ScriptedFrameSource(
            width = pack.inputWidth,
            height = pack.inputHeight,
        )
}
