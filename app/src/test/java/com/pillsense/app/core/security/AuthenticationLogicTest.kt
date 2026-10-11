package com.pillsense.app.core.security

import org.junit.Assert.*
import org.junit.Test

class AuthenticationLogicTest {
    @Test fun `name with 2-40 chars passes validation`() {
        assertTrue(AuthenticationLogic.validateName("Jo").isValid)
        assertTrue(AuthenticationLogic.validateName("John Doe").isValid)
        assertTrue(AuthenticationLogic.validateName("María-José O'Neill").isValid)
    }

    @Test fun `name too short fails`() {
        assertFalse(AuthenticationLogic.validateName("J").isValid)
        assertFalse(AuthenticationLogic.validateName("").isValid)
    }

    @Test fun `name too long fails`() {
        val long = "A".repeat(41)
        assertFalse(AuthenticationLogic.validateName(long).isValid)
    }

    @Test fun `name with invalid characters fails`() {
        assertFalse(AuthenticationLogic.validateName("John123").isValid)
        assertFalse(AuthenticationLogic.validateName("@Maria").isValid)
    }

    @Test fun `email max 254 chars passes`() {
        val valid = "a".repeat(240) + "@b.co"  // 240 + 5 = 245 chars
        assertTrue(AuthenticationLogic.validateEmail(valid, false).isValid)
    }

    @Test fun `email with 255 chars fails`() {
        val invalid = "a".repeat(244) + "@example.com"
        assertFalse(AuthenticationLogic.validateEmail(invalid, false).isValid)
    }

    @Test fun `email with spaces fails`() {
        assertFalse(AuthenticationLogic.validateEmail("john doe@example.com", false).isValid)
    }

    @Test fun `email without at fails`() {
        assertFalse(AuthenticationLogic.validateEmail("johnexample.com", false).isValid)
    }

    @Test fun `signup password min 8 chars with letter and number`() {
        assertTrue(AuthenticationLogic.validatePassword("Pass1234", true).isValid)
        assertFalse(AuthenticationLogic.validatePassword("Pass123", true).isValid) // 7 chars
        assertFalse(AuthenticationLogic.validatePassword("Password", true).isValid) // no number
        assertFalse(AuthenticationLogic.validatePassword("12345678", true).isValid) // no letter
    }

    @Test fun `login password only checks max 64 chars`() {
        assertTrue(AuthenticationLogic.validatePassword("short", false).isValid)
        assertTrue(AuthenticationLogic.validatePassword("Pass123", false).isValid)
        assertTrue(AuthenticationLogic.validatePassword("short".repeat(12), false).isValid)
        assertFalse(AuthenticationLogic.validatePassword("x".repeat(65), false).isValid)
    }

    @Test fun `old account with short password can login`() {
        val (salt, hash) = AuthenticationLogic.createCredentials("short")
        assertTrue(AuthenticationLogic.verifyPassword("short", salt, hash))
    }
}

