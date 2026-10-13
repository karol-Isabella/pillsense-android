package com.pillsense.app.feature.adherence.ui

import org.junit.Test
import org.junit.Assert.*
import com.pillsense.app.core.database.Intake
import com.pillsense.app.core.database.Medication
import com.pillsense.app.feature.adherence.domain.AnonymousAdherence

class InsightsViewModelTest {

    @Test
    fun summaryWithForbiddenKeywordIsDiscarded() {
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("suspender el tratamiento"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("aumentar dosis a 500mg"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("cambiar horario de pastillas"))
    }

    @Test
    fun cleanSummaryPassesFilter() {
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("adherencia mejorada"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("buenos registros"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("considere el horario"))
    }

    @Test
    fun anonymousTextDoesNotContainRealNames() {
        val medications = listOf(
            Medication("med1", "user1", "Metformina", "500mg", "comprimido", 24, "08:00", "Con agua", "2026-01-01", null, 0L, true),
            Medication("med2", "user1", "Ibuprofeno", "200mg", "comprimido", 12, "10:00", "Con comida", "2026-01-01", null, 0L, true)
        )
        val intakes = listOf(
            Intake("i1", "user1", "med1", System.currentTimeMillis() - 1000, "TAKEN"),
            Intake("i2", "user1", "med2", System.currentTimeMillis() - 2000, "TAKEN")
        )
        
        val stats = AnonymousAdherence.buildAnonymousStats(intakes, medications)
        
        assertFalse(stats.contains("Metformina"))
        assertFalse(stats.contains("Ibuprofeno"))
        assertFalse(stats.contains("500mg"))
        assertFalse(stats.contains("200mg"))
        assertFalse(stats.contains("user1"))
        assertTrue(stats.contains("2 tomada"))
    }

    @Test
    fun noEmailOrContactInfoInStats() {
        val intakes = listOf(
            Intake("i1", "user@example.com", "med1", System.currentTimeMillis(), "TAKEN")
        )
        val stats = AnonymousAdherence.buildAnonymousStats(intakes, emptyList())
        
        assertFalse(stats.contains("@"))
        assertFalse(stats.contains("example.com"))
        assertFalse(stats.contains("user@example.com"))
    }
}
