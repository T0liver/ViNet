package hu.toliver.vinet.scan

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.toliver.vinet.core.config.ConfigSnapshot
import javax.inject.Inject

interface ConfigPreferenceReader {
    fun getLong(name: String): Long?
    fun getFloat(name: String): Float?
}

fun loadConfigSnapshot(preferences: ConfigPreferenceReader): ConfigSnapshot {
    val base = ConfigSnapshot.uninitialized()
    return base.copy(
        exposureCapNs = preferences.getLong(ConfigSnapshot.EXPOSURE_CAP_NAME) ?: base.exposureCapNs,
        focusDistance = preferences.getFloat(ConfigSnapshot.FOCUS_DISTANCE_NAME) ?: base.focusDistance,
    )
}

class DeviceConfigSnapshotSource @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = SharedConfigPreferences(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    )

    fun load(): ConfigSnapshot = loadConfigSnapshot(preferences)

    private class SharedConfigPreferences(
        private val preferences: SharedPreferences,
    ) : ConfigPreferenceReader {
        override fun getLong(name: String): Long? =
            if (preferences.contains(name)) preferences.getLong(name, 0L) else null

        override fun getFloat(name: String): Float? =
            if (preferences.contains(name)) preferences.getFloat(name, 0f) else null
    }

    private companion object {
        const val PREFS_NAME = "vinet_config"
    }
}
