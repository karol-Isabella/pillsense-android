package com.pillsense.app.feature.adherence.data

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ConsentStorage(private val encryptedPrefs: EncryptedSharedPreferences) {

    private fun getKeyForEmail(email: String): String = "adhereai_consent_${email.lowercase().trim()}"

    fun hasConsent(email: String = ""): Boolean = encryptedPrefs.getBoolean(getKeyForEmail(email), false)

    fun setConsent(email: String, granted: Boolean) {
        encryptedPrefs.edit().putBoolean(getKeyForEmail(email), granted).apply()
    }

    // Legacy methods for backward compatibility (no email parameter)
    fun hasConsent(): Boolean = hasConsent("")
    
    fun setConsent(granted: Boolean) {
        setConsent("", granted)
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
