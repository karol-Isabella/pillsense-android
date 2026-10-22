package com.pillsense.app.feature.adherence.domain

import com.pillsense.app.core.database.Intake as DbIntake
import com.pillsense.app.core.database.Medication
import java.time.LocalDate
import java.time.ZoneId

object AnonymousAdherence {
    // Patrones de frases PELIGROSAS que deben bloquearse
    private val DANGEROUS_PATTERNS = listOf(
        // Cambios de dosis
        Regex("\\b(duplica|aumenta|dobla|eleva|reduce|reduce|disminuye)\\s+(la\\s+)?dosis\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(aumenta|reduce|cambia)\\s+(la\\s+)?cantidad\\b", RegexOption.IGNORE_CASE),
        // Suspender tratamiento
        Regex("\\b(suspende|deja|detén|para)\\s+(de\\s+)?tomar", RegexOption.IGNORE_CASE),
        Regex("\\b(suspender|abandonar|descontinuar)\\s+(el\\s+)?(medicamento|tratamiento|pastilla)", RegexOption.IGNORE_CASE),
        // Indicaciones tras olvidar
        Regex("\\bsi\\s+olvida.*qué\\s+(hacer|hago)", RegexOption.IGNORE_CASE),
        // Diagnósticos directos
        Regex("\\btienes?\\s+(una\\s+)?(infección|enfermedad|alergia|reacción)\\b", RegexOption.IGNORE_CASE),
    )

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
        // Permitir: derivación a profesionales de salud
        val derivationPattern = Regex("\\b(consulta|habla|contacta)\\s+(a\\s+tu\\s+)?(médico|farmacéutico|profesional|doctor)\\b", RegexOption.IGNORE_CASE)
        if (derivationPattern.containsMatchIn(text)) {
            android.util.Log.d("PillSenseAI", "Respuesta permitida: contiene derivación a profesional")
            return false
        }
        
        // Bloquear si coincide algún patrón peligroso
        for (pattern in DANGEROUS_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                android.util.Log.w("PillSenseAI", "Respuesta bloqueada: patrón peligroso '${match.value}' encontrado, longitud=${text.length}")
                return true
            }
        }
        
        android.util.Log.d("PillSenseAI", "Respuesta permitida: longitud=${text.length}")
        return false
    }
}
