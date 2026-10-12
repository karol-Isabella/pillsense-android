package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class WeekCountsTest {

    @Test
    fun emptyIntakesAndSchedules() {
        val counts = calculateWeekCounts(emptyList(), emptyList(), LocalDate.now())
        assertEquals(0, counts.taken)
        assertEquals(0, counts.postponed)
        assertEquals(0, counts.omitted)
        assertEquals(0, counts.pending)
    }

    @Test
    fun twoOmittedZeroTaken() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val schedules = (0..1).map { Schedule("s$it", "m1", 8, weekStart.plusDays(it.toLong()), weekStart.plusDays(it.toLong())) }
        val counts = calculateWeekCounts(emptyList(), schedules, weekStart)
        assertEquals(0, counts.taken)
        assertEquals(2, counts.omitted)
    }

    @Test
    fun oneTakenOneOmitted() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val schedules = listOf(
            Schedule("s1", "m1", 8, weekStart, weekStart),
            Schedule("s2", "m1", 8, weekStart.plusDays(1), weekStart.plusDays(1))
        )
        val intakes = listOf(
            Intake("i1", "m1", "s1", weekStart, 8)
        )
        val counts = calculateWeekCounts(intakes, schedules, weekStart)
        assertEquals(1, counts.taken)
        assertEquals(1, counts.omitted)
    }

    @Test
    fun allTakenInWeek() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val schedules = (0..4).map { Schedule("s$it", "m1", 8, weekStart.plusDays(it.toLong()), weekStart.plusDays(it.toLong())) }
        val intakes = (0..4).map { Intake("i$it", "m1", "s$it", weekStart.plusDays(it.toLong()), 8) }
        val counts = calculateWeekCounts(intakes, schedules, weekStart)
        assertEquals(5, counts.taken)
        assertEquals(0, counts.omitted)
    }

    @Test
    fun mixedScenario() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val schedules = (0..5).map { Schedule("s$it", "m1", 8, weekStart.plusDays(it.toLong()), weekStart.plusDays(it.toLong())) }
        val intakes = listOf(
            Intake("i1", "m1", "s1", weekStart.plusDays(1), 8),
            Intake("i2", "m1", "s2", weekStart.plusDays(2), 8),
            Intake("i3", "m1", "s4", weekStart.plusDays(4), 8)
        )
        val counts = calculateWeekCounts(intakes, schedules, weekStart)
        assertEquals(3, counts.taken)
        assertEquals(3, counts.omitted)  // s0, s3, s5 have no intake
    }

    @Test
    fun noSchedulesInWeek() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val schedules = emptyList<Schedule>()
        val intakes = emptyList<Intake>()
        val counts = calculateWeekCounts(intakes, schedules, weekStart)
        assertEquals(0, counts.taken)
        assertEquals(0, counts.omitted)
    }
}
