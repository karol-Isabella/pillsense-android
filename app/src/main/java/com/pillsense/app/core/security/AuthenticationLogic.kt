package com.pillsense.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class ValidationResult(val isValid: Boolean, val errorReason: String? = null)

/** Pure functions for authentication logic, independent of SecureStore or context. */
object AuthenticationLogic {
    fun normalizeEmail(email: String): String {
        return email.trim().lowercase(Locale.ROOT)
    }

    fun validateEmail(email: String, isSignup: Boolean = false): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return ValidationResult(false, "El correo es requerido")
        if (trimmed.length > 254) return ValidationResult(false, "El correo no puede exceder 254 caracteres")
        if (trimmed.contains(" ")) return ValidationResult(false, "El correo no puede contener espacios")
        if (!trimmed.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            return ValidationResult(false, "Introduce un correo válido (ej: usuario@ejemplo.com)")
        }
        return ValidationResult(true)
    }

    fun validateName(name: String): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return ValidationResult(false, "El nombre es requerido")
        if (trimmed.length < 2) return ValidationResult(false, "El nombre debe tener al menos 2 caracteres")
        if (trimmed.length > 40) return ValidationResult(false, "El nombre no puede exceder 40 caracteres")
        if (!trimmed.matches(Regex("^[a-záéíóúñ\\s\\-']+$", RegexOption.IGNORE_CASE))) {
            return ValidationResult(false, "El nombre solo puede contener letras, espacios, guiones y apóstrofes")
        }
        return ValidationResult(true)
    }

    fun validatePassword(password: String, isSignup: Boolean = false): ValidationResult {
        if (isSignup) {
            if (password.length < 8) return ValidationResult(false, "La contraseña debe tener al menos 8 caracteres")
            if (password.length > 32) return ValidationResult(false, "La contraseña no puede exceder 32 caracteres")
            if (!password.matches(Regex(".*[a-zA-Z].*"))) {
                return ValidationResult(false, "La contraseña debe contener al menos una letra")
            }
            if (!password.matches(Regex(".*\\d.*"))) {
                return ValidationResult(false, "La contraseña debe contener al menos un número")
            }
        } else {
            // Login: solo límite máximo para no bloquear cuentas viejas
            if (password.length > 64) return ValidationResult(false, "La contraseña no puede exceder 64 caracteres")
        }
        return ValidationResult(true)
    }

    fun hashEmail(email: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(email.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun createCredentials(password: String): Pair<String, String> {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivePassword(password, salt)
        return encode(salt) to encode(hash)
    }

    fun verifyPassword(password: String, saltEncoded: String, hashEncoded: String): Boolean {
        val salt = decode(saltEncoded)
        val derived = derivePassword(password, salt)
        val storedHash = decode(hashEncoded)
        return MessageDigest.isEqual(storedHash, derived)
    }

    fun generateAccountKey(email: String): String {
        return "account-" + hashEmail(email)
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
}
