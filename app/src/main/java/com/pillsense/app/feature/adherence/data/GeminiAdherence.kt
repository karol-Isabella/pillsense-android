package com.pillsense.app.feature.adherence.data

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRecommendation(
    val type: String,
    val medicationLabel: String,
    val reason: String,
    val parameter: String? = null
)

@Serializable
data class GeminiResponse(
    val summary: String,
    val patterns: List<String>,
    val recommendations: List<GeminiRecommendation>
)

@Serializable
data class GeminiRequest(
    val weekPercentage: Float,
    val previousWeekPercentage: Float,
    val byTimeSlot: Map<String, Float>,
    val byMedication: Map<String, Float>,
    val patterns: List<String>
)
