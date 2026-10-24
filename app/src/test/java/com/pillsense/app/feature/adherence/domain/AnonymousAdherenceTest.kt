package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import com.pillsense.app.core.database.Intake
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        
        // Parse JSON to verify structure
        val json = Json.parseToJsonElement(stats).jsonObject
        
        // Should not contain real user, medication or email info
        assertFalse(stats.contains("user1"))
        assertFalse(stats.contains("med1"))
        assertFalse(stats.contains("med2"))
        assertFalse(stats.contains("med3"))
        
        // Should contain counts in JSON fields
        assertEquals(1, json["tomadas"]?.jsonPrimitive?.content?.toInt())
        assertEquals(1, json["pospuestas"]?.jsonPrimitive?.content?.toInt())
        assertEquals(1, json["omitidas"]?.jsonPrimitive?.content?.toInt())
    }

    @Test
    fun forbiddenKeywordDetectionWorks() {
        // Ejemplos que DEBEN bloquearse (patrones peligrosos)
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("aumentar la dosis"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("suspender el medicamento"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("duplica la dosis"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("deja de tomar"))
        assertTrue(AnonymousAdherence.containsForbiddenKeyword("reduce la cantidad"))
        
        // Ejemplos que NO deben bloquearse (vocabulario normal de adherencia)
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("Tomaste el 71% de tus dosis; la noche es tu punto débil"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("tomar con agua"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("adherencia mejorada"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("el recordatorio por la mañana funciona mejor"))
        
        // Derivación a profesionales: NO debe bloquearse
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("Consulta a tu médico o farmacéutico"))
        assertFalse(AnonymousAdherence.containsForbiddenKeyword("contacta a tu profesional de salud"))
    }

    @Test
    fun anonymousStatsDoesNotContainExactDates() {
        val today = LocalDate.now()
        val intakes = listOf(
            Intake("i1", "user1", "med1", today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), "TAKEN")
        )
        val stats = AnonymousAdherence.buildAnonymousStats(intakes, emptyList())
        
        // Should not contain exact date string
        assertFalse(stats.contains(today.toString()))
        // Should have valid JSON structure
        val json = Json.parseToJsonElement(stats).jsonObject
        assertNotNull(json["dias_con_datos"])
    }

    @Test
    fun emptyIntakesProducesValidStats() {
        val stats = AnonymousAdherence.buildAnonymousStats(emptyList(), emptyList())
        val json = Json.parseToJsonElement(stats).jsonObject
        
        assertEquals(0, json["tomadas"]?.jsonPrimitive?.content?.toInt())
        assertEquals(0, json["pospuestas"]?.jsonPrimitive?.content?.toInt())
        assertEquals(0, json["omitidas"]?.jsonPrimitive?.content?.toInt())
    }
}

