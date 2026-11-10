package hu.toliver.vinet.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.toliver.vinet.scan.CAMERA_REQUIRED_MESSAGE
import hu.toliver.vinet.scan.ScanChromeInsets
import hu.toliver.vinet.scan.ScanPreviewControl
import hu.toliver.vinet.scan.missingReplayFileMessage
import hu.toliver.vinet.scan.ScanViewModel
import hu.toliver.vinet.scan.openCameraSettings
import hu.toliver.vinet.ui.ViNetColors

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionGranted by remember {
        mutableStateOf(hasCameraPermission(context))
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = hasCameraPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) viewModel.clearCameraDenied()
        else viewModel.noteCameraDenied()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        viewModel.onStartClicked(granted)
    }
    val showPreview = ScanPreviewControl.showCameraPreview(
        uiState.previewVisible,
        uiState.tractorMode,
    )
    val showOverlay = ScanPreviewControl.showDetectionOverlay(uiState.previewVisible)
    val dimScrim = ScanPreviewControl.applyDimScrim(uiState.tractorMode)
    val screenBlanked = ScanPreviewControl.blankScreenForTractor(uiState.tractorMode)
    val view = LocalView.current
    LaunchedEffect(screenBlanked) {
        val window = (view.context as? android.app.Activity)?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        if (screenBlanked && uiState.isRunning) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val safeDrawing = WindowInsets.safeDrawing
    val screenBlankedNow = screenBlanked && uiState.isRunning
    val chrome =
        ScanChromeInsets.chromePadding(
            leftPx = safeDrawing.getLeft(density, layoutDirection),
            topPx = safeDrawing.getTop(density),
            rightPx = safeDrawing.getRight(density, layoutDirection),
            bottomPx = safeDrawing.getBottom(density),
            screenBlanked = screenBlankedNow,
        )
    fun chromePadding(start: Dp, top: Dp, end: Dp, bottom: Dp): Modifier =
        Modifier.padding(
            start = with(density) { chrome.leftPx.toDp() } + start,
            top = with(density) { chrome.topPx.toDp() } + top,
            end = with(density) { chrome.rightPx.toDp() } + end,
            bottom = with(density) { chrome.bottomPx.toDp() } + bottom,
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ViNetColors.Surface),
    ) {
        if (showPreview) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FIT_CENTER
                    }
                },
                update = { previewView ->
                    val rotation = previewView.display?.rotation ?: android.view.Surface.ROTATION_0
                    viewModel.attachPreview(previewView.surfaceProvider, rotation)
                },
                onRelease = { viewModel.attachPreview(null, android.view.Surface.ROTATION_0) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (showOverlay) {
            DetectionOverlayCanvas(
                overlayBoxes = uiState.overlayBoxes,
                frameWidth = uiState.frameWidth,
                frameHeight = uiState.frameHeight,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (dimScrim && uiState.isRunning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
            )
        }
        if (screenBlanked && uiState.isRunning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }
        if (uiState.missingReplayFile != null) {
            Text(
                text = missingReplayFileMessage(uiState.missingReplayFile!!),
                color = ViNetColors.Ink,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
        }
        if (uiState.debugHudLines.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .then(chromePadding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                uiState.debugHudLines.forEach { line ->
                    Text(text = line, color = ViNetColors.Ink)
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .then(chromePadding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)),
        ) {
            DebugScanDeveloperSlot(
                viewModel = viewModel,
                developerControls = viewModel.developerControls(),
            )
        }
        if (uiState.cameraDeniedMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = CAMERA_REQUIRED_MESSAGE,
                    color = ViNetColors.Ink,
                )
                Button(
                    onClick = { openCameraSettings(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ViNetColors.Vine,
                        contentColor = ViNetColors.OnVine,
                    ),
                ) {
                    Text("Settings")
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .then(chromePadding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Session status: ${uiState.statusLabel}",
                color = ViNetColors.Ink,
            )
            Button(
                onClick = viewModel::cycleTractorMode,
                enabled = uiState.isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.SurfaceRaised,
                    contentColor = ViNetColors.Ink,
                ),
            ) {
                Text(uiState.tractorMode.label)
            }
            Button(
                onClick = viewModel::togglePreviewVisible,
                enabled = uiState.isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.SurfaceRaised,
                    contentColor = ViNetColors.Ink,
                ),
            ) {
                Text(if (uiState.previewVisible) "Hide preview" else "Show preview")
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(chromePadding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 24.dp)),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = {
                    if (permissionGranted) viewModel.onStartClicked(true)
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                enabled = !uiState.isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.Vine,
                    contentColor = ViNetColors.OnVine,
                ),
            ) {
                Text("Start")
            }
            Button(
                onClick = viewModel::stopScan,
                enabled = uiState.isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ViNetColors.Vine,
                    contentColor = ViNetColors.OnVine,
                ),
            ) {
                Text("Stop")
            }
        }
    }
}

private fun hasCameraPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
