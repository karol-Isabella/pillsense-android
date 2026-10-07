package com.pillsense.app.core.security

import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore @Inject constructor(private val secure: SecureStore) {
    private val current = MutableStateFlow(secure.get("session"))
    val user = current.asStateFlow()
    suspend fun authenticate(email: String, password: String, register: Boolean) = withContext(Dispatchers.IO) {
        val normalized = email.trim().lowercase(Locale.ROOT)
        require(normalized.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) { "Introduce un correo válido." }
        require(password.length in 8..128) { "La contraseña debe tener entre 8 y 128 caracteres." }
        val accountKey = "account-" + MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray()).joinToString("") { "%02x".format(it) }
        val existing = secure.get(accountKey)
        if (register) {
            require(existing == null) { "Ya existe una cuenta con ese correo en este teléfono." }
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            secure.put(accountKey, encode(salt) + ":" + encode(derive(password, salt)))
        } else {
            require(existing != null) { "Correo o contraseña incorrectos." }
            val parts = existing.split(":")
            require(MessageDigest.isEqual(decode(parts[1]), derive(password, decode(parts[0])))) {
                "Correo o contraseña incorrectos."
            }
        }
        secure.put("session", normalized)
        current.value = normalized
    }
    fun logout() { secure.put("session", null); current.value = null }
    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 210_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
    private fun encode(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
    private fun decode(value: String) = Base64.getDecoder().decode(value)
}
