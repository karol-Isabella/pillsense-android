package com.pillsense.app.feature.adherence.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillsense.app.feature.adherence.data.ConsentStorage
import com.pillsense.app.feature.adherence.data.GeminiClient
import com.pillsense.app.feature.adherence.data.GeminiResponse
import com.pillsense.app.feature.adherence.domain.validateGeminiResponse
import com.pillsense.app.core.database.Intake as DbIntake
import com.pillsense.app.core.database.Medication
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
                val today = LocalDate.now()
                
                // Anonymize: send only counts, no names, doses, emails or exact dates
                val anonLabel = buildString {
                    val taken = intakes.count { it.status == "TAKEN" }
                    val postponed = intakes.count { it.status == "POSTPONED" }
                    val skipped = intakes.count { it.status == "SKIPPED" }
                    append("Estadísticas últimos 7 días: ")
                    append("$taken tomadas, $postponed pospuestas, $skipped omitidas. ")
                    (0..6).reversed().forEach { offset ->
                        val dayIntakes = intakes.filter { 
                            val dayStart = today.minusDays(offset.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()
                            val dayEnd = today.minusDays(offset.toLong()).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                            it.scheduledAt in dayStart until dayEnd
                        }
                        val dayAdhered = dayIntakes.count { it.status == "TAKEN" }
                        val dayTotal = dayIntakes.size
                        if (dayTotal > 0) append("Día -$offset: $dayAdhered/$dayTotal. ")
                    }
                }
                
                val result = geminiClient.generateAdvice(anonLabel, apiKey, model)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null) {
                        val medicationLabels = medications.associate { it.id to "Medicamento ${medications.indexOf(it) + 1}" }.values.toSet()
                        val validated = validateGeminiResponse(response, medicationLabels)
                        if (validated.recommendations.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                geminiResponse = validated,
                                source = "IA"
                            )
                            consentStorage.setLastAdviceText(validated.summary)
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
