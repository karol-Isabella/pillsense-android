package com.pillsense.app.feature.adherence.data

import org.junit.Test
import org.junit.Assert.*

class GeminiResponseValidatorTest {
    
    @Test
    fun validResponsePassesValidation() {
        val result = GeminiResponseValidator.validate("Vas bien.", emptyList(), adherencePct = 85, omitidas = 1, language = "es")
        assertTrue(result.isValid)
        assertEquals("Vas bien.", result.resumen)
    }
    
    @Test
    fun rejectIfOmitidasZeroButMentionsOlvidos() {
        val result = GeminiResponseValidator.validate("Olvidaste una toma.", emptyList(), adherencePct = 90, omitidas = 0, language = "es")
        assertFalse(result.isValid)
    }
    
    @Test
    fun rejectIfPercentageMismatch() {
        val result = GeminiResponseValidator.validate("Tienes 75% de adherencia.", emptyList(), adherencePct = 85, omitidas = 2, language = "es")
        assertFalse(result.isValid)
    }
    
    @Test
    fun acceptCorrectPercentage() {
        val result = GeminiResponseValidator.validate("Con tu 85% vas por buen camino.", listOf("Continúa"), adherencePct = 85, omitidas = 1, language = "es")
        assertTrue(result.isValid)
    }
    
    @Test
    fun initialLevelAcceptsEncouragement() {
        val result = GeminiResponseValidator.validate("Felicitaciones, cumpliste tu primera dosis. Sigue así.", emptyList(), adherencePct = 100, omitidas = 0, language = "es", levelTag = "inicial")
        assertTrue(result.isValid)
        assertEquals("Felicitaciones, cumpliste tu primera dosis. Sigue así.", result.resumen)
        assertTrue(result.recomendaciones.isEmpty())
    }
    
    @Test
    fun initialLevelRejectsIfMentionsOlvidos() {
        val result = GeminiResponseValidator.validate("Felicitaciones, pero olvidaste una dosis.", emptyList(), adherencePct = 50, omitidas = 1, language = "es", levelTag = "inicial")
        assertFalse(result.isValid)
    }
    
    @Test
    fun initialLevelAllowsMatchingPercentage() {
        val result = GeminiResponseValidator.validate("Felicitaciones, adherencia perfecta del 100%.", emptyList(), adherencePct = 100, omitidas = 0, language = "es", levelTag = "inicial")
        assertTrue(result.isValid)
    }
    
    @Test
    fun initialLevelRejectsIfMentionsPercentageMismatch() {
        val result = GeminiResponseValidator.validate("Felicitaciones, tienes 45% de adherencia.", emptyList(), adherencePct = 100, omitidas = 0, language = "es", levelTag = "inicial")
        assertFalse(result.isValid)
    }
    
    @Test
    fun acceptsTextWithNumbersWithoutPercent() {
        val result = GeminiResponseValidator.validate("En los últimos 7 días has ido bien.", emptyList(), adherencePct = 100, omitidas = 0, language = "es")
        assertTrue(result.isValid)
    }
    
    @Test
    fun rejectIfPercentageWithSymbol() {
        val result = GeminiResponseValidator.validate("Tienes 45% de adherencia.", emptyList(), adherencePct = 100, omitidas = 0, language = "es")
        assertFalse(result.isValid)
    }
    
    @Test
    fun parseJsonClean() {
        val parsed = GeminiResponseValidator.parseGeminiResponse("""{"resumen": "Vas bien.", "recomendaciones": ["Sigue así"]}""")
        assertNotNull(parsed)
        assertEquals("Vas bien.", parsed?.resumen)
        assertEquals(listOf("Sigue así"), parsed?.recomendaciones)
    }
    
    @Test
    fun parseJsonWithFences() {
        val parsed = GeminiResponseValidator.parseGeminiResponse("""```json
{"resumen": "Vas bien.", "recomendaciones": ["Sigue así"]}
```""")
        assertNotNull(parsed)
        assertEquals("Vas bien.", parsed?.resumen)
    }
    
    @Test
    fun parseInvalidJsonReturnsNull() {
        val parsed = GeminiResponseValidator.parseGeminiResponse("""not valid json""")
        assertNull(parsed)
    }
}
