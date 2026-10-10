package com.pillsense.app.core.model

import com.pillsense.app.core.database.Intake
import com.pillsense.app.core.database.Medication
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ScheduleTest {
    private val zone = ZoneId.of("America/Bogota")
    private fun med(first: String = "08:35", hours: Int = 8, duration: Int? = 2, created: String = "2026-10-06T07:00:00") =
        Medication("med", "user", "Medication", "dose", "tablet", hours, first, "", "2026-10-06", duration,
            LocalDateTime.parse(created).atZone(zone).toInstant().toEpochMilli())

    @Test fun `retains minutes and wraps midnight`() {
        assertEquals(listOf("00:35", "08:35", "16:35"), Schedule.times("08:35", 8))
        assertEquals(listOf("07:50", "19:50"), Schedule.times("19:50", 12))
    }
    @Test fun `rejects invalid hours minutes and unsupported intervals`() {
        listOf("24:00", "08:75", "8:00", "no", "-1:00").forEach {
            assertTrue(runCatching { Schedule.times(it, 8) }.isFailure)
        }
        assertTrue(runCatching { Schedule.times("08:00", 7) }.isFailure)
    }
    @Test fun `first day never schedules before first dose`() {
        val rows = Schedule.occurrences(med("22:15"), LocalDate.of(2026, 10, 7), zone)
        assertEquals(listOf("2026-10-06T22:15", "2026-10-07T06:15", "2026-10-07T14:15", "2026-10-07T22:15"),
            rows.map { Instant.ofEpochMilli(it.scheduledAt).atZone(zone).toLocalDateTime().toString() })
    }
    @Test fun `duration excludes days beyond treatment and IDs are repeatable`() {
        val rows = Schedule.occurrences(med(duration = 1), LocalDate.of(2026, 10, 20), zone)
        assertEquals(2, rows.size)
        assertEquals(rows.map { it.id }, Schedule.occurrences(med(duration = 1), LocalDate.of(2026, 10, 20), zone).map { it.id })
        assertTrue(rows.all { it.owner == "user" })
    }
    @Test fun `no retroactive doses when registering later today`() {
        val rows = Schedule.occurrences(med(created = "2026-10-06T15:00:00"), LocalDate.of(2026, 10, 6), zone)
        assertEquals(1, rows.size)
        assertEquals("16:35", Instant.ofEpochMilli(rows.single().scheduledAt).atZone(zone).toLocalTime().toString())
    }
    @Test fun `inactive medication creates no doses`() {
        assertTrue(Schedule.occurrences(med().copy(active = false), LocalDate.of(2026, 10, 10), zone).isEmpty())
    }
    @Test fun `empty or future schedule has no fabricated adherence`() {
        assertNull(Schedule.adherence(emptyList(), 100))
        assertNull(Schedule.adherence(listOf(Intake("1", "u", "m", 200)), 100))
    }
    @Test fun `adherence includes unresolved due doses but excludes future ones`() {
        val rows = listOf(Intake("1", "u", "m", 10, "TAKEN"), Intake("2", "u", "m", 20, "SKIPPED"),
            Intake("3", "u", "m", 30, "POSTPONED"), Intake("4", "u", "m", 40), Intake("5", "u", "m", 200))
        assertEquals(25, Schedule.adherence(rows, 100))
    }
}
