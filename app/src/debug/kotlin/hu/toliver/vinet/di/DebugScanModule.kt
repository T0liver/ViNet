package hu.toliver.vinet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.core.port.LocationSource
import hu.toliver.vinet.devtools.AndroidThermalStatusReader
import hu.toliver.vinet.devtools.DebugHudSampler
import hu.toliver.vinet.devtools.DebugReplayPreferences
import hu.toliver.vinet.devtools.DebugScanWiring
import hu.toliver.vinet.devtools.ThermalStatusReader
import hu.toliver.vinet.scan.DebugDeveloperReplayControls
import hu.toliver.vinet.scan.DebugScanHudRecorder
import hu.toliver.vinet.scan.DeveloperReplayControls
import hu.toliver.vinet.scan.ScanHudRecorder
import hu.toliver.vinet.scan.BundledPackLoader
import hu.toliver.vinet.scan.DebugReplaySessionPorts
import hu.toliver.vinet.scan.ReplaySessionPorts
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DebugScanModule {
    @Provides
    @Singleton
    fun provideScanPack(@ApplicationContext context: Context): ModelPack =
        BundledPackLoader.loadScanPack(context)

    @Provides
    @Singleton
    fun provideDetector(pack: ModelPack): Detector = DebugScanWiring.detector(pack)

    @Provides
    @Singleton
    fun provideDebugReplayPreferences(): DebugReplayPreferences = DebugReplayPreferences()

    @Provides
    @Singleton
    fun provideThermalStatusReader(@ApplicationContext context: Context): ThermalStatusReader =
        AndroidThermalStatusReader(context)

    @Provides
    @Singleton
    fun provideFrameSource(camera: CameraFrameSource): FrameSource = camera

    @Provides
    @Singleton
    fun provideLocationSource(): LocationSource =
        hu.toliver.vinet.location.IdleLocationSource()

    @Provides
    @Singleton
    fun provideReplaySessionPorts(ports: DebugReplaySessionPorts): ReplaySessionPorts = ports

    @Provides
    @Singleton
    fun provideDebugHudSampler(reader: ThermalStatusReader): DebugHudSampler =
        DebugScanWiring.hudSampler(reader)

    @Provides
    @Singleton
    fun provideScanHudRecorder(recorder: DebugScanHudRecorder): ScanHudRecorder = recorder

    @Provides
    @Singleton
    fun provideDeveloperReplayControls(controls: DebugDeveloperReplayControls): DeveloperReplayControls = controls
}
