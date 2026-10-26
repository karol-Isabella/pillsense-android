package com.pillsense.app.feature.adherence.data

import kotlinx.serialization.json.*

object GeminiResponseValidator {
    data class ValidatedResponse(val isValid: Boolean, val resumen: String = "", val recomendaciones: List<String> = emptyList(), val rejectionReason: String? = null)
    
    fun validate(rawText: String, adherencePct: Int, omitidas: Int, language: String, levelTag: String? = null): ValidatedResponse {
        return try {
            val json = Json.parseToJsonElement(rawText).jsonObject
            val resumen = json["resumen"]?.jsonPrimitive?.content?.trim() ?: return ValidatedResponse(false, rejectionReason = "empty_summary")
            val recs = json["recomendaciones"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            
            // Nivel inicial: no debe mencionar olvidos ni porcentajes
            if (levelTag == "inicial") {
                if (resumen.contains("olvid", ignoreCase = true) || recs.any { it.contains("olvid", ignoreCase = true) }) {
                    return ValidatedResponse(false, rejectionReason = "mentions_olvidos_on_inicial")
                }
                val percentMatch = "\\d+\\s*%".toRegex()
                if (percentMatch.find(resumen) != null) {
                    return ValidatedResponse(false, rejectionReason = "mentions_percentage_on_inicial")
                }
                return ValidatedResponse(true, resumen, emptyList())
            }
            
            // Nivel 2: reglas estándar
            // Descartar si menciona olvidos con omitidas = 0
            if (omitidas == 0 && (resumen.contains("olvid", ignoreCase = true) || recs.any { it.contains("olvid", ignoreCase = true) })) {
                return ValidatedResponse(false, rejectionReason = "mentions_olvidos_with_omitidas_zero")
            }
            
            // Descartar si cita un porcentaje (CON %) distinto
            val percentMatch = "\\d+\\s*%".toRegex()
            val percentInResponse = percentMatch.find(resumen)?.value?.replace(Regex("\\D"), "")?.toIntOrNull()
            if (percentInResponse != null && percentInResponse != adherencePct) {
                return ValidatedResponse(false, rejectionReason = "percentage_mismatch: got $percentInResponse, expected $adherencePct")
            }
            
            ValidatedResponse(true, resumen, recs)
        } catch (e: Exception) {
            ValidatedResponse(false, rejectionReason = "json_parse_error: ${e.message}")
        }
    }
}
