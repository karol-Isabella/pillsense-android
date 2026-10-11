package com.pillsense.app.feature.adherence.domain

import kotlinx.serialization.Serializable

@Serializable
data class AnonymizedReport(
    val weekPercentage: Float,
    val previousWeekPercentage: Float,
    val trend: Float,
    val byTimeSlot: Map<String, Float>,
    val byDayOfWeek: Map<String, Float>,
    val byMedication: Map<String, Float>,
    val patterns: List<AnonymizedPattern>
)

@Serializable
data class AnonymizedPattern(
    val medicationLabel: String,
    val type: String,
    val count: Int,
    val timeSlot: String? = null
)

fun anonymize(
    report: WeeklyAdherenceReport,
    patterns: List<ForgottenPattern>,
    medicationNames: Map<String, String>
): AnonymizedReport {
    val medicationMap = mutableMapOf<String, String>()
    var idx = 0
    medicationNames.keys.forEach { id ->
        medicationMap[id] = "Medicamento ${idx + 1}"
        idx++
    }

    val anonByMedication = report.byMedication.mapKeys { entry ->
        medicationMap[entry.key] ?: entry.key
    }

    val anonByDayOfWeek = report.byDayOfWeek.mapKeys { entry ->
        entry.key.name
    }

    val anonPatterns = patterns.map { pattern ->
        val medLabel = medicationMap[pattern.medicationId] ?: "Medicamento"
        AnonymizedPattern(
            medicationLabel = medLabel,
            type = pattern.type,
            count = pattern.count,
            timeSlot = pattern.timeSlot
        )
    }

    return AnonymizedReport(
        weekPercentage = report.weekPercentage,
        previousWeekPercentage = report.previousWeekPercentage,
        trend = report.trend,
        byTimeSlot = report.byTimeSlot,
        byDayOfWeek = anonByDayOfWeek,
        byMedication = anonByMedication,
        patterns = anonPatterns
    )
}

fun deanonymize(
    anonReport: AnonymizedReport,
    medicationNames: Map<String, String>
): Map<String, String> {
    val reverseMap = mutableMapOf<String, String>()
    var idx = 0
    medicationNames.values.forEach { name ->
        val label = "Medicamento " + (idx + 1).toString()
        reverseMap[label] = name
        idx++
    }
    return reverseMap
}
