package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import com.pillsense.app.core.database.Intake
import java.time.LocalDate
import java.time.ZoneId

class WeekCountsTest {

    private val zone = ZoneId.systemDefault()
    private val now = System.currentTimeMillis()

    @Test
    fun emptyIntakesAndSchedules() {
        val weekStart = LocalDate.now()
        val counts = calculateWeekCounts(emptyList(), weekStart, now, zone)
        assertEquals(0, counts.taken)
        assertEquals(0, counts.postponed)
        assertEquals(0, counts.omitted)
        assertEquals(0, counts.pending)
    }

    @Test
    fun oneTakenStatus() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val weekStartEpoch = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val intakes = listOf(
            Intake("i1", "u", "m", weekStartEpoch + 1000, "TAKEN")
        )
        val counts = calculateWeekCounts(intakes, weekStart, now, zone)
        assertEquals(1, counts.taken)
        assertEquals(0, counts.postponed)
        assertEquals(0, counts.omitted)
        assertEquals(0, counts.pending)
    }

    @Test
    fun onePostponedStatus() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val weekStartEpoch = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val intakes = listOf(
            Intake("i1", "u", "m", weekStartEpoch + 1000, "POSTPONED")
        )
        val counts = calculateWeekCounts(intakes, weekStart, now, zone)
        assertEquals(0, counts.taken)
        assertEquals(1, counts.postponed)
        assertEquals(0, counts.omitted)
        assertEquals(0, counts.pending)
    }

    @Test
    fun oneSkippedStatus() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val weekStartEpoch = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val intakes = listOf(
            Intake("i1", "u", "m", weekStartEpoch + 1000, "SKIPPED")
        )
        val counts = calculateWeekCounts(intakes, weekStart, now, zone)
        assertEquals(0, counts.taken)
        assertEquals(0, counts.postponed)
        assertEquals(1, counts.omitted)
        assertEquals(0, counts.pending)
    }

    @Test
    fun onePendingStatus() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val weekStartEpoch = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val intakes = listOf(
            Intake("i1", "u", "m", weekStartEpoch + 1000, "PENDING")
        )
        val counts = calculateWeekCounts(intakes, weekStart, now, zone)
        assertEquals(0, counts.taken)
        assertEquals(0, counts.postponed)
        assertEquals(0, counts.omitted)
        assertEquals(1, counts.pending)
    }

    @Test
    fun mixedStatesSum() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val weekStartEpoch = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val intakes = listOf(
            Intake("i1", "u", "m", weekStartEpoch + 1000, "TAKEN"),
            Intake("i2", "u", "m", weekStartEpoch + 2000, "POSTPONED"),
            Intake("i3", "u", "m", weekStartEpoch + 3000, "SKIPPED"),
            Intake("i4", "u", "m", weekStartEpoch + 4000, "PENDING")
        )
        val counts = calculateWeekCounts(intakes, weekStart, now, zone)
        assertEquals(1, counts.taken)
        assertEquals(1, counts.postponed)
        assertEquals(1, counts.omitted)
        assertEquals(1, counts.pending)
        assertEquals(4, counts.taken + counts.postponed + counts.omitted + counts.pending)
    }

    @Test
    fun outsideWeekNotCounted() {
        val weekStart = LocalDate.of(2026, 10, 5)
        val weekBefore = weekStart.minusDays(1)
        val weekAfter = weekStart.plusDays(7)
        val beforeEpoch = weekBefore.atStartOfDay(zone).toInstant().toEpochMilli()
        val afterEpoch = weekAfter.atStartOfDay(zone).toInstant().toEpochMilli()
        val intakes = listOf(
            Intake("i1", "u", "m", beforeEpoch, "TAKEN"),
            Intake("i2", "u", "m", afterEpoch, "TAKEN")
        )
        val counts = calculateWeekCounts(intakes, weekStart, now, zone)
        assertEquals(0, counts.taken)
    }
}
