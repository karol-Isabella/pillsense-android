package com.pillsense.app.feature.adherence.ui

import androidx.lifecycle.ViewModel
import com.pillsense.app.feature.adherence.data.ConsentStorage
import com.pillsense.app.feature.adherence.data.GeminiClient
import com.pillsense.app.feature.adherence.data.GeminiResponse
import com.pillsense.app.feature.adherence.domain.validateGeminiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class InsightsUiState(
    val geminiResponse: GeminiResponse? = null,
    val isAiEnabled: Boolean = false,
    val source: String = "Reglas locales",
    val lastAdviceDate: String? = null
)

class InsightsViewModel(
    private val consentStorage: ConsentStorage,
    private val geminiClient: GeminiClient
) : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState

    init {
        _uiState.value = _uiState.value.copy(isAiEnabled = consentStorage.hasConsent())
    }

    fun loadGeminiAdvice(anonData: String, apiKey: String, model: String) {
        if (!_uiState.value.isAiEnabled) return
        // TODO: Call Gemini
    }

    fun setConsentGranted(granted: Boolean) {
        consentStorage.setConsent(granted)
        _uiState.value = _uiState.value.copy(isAiEnabled = granted)
    }
}
