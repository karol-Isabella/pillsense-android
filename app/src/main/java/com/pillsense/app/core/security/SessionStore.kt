package com.pillsense.app.core.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(val email: String, val name: String = "")

@Singleton
class SessionStore @Inject constructor(private val secure: SecureStore) {
    private val current = MutableStateFlow(secure.get("session"))
    val user = current.asStateFlow()
    
    suspend fun authenticate(email: String, password: String, name: String = "", register: Boolean) = withContext(Dispatchers.IO) {
        val normalized = AuthenticationLogic.normalizeEmail(email)
        require(AuthenticationLogic.validateEmail(normalized)) { "Introduce un correo válido." }
        require(AuthenticationLogic.validatePassword(password)) { "La contraseña debe tener entre 8 y 128 caracteres." }
        val accountKey = AuthenticationLogic.generateAccountKey(normalized)
        val existing = secure.get(accountKey)
        if (register) {
            require(existing == null) { "Ya existe una cuenta con ese correo en este teléfono." }
            val (salt, hash) = AuthenticationLogic.createCredentials(password)
            secure.put(accountKey, "$salt:$hash")
            secure.put("profile-$normalized", name.take(50))
        } else {
            require(existing != null) { "Correo o contraseña incorrectos." }
            val parts = existing.split(":")
            require(AuthenticationLogic.verifyPassword(password, parts[0], parts[1])) {
                "Correo o contraseña incorrectos."
            }
        }
        secure.put("session", normalized)
        current.value = normalized
    }
    
    fun getUserName(): String? {
        val email = current.value ?: return null
        return secure.get("profile-$email")?.takeIf { it.isNotBlank() }
    }
    
    fun logout() { secure.put("session", null); current.value = null }
}
