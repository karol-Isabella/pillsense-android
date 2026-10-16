package com.pillsense.app.feature.adherence.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GeminiClient(private val okHttpClient: OkHttpClient) {

    private val system = "Eres un asistente de adherencia a la medicación. Responde en español, en un máximo de 3 frases cortas. " +
        "Interpreta los conteos y sugiere hábitos generales (por ejemplo, ligar el recordatorio a una rutina). " +
        "No menciones dosis, cantidades, ni cambios de tratamiento, y no uses las palabras suspender, dejar, aumentar ni disminuir. " +
        "Si hay olvidos, sugiere consultar al profesional de salud."

    suspend fun generateAdvice(anonData: String, apiKey: String, model: String): Result<GeminiResponse> =
        withContext(Dispatchers.IO) {
            var lastError: Exception? = null
            for (attempt in 0..2) {
                try {
                    if (attempt > 0) delay(if (attempt == 1) 2000L else 5000L)
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
                        .header("x-goog-api-key", apiKey)
                        .post(json.toRequestBody("application/json".toMediaType()))
                        .build()
                    val (code, respBody) = okHttpClient.newCall(request).execute()
                        .use { it.code to (it.body?.string() ?: "") }
                    if (code == 200) {
                        val text = Json.parseToJsonElement(respBody).jsonObject["candidates"]
                            ?.jsonArray?.firstOrNull()?.jsonObject?.get("content")
                            ?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
                            ?.jsonObject?.get("text")?.jsonPrimitive?.content
                        if (text.isNullOrBlank()) return@withContext Result.failure(IOException("Respuesta vacía"))
                        return@withContext Result.success(GeminiResponse(text.trim(), emptyList(), emptyList()))
                    }
                    if (code == 503 || code == 429) { lastError = IOException("API $code"); continue }
                    return@withContext Result.failure(IOException("Status $code"))
                } catch (e: Exception) {
                    lastError = e
                }
            }
            Result.failure(lastError ?: IOException("Error desconocido"))
        }
}