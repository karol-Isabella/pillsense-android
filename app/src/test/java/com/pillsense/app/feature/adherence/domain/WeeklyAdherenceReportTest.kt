package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.DayOfWeek

class WeeklyAdherenceReportTest {
    private val weekStart = LocalDate.of(2026, 10, 5)

    @Test
    fun emptyWeekReturnsZeroPercentage() {
        val report = calculateWeeklyAdherence(emptyList(), emptyList(), weekStart)
        assertEquals(0f, report.weekPercentage)
    }

    @Test
    fun noIntakesReturnsZeroPercentage() {
        val schedules = listOf(Schedule("s1", "m1", 8, weekStart, weekStart.plusDays(6)))
        val report = calculateWeeklyAdherence(emptyList(), schedules, weekStart)
        assertEquals(0f, report.weekPercentage)
    }

    @Test
    fun hundredPercentAdherence() {
        val schedule = Schedule("s1", "m1", 8, weekStart, weekStart.plusDays(6))
        val intakes = (0..6).map { Intake("i$it", "m1", "s1", weekStart.plusDays(it.toLong()), 8) }
        val report = calculateWeeklyAdherence(intakes, listOf(schedule), weekStart)
        assertEquals(100f, report.weekPercentage)
    }

    @Test
    fun mixedAdherence() {
        val schedules = (0..6).map { Schedule("s$it", "m1", 8, weekStart.plusDays(it.toLong()), weekStart.plusDays(it.toLong())) }
        val intakes = listOf(
            Intake("i1", "m1", "s1", weekStart, 8),
            Intake("i2", "m1", "s2", weekStart.plusDays(1), 8)
        )
        val report = calculateWeeklyAdherence(intakes, schedules, weekStart)
        assertTrue(report.weekPercentage > 0f && report.weekPercentage < 100f)
    }
}
