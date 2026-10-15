package com.pillsense.app.core.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context
import com.pillsense.app.feature.adherence.data.ConsentStorage
import com.pillsense.app.feature.adherence.data.GeminiClient
import com.pillsense.app.feature.emergency.data.SmsNotificationStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdherenceModule {

    @Provides
    @Singleton
    fun encryptedSharedPreferences(@ApplicationContext context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "adhereai_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    @Provides
    @Singleton
    fun consentStorage(prefs: EncryptedSharedPreferences): ConsentStorage {
        return ConsentStorage(prefs)
    }

    @Provides
    @Singleton
    fun geminiClient(okHttpClient: OkHttpClient): GeminiClient {
        return GeminiClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun smsNotificationStorage(prefs: EncryptedSharedPreferences): SmsNotificationStorage {
        return SmsNotificationStorage(prefs)
    }
}
