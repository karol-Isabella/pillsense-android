package com.pillsense.app.feature.adherence.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GeminiClient(private val okHttpClient: OkHttpClient) {

    private val system = """Eres un asistente de adherencia a la medicación. Responde SOLO en JSON válido con dos campos: "resumen" (máximo 3 frases) y "recomendaciones" (array de máximo 2 strings).
Tono según adherencia: 80%+ reconocimiento positivo; 50-79% ánimo y una sugerencia; <50% comprensivo, sugerir consulta con profesional de salud.
Solo JSON. Solo cambios de horario de recordatorios, no cambios de dosis ni suspensión. No menciones olvidos si omitidas=0. Usa el idioma solicitado. Temperature 0.3."""

    suspend fun generateAdvice(anonData: String, apiKey: String, primaryModel: String, fallbackModel: String): Result<GeminiResponse> =
        withContext(Dispatchers.IO) {
            try {
                val result = callGemini(anonData, apiKey, primaryModel)
                if (result.isSuccess) return@withContext result
                val error = result.exceptionOrNull()
                if (error is GeminiRateLimitError) {
                    android.util.Log.w("PillSenseAI", "429 en $primaryModel, intentando fallback $fallbackModel")
                    return@withContext callGemini(anonData, apiKey, fallbackModel)
                }
                return@withContext result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun callGemini(anonData: String, apiKey: String, model: String): Result<GeminiResponse> {
        return try {
            val json = buildJsonObject {
                putJsonObject("systemInstruction") {
                    putJsonArray("parts") { addJsonObject { put("text", system) } }
                }
                putJsonArray("contents") {
                    addJsonObject {
                        putJsonArray("parts") {
                            addJsonObject { put("text", "Datos anónimos de los últimos 7 días: $anonData") }
                        }
                    }
                }
            }.toString()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                .header("x-goog-api-key", apiKey.trim())
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()
            val response = okHttpClient.newCall(request).execute()
            val code = response.code
            val respBody = response.body?.string() ?: ""
            response.close()

            when {
                code == 200 -> {
                    val text = Json.parseToJsonElement(respBody).jsonObject["candidates"]
                        ?.jsonArray?.firstOrNull()?.jsonObject?.get("content")
                        ?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
                        ?.jsonObject?.get("text")?.jsonPrimitive?.content
                    if (text.isNullOrBlank()) {
                        Result.failure(IOException("Respuesta vacía"))
                    } else {
                        android.util.Log.w("PillSenseAI", "Llamada real a $model: 200")
                        Result.success(GeminiResponse(text.trim(), emptyList(), emptyList()))
                    }
                }
                code == 429 -> {
                    android.util.Log.w("PillSenseAI", "429 en $model: límite diario alcanzado")
                    Result.failure(GeminiRateLimitError("API 429 en $model"))
                }
                else -> {
                    android.util.Log.w("PillSenseAI", "Error en $model: código $code, respuesta: $respBody")
                    Result.failure(IOException("Status $code"))
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("PillSenseAI", "Excepción en $model: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
}

class GeminiRateLimitError(message: String) : Exception(message)