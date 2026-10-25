package com.pillsense.app.feature.adherence.data

import org.junit.Test
import org.junit.Assert.*

class GeminiResponseValidatorTest {
    
    @Test
    fun validResponsePassesValidation() {
        val json = """{"resumen": "Vas bien.", "recomendaciones": ["Sigue así"]}"""
        val result = GeminiResponseValidator.validate(json, adherencePct = 85, omitidas = 1, language = "es")
        assertTrue(result.isValid)
        assertEquals("Vas bien.", result.resumen)
    }
    
    @Test
    fun rejectIfOmitidasZeroButMentionsOlvidos() {
        val json = """{"resumen": "Olvidaste una toma.", "recomendaciones": []}"""
        val result = GeminiResponseValidator.validate(json, adherencePct = 90, omitidas = 0, language = "es")
        assertFalse(result.isValid)
    }
    
    @Test
    fun rejectIfPercentageMismatch() {
        val json = """{"resumen": "Tienes 75% de adherencia.", "recomendaciones": []}"""
        val result = GeminiResponseValidator.validate(json, adherencePct = 85, omitidas = 2, language = "es")
        assertFalse(result.isValid)
    }
    
    @Test
    fun acceptCorrectPercentage() {
        val json = """{"resumen": "Con tu 85% vas por buen camino.", "recomendaciones": ["Continúa"]}"""
        val result = GeminiResponseValidator.validate(json, adherencePct = 85, omitidas = 1, language = "es")
        assertTrue(result.isValid)
    }
    
    @Test
    fun initialLevelAcceptsEncouragement() {
        val json = """{"resumen": "Felicitaciones, cumpliste tu primera dosis. Sigue así.", "recomendaciones": []}"""
        val result = GeminiResponseValidator.validate(json, adherencePct = 100, omitidas = 0, language = "es", levelTag = "inicial")
        assertTrue(result.isValid)
        assertEquals("Felicitaciones, cumpliste tu primera dosis. Sigue así.", result.resumen)
        assertTrue(result.recomendaciones.isEmpty())
    }
    
    @Test
    fun initialLevelRejectsIfMentionsOlvidos() {
        val json = """{"resumen": "Felicitaciones, pero olvidaste una dosis.", "recomendaciones": []}"""
        val result = GeminiResponseValidator.validate(json, adherencePct = 50, omitidas = 1, language = "es", levelTag = "inicial")
        assertFalse(result.isValid)
    }
    
    @Test
    fun initialLevelRejectsIfMentionsPercentage() {
        val json = """{"resumen": "Felicitaciones, tienes 100% de adherencia.", "recomendaciones": []}"""
        val result = GeminiResponseValidator.validate(json, adherencePct = 100, omitidas = 0, language = "es", levelTag = "inicial")
        assertFalse(result.isValid)
    }
}
