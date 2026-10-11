package com.pillsense.app.core.security

import org.junit.Assert.*
import org.junit.Test

class AuthenticationLogicTest {
    @Test
    fun testEmailNormalization() {
        assertEquals("patient@example.com", AuthenticationLogic.normalizeEmail("  Patient@Example.com  "))
        assertEquals("patient@example.com", AuthenticationLogic.normalizeEmail("PATIENT@EXAMPLE.COM"))
    }

    @Test
    fun testEmailValidation() {
        assertTrue(AuthenticationLogic.validateEmail("user@example.com"))
        assertFalse(AuthenticationLogic.validateEmail("bad"))
        assertFalse(AuthenticationLogic.validateEmail("no@domain"))
    }

    @Test
    fun testPasswordValidation() {
        assertTrue(AuthenticationLogic.validatePassword("correct-password"))
        assertFalse(AuthenticationLogic.validatePassword("short"))
    }

    @Test
    fun testAccountKeyGeneration() {
        val key1 = AuthenticationLogic.generateAccountKey("user@example.com")
        val key2 = AuthenticationLogic.generateAccountKey("user@example.com")
        assertEquals(key1, key2)
        assertTrue(key1.startsWith("account-"))
    }

    @Test
    fun testPasswordDerivation() {
        val salt = AuthenticationLogic.generateSalt()
        val hash1 = AuthenticationLogic.derivePassword("password", salt)
        val hash2 = AuthenticationLogic.derivePassword("password", salt)
        assertArrayEquals(hash1, hash2)
    }

    @Test
    fun testDifferentSaltProducesDifferentHash() {
        val salt1 = AuthenticationLogic.generateSalt()
        val salt2 = AuthenticationLogic.generateSalt()
        val hash1 = AuthenticationLogic.derivePassword("password", salt1)
        val hash2 = AuthenticationLogic.derivePassword("password", salt2)
        assertFalse("Different salts should produce different hashes", hash1.contentEquals(hash2))
    }

    @Test
    fun testPasswordVerification() {
        val salt = AuthenticationLogic.generateSalt()
        val hash = AuthenticationLogic.derivePassword("correct", salt)
        val encoded = AuthenticationLogic.encode(hash)
        assertTrue(AuthenticationLogic.verifyPassword(encoded, "correct", salt))
        assertFalse(AuthenticationLogic.verifyPassword(encoded, "wrong", salt))
    }

    @Test
    fun testEncodeDecode() {
        val original = "test-data".toByteArray()
        val encoded = AuthenticationLogic.encode(original)
        val decoded = AuthenticationLogic.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun testPasswordStorageDoesNotContainPlaintext() {
        val salt = AuthenticationLogic.generateSalt()
        val password = "secret-password"
        val hash = AuthenticationLogic.derivePassword(password, salt)
        val encoded = AuthenticationLogic.encode(hash)
        assertFalse("Encoded hash should not contain plaintext password", encoded.contains(password))
    }
}
