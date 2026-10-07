package com.pillsense.app.core.ai

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación de ImageAnalyzer usando ML Kit Text Recognition local (sin API).
 * Bundled on-device text model: no API key, photo upload o model download requerido.
 */
@Singleton
class LocalImageAnalyzer @Inject constructor() : ImageAnalyzer {
    override suspend fun analyzeImage(imageBitmapBytes: ByteArray): MedicationExtractResult = withContext(Dispatchers.IO) {
        try {
            require(imageBitmapBytes.size <= 20 * 1024 * 1024) { "La imagen supera los 20 MB." }
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(imageBitmapBytes, 0, imageBitmapBytes.size, options)
            require(options.outWidth > 0 && options.outHeight > 0) { "No se pudo leer la foto." }
            options.inJustDecodeBounds = false; options.inSampleSize = 1
            while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > 2048) options.inSampleSize *= 2
            val bitmap = BitmapFactory.decodeByteArray(imageBitmapBytes, 0, imageBitmapBytes.size, options)
                ?: error("No se pudo leer la foto.")
            val rotation = runCatching {
                ExifInterface(ByteArrayInputStream(imageBitmapBytes)).rotationDegrees
            }.getOrDefault(0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val recognized = suspendCancellableCoroutine<String> { continuation ->
                recognizer.process(InputImage.fromBitmap(bitmap, rotation))
                    .addOnSuccessListener { if (continuation.isActive) continuation.resume(it.text) }
                    .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
                    .addOnCompleteListener { bitmap.recycle(); recognizer.close() }
            }
            MedicationTextParser.parse(recognized)
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { MedicationExtractResult(error = "No fue posible leer la foto. Usa una imagen clara o registra los datos manualmente.") }
    }
}
