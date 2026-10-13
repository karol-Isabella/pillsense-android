package com.pillsense.app.feature.adherence.domain

import com.pillsense.app.core.database.Intake as DbIntake
import com.pillsense.app.core.database.Medication
import java.time.LocalDate
import java.time.ZoneId

object AnonymousAdherence {
    private val FORBIDDEN_KEYWORDS = listOf("dosis", "pastillas", "suspender", "dejar", "aumentar", "disminuir", "cambiar")

    fun buildAnonymousStats(
        intakes: List<DbIntake>,
        medications: List<Medication>,
        zone: ZoneId = ZoneId.systemDefault()
    ): String {
        val today = LocalDate.now()
        return buildString {
            val taken = intakes.count { it.status == "TAKEN" }
            val postponed = intakes.count { it.status == "POSTPONED" }
            val skipped = intakes.count { it.status == "SKIPPED" }
            append("Estadísticas últimos 7 días: ")
            append("$taken tomadas, $postponed pospuestas, $skipped omitidas. ")
            (0..6).reversed().forEach { offset ->
                val dayIntakes = intakes.filter { 
                    val dayStart = today.minusDays(offset.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
                    val dayEnd = today.minusDays(offset.toLong()).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    it.scheduledAt in dayStart until dayEnd
                }
                val dayAdhered = dayIntakes.count { it.status == "TAKEN" }
                val dayTotal = dayIntakes.size
                if (dayTotal > 0) append("Día -$offset: $dayAdhered/$dayTotal. ")
            }
        }
    }

    fun containsForbiddenKeyword(text: String): Boolean {
        return FORBIDDEN_KEYWORDS.any { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
    }
}
