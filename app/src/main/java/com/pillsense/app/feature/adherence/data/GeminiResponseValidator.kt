package com.pillsense.app.feature.adherence.data

import kotlinx.serialization.json.*

object GeminiResponseValidator {
    data class ValidatedResponse(val isValid: Boolean, val resumen: String = "", val recomendaciones: List<String> = emptyList())
    
    fun validate(rawText: String, adherencePct: Int, omitidas: Int, language: String, levelTag: String? = null): ValidatedResponse {
        return try {
            val json = Json.parseToJsonElement(rawText).jsonObject
            val resumen = json["resumen"]?.jsonPrimitive?.content?.trim() ?: return ValidatedResponse(false)
            val recs = json["recomendaciones"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            
            // Nivel inicial: no debe mencionar olvidos ni porcentajes
            if (levelTag == "inicial") {
                if (resumen.contains("olvid", ignoreCase = true) || recs.any { it.contains("olvid", ignoreCase = true) }) {
                    return ValidatedResponse(false)
                }
                val percentMatch = "\\b(\\d+)%".toRegex()
                if (percentMatch.find(resumen) != null) {
                    return ValidatedResponse(false)
                }
                return ValidatedResponse(true, resumen, emptyList())
            }
            
            // Nivel 2: reglas estándar
            // Descartar si menciona olvidos con omitidas = 0
            if (omitidas == 0 && (resumen.contains("olvid", ignoreCase = true) || recs.any { it.contains("olvid", ignoreCase = true) })) {
                return ValidatedResponse(false)
            }
            
            // Descartar si cita un porcentaje distinto
            val percentMatch = "\\b(\\d+)%".toRegex()
            val percentInResponse = percentMatch.find(resumen)?.groups?.get(1)?.value?.toIntOrNull()
            if (percentInResponse != null && percentInResponse != adherencePct) {
                return ValidatedResponse(false)
            }
            
            ValidatedResponse(true, resumen, recs)
        } catch (e: Exception) {
            ValidatedResponse(false)
        }
    }
}
