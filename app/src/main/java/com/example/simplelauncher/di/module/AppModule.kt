package com.example.simplelauncher.di.module

import android.app.Application
import com.example.launcher.LauncherManager
import com.example.simplelauncher.utils.AppCoroutineDispatchers
import com.example.simplelauncher.utils.ICoroutineDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLauncherManager(application: Application) = LauncherManager.getInstance(application)

    @Provides
    fun provideCoroutineDispatchers(): ICoroutineDispatchers = AppCoroutineDispatchers()
}