package com.pillsense.app.feature.medication.ui.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillsense.app.core.ai.ImageAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ScanState(val busy: Boolean = false, val result: ScannedMedication? = null, val error: String? = null)
@HiltViewModel
class ScanViewModel @Inject constructor(@ApplicationContext private val context: Context, private val analyzer: ImageAnalyzer) : ViewModel() {
    private val mutable = MutableStateFlow(ScanState())
    val state = mutable.asStateFlow()
    private val captures = mutableSetOf<Uri>()
    fun trackCapture(uri: Uri) { captures += uri }
    fun reset() { if (!mutable.value.busy) mutable.value = ScanState() }
    override fun onCleared() {
        captures.forEach { uri -> runCatching { context.contentResolver.delete(uri, null, null) } }
        super.onCleared()
    }
    fun analyze(uri: Uri) {
        if (mutable.value.busy) return
        mutable.value = ScanState(busy = true)
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var count = stream.read(buffer)
                        while (count != -1) {
                            require(output.size() + count <= 20 * 1024 * 1024) { "La imagen supera los 20 MB." }
                            output.write(buffer, 0, count); count = stream.read(buffer)
                        }
                        output.toByteArray()
                    } ?: error("No se pudo abrir la foto.")
                }
                val result = analyzer.analyzeImage(bytes)
                mutable.value = if (result.esExitoso) ScanState(result = ScannedMedication(
                    result.nombre, result.dosis, result.forma, result.frecuencia,
                    result.duracionDias?.toString().orEmpty(), result.indicaciones, result.textoOriginal,
                ), error = result.error) else ScanState(error = result.error ?: "No se encontró un medicamento legible.")
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.value = ScanState(error = e.message ?: "No fue posible abrir la imagen.") }
        }
    }
}
