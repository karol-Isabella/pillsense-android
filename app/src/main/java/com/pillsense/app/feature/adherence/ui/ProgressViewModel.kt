package com.pillsense.app.feature.adherence.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

data class ProgressUiState(
    val weekPercentage: Float = 0f,
    val trend: Float = 0f,
    val summary: String = "",
    val recommendations: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val source: String = "Reglas locales"
)

class ProgressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState

    fun loadProgress() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        // TODO: Cargar datos reales
        _uiState.value = ProgressUiState(
            weekPercentage = 85f,
            trend = 5f,
            summary = "Adherencia estable",
            recommendations = listOf("Considerar refuerzo en la tarde"),
            source = "Reglas locales"
        )
    }

    fun applyRecommendation(index: Int) {
        // TODO: Aplicar recomendación
    }
}
