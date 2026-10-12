package com.pillsense.app.feature.adherence.domain

import java.time.LocalDate
import java.time.DayOfWeek

const val MIN_OMISSIONS_IN_SLOT = 3
const val ADHERENCE_THRESHOLD = 60f
const val PATTERN_DAYS = 7

data class ForgottenPattern(
    val medicationId: String,
    val type: String,
    val count: Int,
    val dayOfWeek: DayOfWeek? = null,
    val timeSlot: String? = null
)

fun findForgottenPatterns(
    intakes: List<Intake>,
    schedules: List<Schedule>,
    today: LocalDate = LocalDate.now()
): List<ForgottenPattern> {
    val patterns = mutableListOf<ForgottenPattern>()
    val lookbackStart = today.minusDays((PATTERN_DAYS - 1).toLong())

    val recentIntakes = intakes.filter { it.date in lookbackStart..today }
    val recentSchedules = schedules.filter { it.startDate <= today && (it.endDate == null || it.endDate >= lookbackStart) }

    recentSchedules.groupBy { it.medicationId }.forEach { (medId, medSchedules) ->
        val medIntakes = recentIntakes.filter { it.medicationId == medId }
        val medTaken = medIntakes.size.toFloat()
        val medScheduled = medSchedules.size.toFloat()

        if (medScheduled > 0) {
            val adherence = (medTaken / medScheduled * 100).coerceIn(0f, 100f)
            if (adherence < ADHERENCE_THRESHOLD) {
                patterns.add(ForgottenPattern("med_$medId", "LOW_ADHERENCE", medSchedules.size - medIntakes.size))
            }
        }

        listOf("morning", "afternoon", "night").forEach { slot ->
            val (minHour, maxHour) = when (slot) {
                "morning" -> 0 to 11
                "afternoon" -> 12 to 17
                else -> 18 to 23
            }
            val slotSchedules = medSchedules.filter { it.hour in minHour..maxHour }
            val slotIntakes = medIntakes.filter { it.hour in minHour..maxHour }
            if (slotSchedules.size - slotIntakes.size >= MIN_OMISSIONS_IN_SLOT) {
                patterns.add(ForgottenPattern("med_$medId", "MISSING_SLOT", slotSchedules.size - slotIntakes.size, timeSlot = slot))
            }
        }
    }

    return patterns
}
