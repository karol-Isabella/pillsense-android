package com.pillsense.app.feature.adherence.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillsense.app.feature.adherence.data.ConsentStorage
import com.pillsense.app.feature.adherence.data.GeminiClient
import com.pillsense.app.feature.adherence.data.GeminiResponse
import com.pillsense.app.feature.adherence.domain.validateGeminiResponse
import com.pillsense.app.feature.adherence.domain.AnonymousAdherence
import com.pillsense.app.core.database.Intake as DbIntake
import com.pillsense.app.core.database.Medication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId

data class InsightsUiState(
    val geminiResponse: GeminiResponse? = null,
    val isAiEnabled: Boolean = false,
    val source: String = "Reglas locales",
    val lastAdviceDate: String? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val consentStorage: ConsentStorage,
    private val geminiClient: GeminiClient
) : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState

    init {
        _uiState.value = _uiState.value.copy(isAiEnabled = consentStorage.hasConsent())
    }

    fun loadGeminiAdvice(intakes: List<DbIntake>, medications: List<Medication>, apiKey: String, model: String) {
        if (!_uiState.value.isAiEnabled || apiKey.isBlank()) return
        
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val zone = ZoneId.systemDefault()
                
                // Build anonymous stats
                val anonLabel = AnonymousAdherence.buildAnonymousStats(intakes, medications, zone)
                
                val result = geminiClient.generateAdvice(anonLabel, apiKey, model)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null) {
                        // Filter: discard if summary contains forbidden keywords
                        if (AnonymousAdherence.containsForbiddenKeyword(response.summary)) {
                            return@launch  // Silent fall back to local
                        }
                        
                        val medicationLabels = medications.associate { it.id to "Medicamento ${medications.indexOf(it) + 1}" }.values.toSet()
                        val validated = validateGeminiResponse(response, medicationLabels)
                        
                        // Filter patterns for forbidden keywords
                        val cleanPatterns = validated.patterns.filterNot { AnonymousAdherence.containsForbiddenKeyword(it) }
                        val cleanRecs = validated.recommendations.filterNot { AnonymousAdherence.containsForbiddenKeyword(it.reason) }
                        
                        if (cleanRecs.isNotEmpty() || cleanPatterns.isNotEmpty()) {
                            val cleanedResponse = response.copy(
                                patterns = cleanPatterns,
                                recommendations = cleanRecs
                            )
                            _uiState.value = _uiState.value.copy(
                                geminiResponse = cleanedResponse,
                                source = "IA"
                            )
                            consentStorage.setLastAdviceText(cleanedResponse.summary)
                            consentStorage.setLastAdviceDate(LocalDate.now().toString())
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent fail: keep local recommendations
            }
        }
    }

    fun setConsentGranted(granted: Boolean) {
        consentStorage.setConsent(granted)
        _uiState.value = _uiState.value.copy(isAiEnabled = granted)
    }
}
