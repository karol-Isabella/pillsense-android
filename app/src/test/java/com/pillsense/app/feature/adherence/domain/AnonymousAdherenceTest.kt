package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import com.pillsense.app.core.database.Intake
import java.time.LocalDate
import java.time.ZoneId

class AnonymousAdherenceTest {

    @Test
    fun buildsAnonymousStatsWithoutRealNames() {
        val intakes = listOf(
            Intake("i1", "user1", "med1", System.currentTimeMillis() - 1000, "TAKEN"),
            Intake("i2", "user1", "med2", System.currentTimeMillis() - 2000, "POSTPONED"),
            Intake("i3", "user1", "med3", System.currentTimeMillis() - 3000, "SKIPPED")
        )
        val stats = AnonymousAdherence.buildAnonymousStats(intakes, emptyList())
        
        // Should not contain real user, medication or email info
        assertFalse(stats.contains("user1"))
        assertFalse(stats.contains("med1"))
        assertFalse(stats.contains("med2"))
        assertFalse(stats.contains("med3"))
        
        // Should contain only counts
        assertTrue(stats.contains("1 tomada"))
        assertTrue(stats.contains("1 pospuesta"))
        assertTrue(stats.contains("1 omitida"))
    }

    @Test
    fun forbiddenKeywordDetectionWorks() {
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("aumentar la dosis"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("suspender el medicamento"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("cambiar las pastillas"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("disminuir cantidad"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("tomar con agua"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("adherencia mejorada"))
    }

    @Test
    fun anonymousStatsDoesNotContainExactDates() {
        val today = LocalDate.now()
        val intakes = listOf(
            Intake("i1", "user1", "med1", today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), "TAKEN")
        )
        val stats = AnonymousAdherence.buildAnonymousStats(intakes, emptyList())
        
        // Should use relative dates like "Día -0" not exact dates
        assertTrue(stats.contains("Día -0"))
        assertFalse(stats.contains(today.toString()))
    }

    @Test
    fun emptyIntakesProducesValidStats() {
        val stats = AnonymousAdherence.buildAnonymousStats(emptyList(), emptyList())
        assertTrue(stats.contains("0 tomadas"))
        assertTrue(stats.contains("0 pospuestas"))
        assertTrue(stats.contains("0 omitidas"))
    }
}
