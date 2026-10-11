package com.pillsense.app.core.security

import org.junit.Assert.*
import org.junit.Test

class AuthenticationLogicTest {
    @Test fun `normalizes email to lowercase and trims whitespace`() {
        assertEquals("user@example.com", AuthenticationLogic.normalizeEmail("  User@EXAMPLE.COM  "))
        assertEquals("test+tag@domain.co.uk", AuthenticationLogic.normalizeEmail("TEST+TAG@DOMAIN.CO.UK"))
    }

    @Test fun `rejects invalid email formats`() {
        assertTrue(AuthenticationLogic.validateEmail("user@example.com"))
        assertFalse(AuthenticationLogic.validateEmail("invalid"))
        assertFalse(AuthenticationLogic.validateEmail("@nodomain"))
        assertFalse(AuthenticationLogic.validateEmail("user@"))
    }

    @Test fun `validates password length and content`() {
        assertTrue(AuthenticationLogic.validatePassword("validPass123"))
        assertFalse(AuthenticationLogic.validatePassword("short"))
        assertFalse(AuthenticationLogic.validatePassword("a".repeat(129)))
    }

    @Test fun `creates and verifies credentials consistently`() {
        val (salt, hash) = AuthenticationLogic.createCredentials("myPassword")
        assertTrue(salt.isNotEmpty() && hash.isNotEmpty())
        assertTrue(AuthenticationLogic.verifyPassword("myPassword", salt, hash))
        assertFalse(AuthenticationLogic.verifyPassword("wrongPassword", salt, hash))
    }

    @Test fun `hashes email consistently`() {
        val hash1 = AuthenticationLogic.hashEmail("user@example.com")
        val hash2 = AuthenticationLogic.hashEmail("user@example.com")
        assertEquals(hash1, hash2)
    }

    @Test fun `different passwords produce different hashes`() {
        val (salt1, hash1) = AuthenticationLogic.createCredentials("password1")
        val (salt2, hash2) = AuthenticationLogic.createCredentials("password2")
        assertNotEquals(hash1, hash2)
    }

    @Test fun `register then reinstall and login reproduces the issue`() {
        // Simula: registrar cuenta, reinstalar app (nuevas instancias), intentar login y re-registrar
        val stored = mutableMapOf<String, String>()
        val email = "user@example.com"
        val password = "securePass123"
        val name = "John"

        // Step 1: Registrar (primera app)
        val normalized = AuthenticationLogic.normalizeEmail(email)
        val (salt, hash) = AuthenticationLogic.createCredentials(password)
        val accountKey = "account-" + AuthenticationLogic.hashEmail(normalized)
        stored[accountKey] = "$salt:$hash"
        stored["profile-$normalized"] = name

        // Step 2: Reinstalar (nueva instancia, datos guardados persisten)
        val stored2 = stored.toMutableMap()

        // Step 3: Intentar login (debe funcionar)
        val parts = stored2[accountKey]!!.split(":")
        assertTrue("Login debe funcionar con la contraseña correcta",
            AuthenticationLogic.verifyPassword(password, parts[0], parts[1]))

        // Step 4: Intentar registrar de nuevo (debe rechazar, cuenta existe)
        val accountExists = stored2.containsKey(accountKey)
        assertTrue("Cuenta debe existir después de reinstalar", accountExists)

        // Step 5: Verificar que los datos persisten correctamente
        assertEquals(name, stored2["profile-$normalized"])
    }
}
