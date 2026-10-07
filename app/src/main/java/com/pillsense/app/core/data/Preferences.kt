package com.pillsense.app.core.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settings by preferencesDataStore("settings")
@Singleton
class Preferences @Inject constructor(@ApplicationContext private val context: Context) {
    val dark = context.settings.data.map { it[booleanPreferencesKey("dark")] ?: false }
    val language = context.settings.data.map { it[stringPreferencesKey("language")] ?: "es" }
    suspend fun setDark(value: Boolean) { context.settings.edit { it[booleanPreferencesKey("dark")] = value } }
    suspend fun setLanguage(value: String) { context.settings.edit { it[stringPreferencesKey("language")] = value } }
}
