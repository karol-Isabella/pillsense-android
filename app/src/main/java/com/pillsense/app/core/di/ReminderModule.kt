package com.pillsense.app.core.di

import com.pillsense.app.core.model.ReminderCoordinator
import com.pillsense.app.feature.reminders.ReminderEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module @InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds abstract fun reminders(engine: ReminderEngine): ReminderCoordinator
}
