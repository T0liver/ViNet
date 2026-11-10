package hu.toliver.vinet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.toliver.vinet.camera.CameraFrameSource
import hu.toliver.vinet.camera.CameraXSession
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LiveCameraModule {
    @Provides
    @Singleton
    fun provideCameraFrameSource(@ApplicationContext context: Context): CameraFrameSource =
        CameraFrameSource(CameraXSession(context))
}
