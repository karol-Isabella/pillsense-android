package com.pillsense.app.core.di

import com.pillsense.app.core.ai.LocalImageAnalyzer
import com.pillsense.app.core.ai.ImageAnalyzer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module @InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds abstract fun analyzer(analyzer: LocalImageAnalyzer): ImageAnalyzer
}
