package hu.toliver.vinet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.devtools.DebugScanWiring
import hu.toliver.vinet.scan.BundledPackLoader
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
    fun provideFrameSource(pack: ModelPack): FrameSource = DebugScanWiring.frameSource(pack)
}
