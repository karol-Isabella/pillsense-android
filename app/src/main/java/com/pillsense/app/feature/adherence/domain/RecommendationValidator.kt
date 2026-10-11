package com.pillsense.app.feature.adherence.domain

import com.pillsense.app.feature.adherence.data.GeminiRecommendation
import com.pillsense.app.feature.adherence.data.GeminiResponse

private val FORBIDDEN_KEYWORDS = listOf("dosis", "pastillas", "suspender", "dejar", "aumentar", "disminuir", "cantidad")
private val VALID_TYPES = setOf("MOVE_REMINDER", "ADD_REINFORCEMENT", "NOTIFY_CONTACT")

fun validateGeminiResponse(response: GeminiResponse, medicationLabels: Set<String>): GeminiResponse {
    val validRecommendations = response.recommendations
        .filter { validateRecommendation(it, medicationLabels) }
        .filter { !containsForbiddenContent(it.reason) }

    return response.copy(recommendations = validRecommendations)
}

private fun validateRecommendation(rec: GeminiRecommendation, validLabels: Set<String>): Boolean {
    if (!VALID_TYPES.contains(rec.type)) return false
    if (!validLabels.contains(rec.medicationLabel)) return false

    return when (rec.type) {
        "MOVE_REMINDER" -> {
            val param = rec.parameter?.toIntOrNull() ?: return false
            param in -120..120
        }
        "ADD_REINFORCEMENT" -> rec.parameter != null
        "NOTIFY_CONTACT" -> true
        else -> false
    }
}

private fun containsForbiddenContent(text: String): Boolean {
    return FORBIDDEN_KEYWORDS.any { text.lowercase().contains(it) }
}
