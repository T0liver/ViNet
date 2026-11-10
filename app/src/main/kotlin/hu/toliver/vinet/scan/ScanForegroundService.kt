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
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import hu.toliver.vinet.LauncherActivity
import hu.toliver.vinet.R
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScanForegroundService : Service() {
    @Inject lateinit var detector: Detector
    @Inject lateinit var frameSource: FrameSource
    @Inject lateinit var scanPack: ModelPack
    @Inject lateinit var scanState: ScanStateRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var pipelineJob: Job? = null

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
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        scanState.onServiceRunning(emptyList())
        pipelineJob = serviceScope.launch {
            frameSource.frames.collect { frame ->
                val published = runScanPublishStep(detector, frame, scanPack)
                scanState.updateOverlay(published.overlayBoxes)
            }
        }
    }

    private fun stopPipeline() {
        pipelineJob?.cancel()
        pipelineJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        scanState.onServiceStopped()
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, LauncherActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.scan_notification_title))
            .setContentText(getString(R.string.scan_notification_running))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
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
