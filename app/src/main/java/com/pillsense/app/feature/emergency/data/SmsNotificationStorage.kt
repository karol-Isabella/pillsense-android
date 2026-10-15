package com.pillsense.app.feature.emergency.data

import androidx.security.crypto.EncryptedSharedPreferences

class SmsNotificationStorage(private val encryptedPrefs: EncryptedSharedPreferences) {

    private fun getKeyForEmail(email: String): String = "sms_notify_${email.lowercase().trim()}"

    fun isSmsNotificationEnabled(email: String = ""): Boolean = encryptedPrefs.getBoolean(getKeyForEmail(email), false)

    fun setSmsNotificationEnabled(email: String, enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(getKeyForEmail(email), enabled).apply()
    }
}
