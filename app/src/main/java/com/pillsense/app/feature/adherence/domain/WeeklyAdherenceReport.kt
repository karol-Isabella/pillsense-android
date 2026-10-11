package com.pillsense.app.feature.adherence.domain

import java.time.LocalDate
import java.time.DayOfWeek

data class WeeklyAdherenceReport(
    val weekStartDate: LocalDate,
    val weekPercentage: Float,
    val previousWeekPercentage: Float,
    val trend: Float,
    val byTimeSlot: Map<String, Float>,
    val byDayOfWeek: Map<DayOfWeek, Float>,
    val byMedication: Map<String, Float>
)

data class Intake(
    val id: String,
    val medicationId: String,
    val scheduleId: String,
    val date: LocalDate,
    val hour: Int
)

data class Schedule(
    val id: String,
    val medicationId: String,
    val hour: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?
) {
    fun isActiveOn(date: LocalDate): Boolean =
        date >= startDate && (endDate == null || date <= endDate)
}

fun calculateWeeklyAdherence(
    intakes: List<Intake>,
    schedules: List<Schedule>,
    weekStart: LocalDate
): WeeklyAdherenceReport {
    val weekEnd = weekStart.plusDays(6)
    val weekIntakes = intakes.filter { it.date in weekStart..weekEnd }
    val weekSchedules = schedules.filter { it.startDate <= weekEnd && (it.endDate == null || it.endDate >= weekStart) }

    val weekPercentage = if (weekSchedules.isNotEmpty()) {
        (weekIntakes.size.toFloat() / weekSchedules.size.toFloat() * 100).coerceIn(0f, 100f)
    } else 0f

    val prevWeekStart = weekStart.minusDays(7)
    val prevWeekEnd = weekStart.minusDays(1)
    val prevIntakes = intakes.filter { it.date in prevWeekStart..prevWeekEnd }
    val prevSchedules = schedules.filter { it.startDate <= prevWeekEnd && (it.endDate == null || it.endDate >= prevWeekStart) }
    val prevWeekPercentage = if (prevSchedules.isNotEmpty()) {
        (prevIntakes.size.toFloat() / prevSchedules.size.toFloat() * 100).coerceIn(0f, 100f)
    } else 0f

    val byTimeSlot = mutableMapOf("morning" to 0f, "afternoon" to 0f, "night" to 0f)
    listOf("morning", "afternoon", "night").forEach { slot ->
        val (minHour, maxHour) = when (slot) {
            "morning" -> 0 to 11
            "afternoon" -> 12 to 17
            else -> 18 to 23
        }
        val slotSchedules = weekSchedules.filter { it.hour in minHour..maxHour }
        val slotIntakes = weekIntakes.filter { it.hour in minHour..maxHour }
        byTimeSlot[slot] = if (slotSchedules.isNotEmpty()) {
            (slotIntakes.size.toFloat() / slotSchedules.size.toFloat() * 100).coerceIn(0f, 100f)
        } else 0f
    }

    val byDayOfWeek = mutableMapOf<DayOfWeek, Float>()
    for (day in 0..6) {
        val date = weekStart.plusDays(day.toLong())
        val daySchedules = weekSchedules.filter { it.isActiveOn(date) }
        val dayIntakes = weekIntakes.filter { it.date == date }
        byDayOfWeek[date.dayOfWeek] = if (daySchedules.isNotEmpty()) {
            (dayIntakes.size.toFloat() / daySchedules.size.toFloat() * 100).coerceIn(0f, 100f)
        } else 0f
    }

    val byMedication = weekSchedules.groupBy { it.medicationId }.mapValues { (medId, medSchedules) ->
        val medIntakes = weekIntakes.filter { it.medicationId == medId }
        if (medSchedules.isNotEmpty()) {
            (medIntakes.size.toFloat() / medSchedules.size.toFloat() * 100).coerceIn(0f, 100f)
        } else 0f
    }

    return WeeklyAdherenceReport(
        weekStartDate = weekStart,
        weekPercentage = weekPercentage,
        previousWeekPercentage = prevWeekPercentage,
        trend = weekPercentage - prevWeekPercentage,
        byTimeSlot = byTimeSlot,
        byDayOfWeek = byDayOfWeek,
        byMedication = byMedication
    )
}
