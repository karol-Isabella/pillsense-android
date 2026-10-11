package com.pillsense.app.feature.adherence.domain

import org.junit.Test
import org.junit.Assert.*
import com.pillsense.app.feature.adherence.data.GeminiRecommendation
import com.pillsense.app.feature.adherence.data.GeminiResponse

class RecommendationValidatorTest {

    @Test
    fun validRecommendationPasses() {
        val rec = GeminiRecommendation("MOVE_REMINDER", "Medicamento 1", "cambiar horario", "30")
        val resp = GeminiResponse("test", emptyList(), listOf(rec))
        val validated = validateGeminiResponse(resp, setOf("Medicamento 1"))
        assertEquals(1, validated.recommendations.size)
    }

    @Test
    fun invalidTypeDiscarded() {
        val rec = GeminiRecommendation("UNKNOWN_TYPE", "Medicamento 1", "cambiar", "30")
        val resp = GeminiResponse("test", emptyList(), listOf(rec))
        val validated = validateGeminiResponse(resp, setOf("Medicamento 1"))
        assertTrue(validated.recommendations.isEmpty())
    }

    @Test
    fun forbiddenKeywordDiscarded() {
        val rec = GeminiRecommendation("MOVE_REMINDER", "Medicamento 1", "aumentar dosis", "30")
        val resp = GeminiResponse("test", emptyList(), listOf(rec))
        val validated = validateGeminiResponse(resp, setOf("Medicamento 1"))
        assertTrue(validated.recommendations.isEmpty())
    }

    @Test
    fun parameterOutOfRangeDiscarded() {
        val rec = GeminiRecommendation("MOVE_REMINDER", "Medicamento 1", "cambiar", "200")
        val resp = GeminiResponse("test", emptyList(), listOf(rec))
        val validated = validateGeminiResponse(resp, setOf("Medicamento 1"))
        assertTrue(validated.recommendations.isEmpty())
    }

    @Test
    fun unknownMedicationLabelDiscarded() {
        val rec = GeminiRecommendation("MOVE_REMINDER", "Medicamento 99", "cambiar", "30")
        val resp = GeminiResponse("test", emptyList(), listOf(rec))
        val validated = validateGeminiResponse(resp, setOf("Medicamento 1"))
        assertTrue(validated.recommendations.isEmpty())
    }
}
