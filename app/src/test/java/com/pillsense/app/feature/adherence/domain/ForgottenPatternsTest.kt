package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class ForgottenPatternsTest {
    private val today = LocalDate.of(2026, 10, 11)

    @Test
    fun noOmissionsReturnsEmptyPatterns() {
        val schedules = listOf(Schedule("s1", "m1", 8, today.minusDays(6), today))
        val intakes = listOf(Intake("i1", "m1", "s1", today, 8))
        val patterns = findForgottenPatterns(intakes, schedules, today)
        assertTrue(patterns.isEmpty() || patterns.none { it.type == "LOW_ADHERENCE" })
    }

    @Test
    fun lowAdherenceDetected() {
        val schedules = (0..6).map { Schedule("s$it", "m1", 8, today.minusDays((6 - it).toLong()), today) }
        val intakes = listOf(Intake("i1", "m1", "s1", today.minusDays(5), 8))
        val patterns = findForgottenPatterns(intakes, schedules, today)
        assertTrue(patterns.any { it.type == "LOW_ADHERENCE" })
    }

    @Test
    fun missingSlotDetected() {
        val schedules = (0..6).map { Schedule("s$it", "m1", 8, today.minusDays((6 - it).toLong()), today) }
        val patterns = findForgottenPatterns(emptyList(), schedules, today)
        assertTrue(patterns.any { it.type == "MISSING_SLOT" })
    }
}
