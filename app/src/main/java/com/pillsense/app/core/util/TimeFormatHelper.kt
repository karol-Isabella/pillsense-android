package com.pillsense.app.core.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeFormatHelper {
    fun format12Hour(time: LocalTime, language: String): String {
        val hour = time.hour
        val minute = time.minute
        val paddedMinute = String.format("%02d", minute)
        
        val (displayHour, period) = when {
            hour == 0 -> Pair(12, if (language == "es") "a. m." else "AM")
            hour < 12 -> Pair(hour, if (language == "es") "a. m." else "AM")
            hour == 12 -> Pair(12, if (language == "es") "p. m." else "PM")
            else -> Pair(hour - 12, if (language == "es") "p. m." else "PM")
        }
        
        return "$displayHour:$paddedMinute $period"
    }
    
    fun format12Hour(timeString: String, language: String): String {
        return try {
            val time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
            format12Hour(time, language)
        } catch (e: Exception) {
            timeString
        }
    }
}
