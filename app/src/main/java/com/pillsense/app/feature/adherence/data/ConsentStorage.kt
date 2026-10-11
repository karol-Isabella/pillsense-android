package com.pillsense.app.feature.adherence.data

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ConsentStorage(private val encryptedPrefs: EncryptedSharedPreferences) {

    fun hasConsent(): Boolean = encryptedPrefs.getBoolean("adhereai_consent", false)

    fun setConsent(granted: Boolean) {
        encryptedPrefs.edit().putBoolean("adhereai_consent", granted).apply()
    }

    fun getLastAdviceDate(): String? = encryptedPrefs.getString("last_advice_date", null)

    fun setLastAdviceDate(date: String) {
        encryptedPrefs.edit().putString("last_advice_date", date).apply()
    }

    fun getLastAdviceText(): String? = encryptedPrefs.getString("last_advice_text", null)

    fun setLastAdviceText(text: String) {
        encryptedPrefs.edit().putString("last_advice_text", text).apply()
    }
}
