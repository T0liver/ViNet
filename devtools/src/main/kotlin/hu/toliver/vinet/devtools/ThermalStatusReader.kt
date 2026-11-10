package hu.toliver.vinet.devtools

import android.content.Context
import android.os.Build
import android.os.PowerManager

fun interface ThermalStatusReader {
    fun readCelsius(): Float
}

object NoThermalStatusReader : ThermalStatusReader {
    override fun readCelsius(): Float = 0f
}

class AndroidThermalStatusReader(
    context: Context,
) : ThermalStatusReader {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    override fun readCelsius(): Float {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return 0f
        return when (powerManager.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE,
            PowerManager.THERMAL_STATUS_LIGHT,
            -> 32f
            PowerManager.THERMAL_STATUS_MODERATE -> 38f
            PowerManager.THERMAL_STATUS_SEVERE -> 42f
            PowerManager.THERMAL_STATUS_CRITICAL -> 46f
            PowerManager.THERMAL_STATUS_EMERGENCY,
            PowerManager.THERMAL_STATUS_SHUTDOWN,
            -> 50f
            else -> 0f
        }
    }
}
