package hu.toliver.vinet.devtools

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory replay selection for debug builds; the Settings surface will persist this later. */
class DebugReplayPreferences {
    private val mutableSettings = MutableStateFlow(DebugReplaySettings())
    val settings: StateFlow<DebugReplaySettings> = mutableSettings.asStateFlow()

    fun update(transform: (DebugReplaySettings) -> DebugReplaySettings) {
        mutableSettings.value = transform(mutableSettings.value)
    }

    fun current(): DebugReplaySettings = mutableSettings.value
}
