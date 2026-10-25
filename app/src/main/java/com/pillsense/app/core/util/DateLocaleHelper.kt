package com.pillsense.app.core.util

import java.util.Locale
import java.time.format.TextStyle

object DateLocaleHelper {
    fun getAppLocale(language: String): Locale = Locale(language)
    
    fun getShortDayName(language: String, dayOfWeek: java.time.DayOfWeek): String =
        dayOfWeek.getDisplayName(TextStyle.SHORT, getAppLocale(language))
}
