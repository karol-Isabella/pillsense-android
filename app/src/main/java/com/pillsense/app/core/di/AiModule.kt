package com.pillsense.app.core.di

import android.content.Context
import com.pillsense.app.BuildConfig
import com.pillsense.app.core.ai.GeminiImageAnalyzer
import com.pillsense.app.core.ai.ImageAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideImageAnalyzer(): ImageAnalyzer {
        val apiKey = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() }
        return GeminiImageAnalyzer(apiKey)
    }
}
