package com.pillsense.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Pure functions for authentication logic, independent of SecureStore or context. */
object AuthenticationLogic {
    fun normalizeEmail(email: String): String {
        return email.trim().lowercase(Locale.ROOT)
    }

    fun validateEmail(email: String): Boolean {
        return email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
    }

    fun validatePassword(password: String): Boolean {
        return password.length in 8..128
    }

    fun generateAccountKey(email: String): String {
        return "account-" + MessageDigest.getInstance("SHA-256")
            .digest(email.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun generateSalt(): ByteArray {
        return ByteArray(16).also { SecureRandom().nextBytes(it) }
    }

    fun derivePassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 210_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun encode(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun decode(value: String): ByteArray {
        return Base64.getDecoder().decode(value)
    }

    fun verifyPassword(storedHash: String, password: String, salt: ByteArray): Boolean {
        val derived = derivePassword(password, salt)
        return MessageDigest.isEqual(decode(storedHash), derived)
    }
}
