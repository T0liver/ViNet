package hu.toliver.vinet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.toliver.vinet.core.model.Detection
import hu.toliver.vinet.core.model.Frame
import hu.toliver.vinet.core.pack.ModelPack
import hu.toliver.vinet.core.port.Detector
import hu.toliver.vinet.core.port.FrameSource
import hu.toliver.vinet.scan.BundledPackLoader
import javax.inject.Singleton
import kotlinx.coroutines.flow.emptyFlow

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
    fun provideFrameSource(): FrameSource = object : FrameSource {
        override val frames = emptyFlow<Frame>()
    }
}
