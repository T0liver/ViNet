package hu.toliver.vinet.core.port

import hu.toliver.vinet.core.model.Frame
import kotlinx.coroutines.flow.Flow

interface FrameSource {
    val frames: Flow<Frame>
}
