package com.pillsense.app.feature.adherence.data

import kotlinx.serialization.json.*

object GeminiResponseValidator {
    data class ValidatedResponse(val isValid: Boolean, val resumen: String = "", val recomendaciones: List<String> = emptyList(), val rejectionReason: String? = null)
    data class ParsedResponse(val resumen: String, val recomendaciones: List<String>)
    
    fun parseGeminiResponse(rawText: String): ParsedResponse? {
        return try {
            var json = rawText.trim()
            if (json.startsWith("```json")) {
                json = json.removePrefix("```json").removeSuffix("```").trim()
            } else if (json.startsWith("```")) {
                json = json.removePrefix("```").removeSuffix("```").trim()
            }
            
            val jsonObj = Json.parseToJsonElement(json).jsonObject
            val resumen = jsonObj["resumen"]?.jsonPrimitive?.content?.trim() ?: return null
            val recs = jsonObj["recomendaciones"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
            ParsedResponse(resumen, recs)
        } catch (e: Exception) {
            null
        }
    }
    
    fun validate(resumen: String, recomendaciones: List<String>, adherencePct: Int, omitidas: Int, language: String, levelTag: String? = null): ValidatedResponse {
        return try {
            // Nivel inicial: no debe mencionar olvidos
            if (levelTag == "inicial") {
                if (resumen.contains("olvid", ignoreCase = true) || recomendaciones.any { it.contains("olvid", ignoreCase = true) }) {
                    return ValidatedResponse(false, rejectionReason = "mentions_olvidos_on_inicial")
                }
                // Permitir porcentaje si coincide con adherencePct, rechazar si es distinto
                val percentMatch = "\\d+\\s*%".toRegex()
                val percentInResponse = percentMatch.find(resumen)?.value?.replace(Regex("\\D"), "")?.toIntOrNull()
                if (percentInResponse != null && percentInResponse != adherencePct) {
                    return ValidatedResponse(false, rejectionReason = "percentage_mismatch: got $percentInResponse, expected $adherencePct")
                }
                return ValidatedResponse(true, resumen, emptyList())
            }
            
            // Nivel 2: reglas estándar
            if (omitidas == 0 && (resumen.contains("olvid", ignoreCase = true) || recomendaciones.any { it.contains("olvid", ignoreCase = true) })) {
                return ValidatedResponse(false, rejectionReason = "mentions_olvidos_with_omitidas_zero")
            }
            
            val percentMatch = "\\d+\\s*%".toRegex()
            val percentInResponse = percentMatch.find(resumen)?.value?.replace(Regex("\\D"), "")?.toIntOrNull()
            if (percentInResponse != null && percentInResponse != adherencePct) {
                return ValidatedResponse(false, rejectionReason = "percentage_mismatch: got $percentInResponse, expected $adherencePct")
            }
            
            ValidatedResponse(true, resumen, recomendaciones)
        } catch (e: Exception) {
            ValidatedResponse(false, rejectionReason = "validation_error: ${e.message}")
        }
    }
}
