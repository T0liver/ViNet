package hu.toliver.vinet.scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

const val CAMERA_REQUIRED_MESSAGE = "Camera is required to scan the canopy."

fun missingReplayFileMessage(fileName: String): String = "Missing replay file: $fileName"

data class CameraStartDecision(
    val sessionStarts: Boolean,
    val message: String?,
    val openSystemSettings: Boolean,
)

fun cameraStartDecision(cameraPermissionGranted: Boolean): CameraStartDecision =
    if (cameraPermissionGranted) {
        CameraStartDecision(sessionStarts = true, message = null, openSystemSettings = false)
    } else {
        CameraStartDecision(
            sessionStarts = false,
            message = CAMERA_REQUIRED_MESSAGE,
            openSystemSettings = true,
        )
    }

data class SystemSettingsRequest(
    val action: String,
    val dataUri: String,
)

fun cameraPermissionSettings(packageName: String): SystemSettingsRequest =
    SystemSettingsRequest(
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        dataUri = "package:$packageName",
    )

fun openCameraSettings(context: Context) {
    val request = cameraPermissionSettings(context.packageName)
    val intent = Intent(request.action, Uri.parse(request.dataUri)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
