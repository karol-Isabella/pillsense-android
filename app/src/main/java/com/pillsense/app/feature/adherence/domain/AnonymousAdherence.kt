package com.pillsense.app.feature.adherence.domain

import com.pillsense.app.core.database.Intake as DbIntake
import com.pillsense.app.core.database.Medication
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.ZoneId

object AnonymousAdherence {
    // Patrones de frases PELIGROSAS que deben bloquearse
    private val DANGEROUS_PATTERNS = listOf(
        Regex("\\b(duplica|aumenta|dobla|eleva|reduce|reduce|disminuye|aumentar|elevar|duplicar|doblar|reducir|disminuir)\\s+(la\\s+)?dosis\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(aumenta|reduce|cambia|aumentar|reducir|cambiar)\\s+(la\\s+)?(cantidad|horario|hora|frecuencia|dosis|intervalo)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(suspende|deja|detén|para|suspender|dejar|detener)\\s+(de\\s+)?tomar", RegexOption.IGNORE_CASE),
        Regex("\\b(suspender|abandonar|descontinuar)\\s+(el\\s+)?(medicamento|tratamiento|pastilla)", RegexOption.IGNORE_CASE),
        Regex("\\bsi\\s+olvida.*qué\\s+(hacer|hago)", RegexOption.IGNORE_CASE),
        Regex("\\btienes?\\s+(una\\s+)?(infección|enfermedad|alergia|reacción)\\b", RegexOption.IGNORE_CASE),
    )

    fun buildAnonymousStats(
        intakes: List<DbIntake>,
        medications: List<Medication>,
        zone: ZoneId = ZoneId.systemDefault(),
        language: String = "es",
        levelTag: String? = null
    ): String {
        val today = LocalDate.now()
        val taken = intakes.count { it.status == "TAKEN" }
        val postponed = intakes.count { it.status == "POSTPONED" }
        val skipped = intakes.count { it.status == "SKIPPED" }
        val total = taken + postponed + skipped
        val adherence = if (total > 0) (taken * 100) / total else 0
        
        var daysWithData = 0
        (0..6).forEach { offset ->
            val dayStart = today.minusDays(offset.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = today.minusDays(offset.toLong()).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            if (intakes.any { it.scheduledAt in dayStart until dayEnd }) daysWithData++
        }
        
        val medLabels = medications.mapIndexed { idx, _ -> "Medicamento ${idx + 1}" }.associateBy { it }
        
        return buildJsonObject {
            put("programadas", total)
            put("tomadas", taken)
            put("omitidas", skipped)
            put("pospuestas", postponed)
            put("adherencia_pct", adherence)
            put("adherencia_semana_anterior_pct", null as? Number)
            put("dias_con_datos", daysWithData)
            put("franja_mas_debil", null as? String)
            put("retraso_promedio_min", 0)
            put("patrones", JsonArray(emptyList()))
            put("idioma", language)
            if (levelTag != null) {
                put("nivel", levelTag)
            }
            put("medicamentos", Json.encodeToJsonElement(medLabels))
        }.toString()
    }

    fun containsForbiddenKeyword(text: String): Boolean {
        val derivationPattern = Regex("\\b(consulta|habla|contacta)\\s+(a\\s+tu\\s+)?(médico|farmacéutico|profesional|doctor)\\b", RegexOption.IGNORE_CASE)
        if (derivationPattern.containsMatchIn(text)) {
            android.util.Log.d("PillSenseAI", "Respuesta permitida: contiene derivación a profesional")
            return false
        }
        
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
