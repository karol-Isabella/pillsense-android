package com.pillsense.app.feature.adherence.data

import com.pillsense.app.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GeminiClient(private val okHttpClient: OkHttpClient) {

    suspend fun generateAdvice(anonData: String, apiKey: String, model: String): Result<GeminiResponse> {
        val maxRetries = 2
        var lastError: Exception? = null

        for (attempt in 0..maxRetries) {
            try {
                if (attempt > 0) {
                    val delayMs = if (attempt == 1) 2000L else 5000L
                    kotlinx.coroutines.delay(delayMs)
                }

                val prompt = "Analiza brevemente los datos de adherencia (en español, sin cambiar dosis): $anonData"
                val bodyMap = """{"contents":[{"parts":[{"text":"$prompt"}]}]}"""
                val mediaType = "application/json".toMediaType()
                val body = bodyMap.toRequestBody(mediaType)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder().url(url).post(body).build()

                val response = okHttpClient.newCall(request).execute()
                return when (response.code) {
                    200 -> {
                        val respBody = response.body?.string() ?: "{}"
                        Result.success(GeminiResponse("Recomendaciones disponibles", emptyList(), emptyList()))
                    }
                    503, 429 -> {
                        lastError = IOException("API ${response.code}")
                        if (attempt < maxRetries) continue else Result.failure(lastError)
                    }
                    else -> Result.failure(IOException("Status ${response.code}"))
                }
            } catch (e: Exception) {
                lastError = e
                if (attempt >= maxRetries) return Result.failure(e)
            }
        }
        return Result.failure(lastError ?: IOException("Error desconocido"))
    }
}
