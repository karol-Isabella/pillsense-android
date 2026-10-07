package com.pillsense.app.core.util

import org.junit.Test
import org.junit.Assert.*

class ResultTest {

    @Test
    fun testSuccessCreation() {
        val result = Result.Success("test data")
        assertEquals("test data", result.data)
    }

    @Test
    fun testSuccessGetOrNull() {
        val result = Result.Success("test")
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun testErrorGetOrNull() {
        val exception = Exception("test error")
        val result = Result.Error(exception)
        assertNull(result.getOrNull())
    }

    @Test
    fun testErrorExceptionOrNull() {
        val exception = Exception("test error")
        val result = Result.Error(exception)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun testSuccessMap() {
        val result = Result.Success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, (mapped as Result.Success).data)
    }

    @Test
    fun testErrorMap() {
        val exception = Exception("test error")
        val result: Result<Int> = Result.Error(exception)
        val mapped = result.map { it * 2 }
        assertEquals(exception, (mapped as Result.Error).exception)
    }

    @Test
    fun testOnSuccess() {
        var called = false
        Result.Success("test").onSuccess { called = true }
        assertTrue(called)
    }

    @Test
    fun testOnSuccessNotCalledForError() {
        var called = false
        Result.Error(Exception()).onSuccess { called = true }
        assertFalse(called)
    }
}
