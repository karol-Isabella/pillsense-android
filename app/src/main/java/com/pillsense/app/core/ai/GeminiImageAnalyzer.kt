package com.pillsense.app.core.ai

import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

data class MedicationExtractResult(
    val nombre: String,
    val dosis: String,
    val frecuencia: String,
    val indicaciones: String,
    val esExitoso: Boolean,
    val error: String? = null
)

interface ImageAnalyzer {
    suspend fun analyzeImage(imageBitmapBytes: ByteArray): MedicationExtractResult
}

class GeminiImageAnalyzer(
    private val apiKey: String? = null
) : ImageAnalyzer {

    override suspend fun analyzeImage(imageBitmapBytes: ByteArray): MedicationExtractResult {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isNullOrBlank()) {
                    Timber.w("GeminiImageAnalyzer: sin API key, devolviendo mock")
                    return@withContext mockResult()
                }
                // TODO: integración real con com.google.ai.client.generativeai
                // val model = GenerativeModel("gemini-1.5-flash", apiKey)
                // val prompt = "Analiza este empaque/receta médica y extrae: nombre del medicamento, dosis, frecuencia de toma e indicaciones. Responde en JSON con campos: nombre, dosis, frecuencia, indicaciones."
                // val response = model.generateContent(content { image(BitmapFactory.decodeByteArray(imageBitmapBytes, 0, imageBitmapBytes.size)); text(prompt) })
                // parseGeminiResponse(response.text ?: "")
                mockResult()
            } catch (e: Exception) {
                Timber.e(e, "GeminiImageAnalyzer: error analizando imagen")
                MedicationExtractResult(
                    nombre = "",
                    dosis = "",
                    frecuencia = "",
                    indicaciones = "",
                    esExitoso = false,
                    error = e.message
                )
            }
        }
    }

    private fun mockResult() = MedicationExtractResult(
        nombre = "Ibuprofeno 400mg",
        dosis = "400mg",
        frecuencia = "Cada 8 horas",
        indicaciones = "Tomar con alimentos. No exceder 1200mg/día.",
        esExitoso = true
    )
}
