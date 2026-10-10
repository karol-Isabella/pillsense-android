package com.pillsense.app.core.security

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SessionStoreTest {
    private val values = mutableMapOf<String, String>()
    private val secure = mockk<SecureStore>()
    init {
        every { secure.get(any()) } answers { values[firstArg<String>()] }
        every { secure.put(any(), any()) } answers {
            val value = secondArg<String?>()
            if (value == null) values.remove(firstArg()) else values[firstArg()] = value
            Unit
        }
    }
    @Test fun `registers normalized account and verifies credentials`() = runTest {
        val store = SessionStore(secure)
        store.authenticate("  Patient@Example.com  ", "correct-password", true)
        assertEquals("patient@example.com", store.user.value)
        store.logout()
        store.authenticate("PATIENT@example.com", "correct-password", false)
        assertEquals("patient@example.com", store.user.value)
    }
    @Test fun `wrong password never authenticates`() = runTest {
        val store = SessionStore(secure)
        store.authenticate("patient@example.com", "correct-password", true)
        store.logout()
        assertTrue(runCatching { store.authenticate("patient@example.com", "wrong-password", false) }.isFailure)
        assertNull(store.user.value)
    }
    @Test fun `duplicate registration cannot replace credentials`() = runTest {
        val store = SessionStore(secure)
        store.authenticate("patient@example.com", "correct-password", true)
        store.logout()
        assertTrue(runCatching { store.authenticate("patient@example.com", "another-password", true) }.isFailure)
        store.authenticate("patient@example.com", "correct-password", false)
        assertNotNull(store.user.value)
    }
    @Test fun `password storage uses different salted hashes`() = runTest {
        val store = SessionStore(secure)
        store.authenticate("first@example.com", "shared-password", true)
        store.logout()
        store.authenticate("second@example.com", "shared-password", true)
        val hashes = values.filterKeys { it.startsWith("account-") }.values.toList()
        assertEquals(2, hashes.distinct().size)
        assertTrue(hashes.none { it.contains("shared-password") })
    }
    @Test fun `logout survives recreation and keeps account available`() = runTest {
        val store = SessionStore(secure)
        store.authenticate("patient@example.com", "correct-password", true)
        assertEquals("patient@example.com", SessionStore(secure).user.value)
        store.logout()
        assertNull(SessionStore(secure).user.value)
        assertEquals(1, values.keys.count { it.startsWith("account-") })
    }
    @Test fun `invalid inputs and unknown accounts do not create sessions`() = runTest {
        val store = SessionStore(secure)
        assertTrue(runCatching { store.authenticate("bad", "correct-password", true) }.isFailure)
        assertTrue(runCatching { store.authenticate("patient@example.com", "short", true) }.isFailure)
        assertTrue(runCatching { store.authenticate("unknown@example.com", "correct-password", false) }.isFailure)
        assertNull(store.user.value)
        assertTrue(values.isEmpty())
    }
}
