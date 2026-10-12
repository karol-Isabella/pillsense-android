package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class WeekCountsTest {

    @Test
    fun emptyIntakesAndSchedules() {
        val counts = calculateWeekCounts(emptyList(), emptyList(), LocalDate.now())
        assertEquals(0, counts.taken)
        assertEquals(0, counts.omitted)
    }

    @Test
    fun twoOmittedZeroTaken() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val schedules = (0..1).map { Schedule("s$it", "m1", 8, weekStart.plusDays(it.toLong()), weekStart.plusDays(it.toLong())) }
        val counts = calculateWeekCounts(emptyList(), schedules, weekStart)
        assertEquals(0, counts.taken)
        assertEquals(2, counts.omitted)
    }
}
