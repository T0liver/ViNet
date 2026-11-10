package hu.toliver.vinet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.core.port.LocationSource
import hu.toliver.vinet.location.IdleLocationSource
import hu.toliver.vinet.scan.BundledPackLoader
import hu.toliver.vinet.scan.DeveloperReplayControls
import hu.toliver.vinet.scan.OffDeveloperReplayControls
import hu.toliver.vinet.scan.LiveReplaySessionPorts
import hu.toliver.vinet.scan.NoScanHudRecorder
import hu.toliver.vinet.scan.ReplaySessionPorts
import hu.toliver.vinet.scan.ScanHudRecorder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReleaseScanModule {
    @Provides
    @Singleton
    fun provideScanPack(@ApplicationContext context: Context): ModelPack =
        BundledPackLoader.loadScanPack(context)

    @Provides
    @Singleton
    fun provideDetector(pack: ModelPack): Detector = object : Detector {
        override val pack: ModelPack = pack
        override fun detect(frame: Frame): List<Detection> = emptyList()
        override fun close() = Unit
    }

    @Provides
    @Singleton
    fun provideFrameSource(camera: CameraFrameSource): FrameSource = camera

    @Provides
    @Singleton
    fun provideLocationSource(): LocationSource = IdleLocationSource()

    @Provides
    @Singleton
    fun provideReplaySessionPorts(ports: LiveReplaySessionPorts): ReplaySessionPorts = ports

    @Provides
    @Singleton
    fun provideScanHudRecorder(): ScanHudRecorder = NoScanHudRecorder

    @Provides
    @Singleton
    fun provideDeveloperReplayControls(): DeveloperReplayControls = OffDeveloperReplayControls
}
