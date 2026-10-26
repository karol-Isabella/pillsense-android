package com.pillsense.app.core.util

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalTime

class TimeFormatHelperTest {
    
    @Test
    fun midnightFormatSpanish() {
        val time = LocalTime.of(0, 0)
        val result = TimeFormatHelper.format12Hour(time, "es")
        assertEquals("12:00 a. m.", result)
    }
    
    @Test
    fun midnightFormatEnglish() {
        val time = LocalTime.of(0, 0)
        val result = TimeFormatHelper.format12Hour(time, "en")
        assertEquals("12:00 AM", result)
    }
    
    @Test
    fun noonFormatSpanish() {
        val time = LocalTime.of(12, 0)
        val result = TimeFormatHelper.format12Hour(time, "es")
        assertEquals("12:00 p. m.", result)
    }
    
    @Test
    fun noonFormatEnglish() {
        val time = LocalTime.of(12, 0)
        val result = TimeFormatHelper.format12Hour(time, "en")
        assertEquals("12:00 PM", result)
    }
    
    @Test
    fun afternoonFormatSpanish() {
        val time = LocalTime.of(13, 5)
        val result = TimeFormatHelper.format12Hour(time, "es")
        assertEquals("1:05 p. m.", result)
    }
    
    @Test
    fun afternoonFormatEnglish() {
        val time = LocalTime.of(13, 5)
        val result = TimeFormatHelper.format12Hour(time, "en")
        assertEquals("1:05 PM", result)
    }
    
    @Test
    fun morningFormatSpanish() {
        val time = LocalTime.of(10, 30)
        val result = TimeFormatHelper.format12Hour(time, "es")
        assertEquals("10:30 a. m.", result)
    }
    
    @Test
    fun formatFromString() {
        val result = TimeFormatHelper.format12Hour("13:05", "en")
        assertEquals("1:05 PM", result)
    }
}
