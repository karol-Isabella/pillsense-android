package com.pillsense.app.core.ai

/**
 * Resultado unificado del análisis de imagen de medicamento.
 * Contiene todos los campos esperados por LocalImageAnalyzer y MedicationTextParser.
 */
data class MedicationExtractResult(
    val nombre: String = "",
    val dosis: String = "",
    val frecuencia: String = "",
    val indicaciones: String = "",
    val forma: String = "",
    val duracionDias: Int? = null,
    val textoOriginal: String = "",
    val esExitoso: Boolean = false,
    val error: String? = null
)

/**
 * Interfaz común para analizadores de imágenes.
 * Implementaciones: GeminiImageAnalyzer (IA), LocalImageAnalyzer (OCR local).
 */
interface ImageAnalyzer {
    suspend fun analyzeImage(imageBitmapBytes: ByteArray): MedicationExtractResult
}
