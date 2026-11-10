package hu.toliver.vinet.scan

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import dagger.hilt.android.AndroidEntryPoint
import hu.toliver.vinet.LauncherActivity
import hu.toliver.vinet.R
import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.camera.VideoReplayFrameSource
import hu.toliver.vinet.core.model.GeoFix
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.core.port.LocationSource
import hu.toliver.vinet.location.GpxReplayLocationSource
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScanForegroundService : Service(), LifecycleOwner {
    @Inject lateinit var detector: Detector
    @Inject lateinit var scanPack: ModelPack
    @Inject lateinit var scanState: ScanStateRepository
    @Inject lateinit var configSource: DeviceConfigSnapshotSource
    @Inject lateinit var captureMemory: LiveCaptureMemory
    @Inject lateinit var replaySessionPorts: ReplaySessionPorts
    @Inject lateinit var scanHudRecorder: ScanHudRecorder

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var pipelineJob: Job? = null
    private var locationJob: Job? = null
    private var notificationJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var activeFrameSource: FrameSource? = null
    private var activeLocationSource: LocationSource? = null
    private var showDebugHud = false
    private val replayFixes = mutableListOf<GeoFix>()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startPipeline()
            ACTION_STOP -> stopPipeline()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopPipeline()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startPipeline() {
        if (pipelineJob?.isActive == true) return
        val granted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        val plan = replaySessionPorts.plan(granted)
        createNotificationChannel()
        if (!plan.sessionStarts) {
            plan.missingReplayFile?.let { scanState.showMissingReplayFile(it) }
            if (plan.requiresCameraPermission && !granted) {
                scanState.showCameraDenied()
            }
            stopSelf()
            return
        }
        activeFrameSource = plan.frameSource
        activeLocationSource = plan.locationSource
        showDebugHud = plan.showDebugHud
        startInForeground(includeCamera = plan.requiresCameraPermission && granted)
        val config = configSource.load()
        scanState.clearCameraDenied()
        scanState.clearMissingReplayFile()
        scanState.onServiceRunning(emptyList())
        notificationJob = serviceScope.launch {
            scanState.state.collect { uiState ->
                applyTractorWakeLock(uiState.isRunning, uiState.tractorMode)
                if (uiState.isRunning) {
                    val notification = buildNotification(uiState.tractorMode)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
        activeLocationSource?.start()
        locationJob = serviceScope.launch {
            val fixes = activeLocationSource?.fixes ?: return@launch
            fixes.collect { fix ->
                replayFixes += fix
            }
        }
        pipelineJob = serviceScope.launch {
            val source = requireNotNull(activeFrameSource)
            source.frames.collect { frame ->
                val inferenceStart = System.nanoTime()
                onLiveFrame(
                    frame = frame,
                    detector = detector,
                    pack = scanPack,
                    sink = captureMemory,
                    events = NoEventWriter,
                ) { boxes ->
                    scanState.updateOverlay(boxes, frame.width, frame.height)
                }
                val inferenceMs = (System.nanoTime() - inferenceStart) / 1_000_000f
                if (showDebugHud) {
                    scanHudRecorder.recordFrame(frame.timestampNs)
                    scanHudRecorder.recordInference(inferenceMs)
                    scanState.updateDebugHud(scanHudRecorder.hudLines())
                }
            }
        }
        when (val source = activeFrameSource) {
            is CameraFrameSource -> {
                if (lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED) {
                    lifecycleRegistry.currentState = Lifecycle.State.CREATED
                }
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
                source.start(this, config)
            }
            is VideoReplayFrameSource -> source.start()
        }
    }

    private fun stopPipeline() {
        (activeFrameSource as? CameraFrameSource)?.stop()
        (activeFrameSource as? VideoReplayFrameSource)?.stop()
        activeLocationSource?.stop()
        (activeLocationSource as? GpxReplayLocationSource)?.close()
        activeFrameSource = null
        activeLocationSource = null
        replayFixes.clear()
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        notificationJob?.cancel()
        notificationJob = null
        locationJob?.cancel()
        locationJob = null
        releaseWakeLock()
        pipelineJob?.cancel()
        pipelineJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        scanState.updateDebugHud(emptyList())
        scanState.onServiceStopped()
    }

    private fun startInForeground(includeCamera: Boolean) {
        val notification = buildNotification(scanState.state.value.tractorMode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            if (includeCamera && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            }
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(tractorMode: TractorDisplayMode): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, LauncherActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val content = ScanSessionNotification.content(
            sessionRunning = true,
            tractorMode = tractorMode,
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.scan_notification_title))
            .setContentText(content.bodyText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openIntent)
            .setOngoing(true)
        if (content.includesStopAction) {
            val stopIntent = PendingIntent.getService(
                this,
                1,
                Intent(this, ScanForegroundService::class.java).apply { action = ACTION_STOP },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(
                0,
                getString(R.string.scan_notification_stop),
                stopIntent,
            )
        }
        return builder.build()
    }

    private fun applyTractorWakeLock(running: Boolean, tractorMode: TractorDisplayMode) {
        if (running && tractorMode == TractorDisplayMode.ScreenOff) {
            acquireWakeLock()
        } else {
            releaseWakeLock()
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val manager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = manager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ViNet:scan",
        ).apply { acquire() }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.scan_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "hu.toliver.vinet.scan.START"
        const val ACTION_STOP = "hu.toliver.vinet.scan.STOP"
        private const val CHANNEL_ID = "scan_capture"
        private const val NOTIFICATION_ID = 42

        fun start(context: Context) {
            val intent = Intent(context, ScanForegroundService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ScanForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
