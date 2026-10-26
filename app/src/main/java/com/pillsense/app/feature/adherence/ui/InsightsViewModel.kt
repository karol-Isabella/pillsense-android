package com.pillsense.app.feature.adherence.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillsense.app.BuildConfig
import com.pillsense.app.feature.adherence.data.ConsentStorage
import com.pillsense.app.feature.adherence.data.GeminiClient
import com.pillsense.app.feature.adherence.data.GeminiResponse
import com.pillsense.app.feature.adherence.data.GeminiRecommendation
import com.pillsense.app.feature.adherence.data.GeminiResponseValidator
import com.pillsense.app.feature.adherence.domain.AnonymousAdherence
import com.pillsense.app.core.database.Intake as DbIntake
import com.pillsense.app.core.database.Medication
import com.pillsense.app.core.database.AiCache
import com.pillsense.app.core.database.PillSenseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId

data class InsightsUiState(
    val geminiResponse: GeminiResponse? = null,
    val isAiEnabled: Boolean = false,
    val source: String = "Reglas locales",
    val lastAdviceDate: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val consentStorage: ConsentStorage,
    private val geminiClient: GeminiClient,
    private val database: PillSenseDatabase
) : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState
    
    private var currentEmail = ""
    private val loadMutex = Mutex()
    private var lastLoadTime = 0L
    private var lastDataHash = ""

    init {
        _uiState.value = _uiState.value.copy(isAiEnabled = consentStorage.hasConsent(currentEmail))
    }
    
    fun setUserEmail(email: String) {
        currentEmail = email
        _uiState.value = _uiState.value.copy(isAiEnabled = consentStorage.hasConsent(email))
    }

    fun loadGeminiAdvice(intakes: List<DbIntake>, medications: List<Medication>, language: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            if (!loadMutex.tryLock()) {
                android.util.Log.w("PillSenseAI", "Llamada simultánea bloqueada")
                return@launch
            }
            try {
                val hasConsent = consentStorage.hasConsent(currentEmail)
                if (!hasConsent) {
                    android.util.Log.d("PillSenseAI", "sin consentimiento")
                    _uiState.value = _uiState.value.copy(source = "Reglas locales")
                    return@launch
                }
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    android.util.Log.d("PillSenseAI", "sin clave API")
                    return@launch
                }
                
                val now = System.currentTimeMillis()
                val zone = ZoneId.systemDefault()
                val totalDoses = intakes.size
                val takenDoses = intakes.count { it.status == "TAKEN" }
                
                // Nivel 0: Sin tomas en la ventana
                if (totalDoses == 0) {
                    android.util.Log.d("PillSenseAI", "Nivel 0: $totalDoses programadas, $takenDoses tomadas en ventana")
                    _uiState.value = _uiState.value.copy(
                        geminiResponse = GeminiResponse(
                            summary = if (language == "es") "¡Bienvenido! Comienza a registrar tus medicamentos para recibir seguimiento." 
                                     else "Welcome! Start recording your medications to receive tracking.",
                            patterns = emptyList(),
                            recommendations = emptyList()
                        ),
                        source = "Reglas locales"
                    )
                    return@launch
                }
                
                // Nivel 1: 1 a 4 dosis (inicial)
                if (totalDoses in 1..4) {
                    android.util.Log.d("PillSenseAI", "Nivel 1: $totalDoses programadas, $takenDoses tomadas en ventana (inicial)")
                    
                    // Llamar a Gemini con "nivel": "inicial"
                    val anonLabel = try {
                        AnonymousAdherence.buildAnonymousStats(intakes, medications, zone, language, levelTag = "inicial")
                    } catch (e: Exception) {
                        android.util.Log.w("PillSenseAI", "error buildAnonymousStats: ${e.javaClass.simpleName}")
                        return@launch
                    }
                    
                    if (anonLabel.isBlank()) {
                        android.util.Log.d("PillSenseAI", "sin datos de tomas")
                        return@launch
                    }
                    
                    if (BuildConfig.DEBUG) {
                        android.util.Log.d("PillSenseAI", "Payload JSON: $anonLabel")
                    }

                    val dataHash = anonLabel.hashCode().toString()
                    
                    // Validar caché (< 1 hora) - ignorar si contiene JSON crudo
                    val cache = database.dao().getAiCache(currentEmail)
                    if (cache != null && cache.dataHash == dataHash && (now - cache.cachedAt) < 3600000) {
                        // Ignorar caché si parece JSON (comienza con {)
                        if (!cache.summary.trim().startsWith("{")) {
                            android.util.Log.w("PillSenseAI", "Usando caché guardada (nivel inicial)")
                            _uiState.value = _uiState.value.copy(
                                geminiResponse = GeminiResponse(cache.summary, emptyList(), emptyList()),
                                source = "IA"
                            )
                            return@launch
                        }
                    }
                    
                    // Throttle: no hacer llamada si hace menos de 5 minutos
                    if ((now - lastLoadTime) < 300000) {
                        android.util.Log.w("PillSenseAI", "Menos de 5 min desde última llamada")
                        return@launch
                    }
                    
                    lastLoadTime = now
                    
                    val result = geminiClient.generateAdvice(anonLabel, BuildConfig.GEMINI_API_KEY, BuildConfig.GEMINI_MODEL, BuildConfig.GEMINI_MODEL_FALLBACK)
                    if (result.isFailure) {
                        android.util.Log.d("PillSenseAI", "fallo Gemini: ${result.exceptionOrNull()?.message}")
                        _uiState.value = _uiState.value.copy(
                            geminiResponse = GeminiResponse(
                                summary = if (language == "es") "¡Felicitaciones! Cumpliste con tu medicación. Sigue así." 
                                         else "Congratulations! You completed your medication. Keep it up.",
                                patterns = emptyList(),
                                recommendations = emptyList()
                            ),
                            source = "Reglas locales"
                        )
                        return@launch
                    }
                    
                    if (result.isSuccess) {
                        val response = result.getOrNull()
                        if (response != null && response.summary.isNotBlank()) {
                            // Parsear respuesta JSON
                            val parsed = GeminiResponseValidator.parseGeminiResponse(response.summary)
                            if (parsed == null) {
                                if (BuildConfig.DEBUG) {
                                    android.util.Log.d("PillSenseAI", "parseo falló en nivel inicial, JSON crudo: ${response.summary.take(200)}")
                                }
                                _uiState.value = _uiState.value.copy(
                                    geminiResponse = GeminiResponse(
                                        summary = if (language == "es") "¡Felicitaciones! Cumpliste con tu medicación. Sigue así." 
                                                 else "Congratulations! You completed your medication. Keep it up.",
                                        patterns = emptyList(),
                                        recommendations = emptyList()
                                    ),
                                    source = "Reglas locales"
                                )
                                return@launch
                            }
                            
                            // Validar respuesta parseada
                            val adherencePct = (intakes.count { it.status == "TAKEN" } * 100) / intakes.size
                            val omitidas = intakes.count { it.status == "SKIPPED" }
                            val validated = GeminiResponseValidator.validate(parsed.resumen, parsed.recomendaciones, adherencePct, omitidas, language, levelTag = "inicial")
                            
                            if (!validated.isValid) {
                                if (BuildConfig.DEBUG) {
                                    val textPreview = parsed.resumen.take(200)
                                    android.util.Log.d("PillSenseAI", "validador rechazó inicial: ${validated.rejectionReason} | Text: $textPreview")
                                }
                                _uiState.value = _uiState.value.copy(
                                    geminiResponse = GeminiResponse(
                                        summary = if (language == "es") "¡Felicitaciones! Cumpliste con tu medicación. Sigue así." 
                                                 else "Congratulations! You completed your medication. Keep it up.",
                                        patterns = emptyList(),
                                        recommendations = emptyList()
                                    ),
                                    source = "Reglas locales"
                                )
                                return@launch
                            }
                            
                            if (AnonymousAdherence.containsForbiddenKeyword(response.summary)) {
                                android.util.Log.w("PillSenseAI", "respuesta bloqueada nivel inicial")
                                _uiState.value = _uiState.value.copy(
                                    geminiResponse = GeminiResponse(
                                        summary = if (language == "es") "¡Felicitaciones! Cumpliste con tu medicación. Sigue así." 
                                                 else "Congratulations! You completed your medication. Keep it up.",
                                        patterns = emptyList(),
                                        recommendations = emptyList()
                                    ),
                                    source = "Reglas locales"
                                )
                                return@launch
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                geminiResponse = GeminiResponse(
                                    summary = parsed.resumen,
                                    patterns = emptyList(),
                                    recommendations = emptyList()
                                ),
                                source = "IA"
                            )
                            
                            database.dao().saveAiCache(AiCache(
                                owner = currentEmail,
                                summary = parsed.resumen,
                                dataHash = dataHash,
                                cachedAt = now
                            ))
                            
                            consentStorage.setLastAdviceText(response.summary)
                            consentStorage.setLastAdviceDate(LocalDate.now().toString())
                            android.util.Log.d("PillSenseAI", "consejo nivel inicial generado")
                        }
                    }
                    return@launch
                }
                
                // Nivel 2: 5+ dosis (comportamiento completo)
                android.util.Log.d("PillSenseAI", "Nivel 2: $totalDoses programadas, $takenDoses tomadas en ventana (completo)")
                
                val anonLabel = try {
                    AnonymousAdherence.buildAnonymousStats(intakes, medications, zone, language)
                } catch (e: Exception) {
                    android.util.Log.w("PillSenseAI", "error buildAnonymousStats: ${e.javaClass.simpleName}")
                    return@launch
                }
                
                if (anonLabel.isBlank()) {
                    android.util.Log.d("PillSenseAI", "sin datos de tomas")
                    return@launch
                }
                
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("PillSenseAI", "Payload JSON: $anonLabel")
                }

                val dataHash = anonLabel.hashCode().toString()
                
                // Validar caché válida - ignorar si contiene JSON crudo
                val cache = database.dao().getAiCache(currentEmail)
                if (cache != null && cache.dataHash == dataHash && (now - cache.cachedAt) < 3600000) {
                    // Ignorar caché si parece JSON (comienza con {)
                    if (!cache.summary.trim().startsWith("{")) {
                        android.util.Log.w("PillSenseAI", "Usando caché guardada (nivel 2)")
                        _uiState.value = _uiState.value.copy(
                            geminiResponse = GeminiResponse(cache.summary, emptyList(), emptyList()),
                            source = "IA"
                        )
                        return@launch
                    }
                }
                
                // Throttle: no hacer llamada si hace menos de 5 minutos
                if ((now - lastLoadTime) < 300000) {
                    android.util.Log.w("PillSenseAI", "Menos de 5 min desde última llamada")
                    return@launch
                }
                
                lastLoadTime = now
                
                val result = geminiClient.generateAdvice(anonLabel, BuildConfig.GEMINI_API_KEY, BuildConfig.GEMINI_MODEL, BuildConfig.GEMINI_MODEL_FALLBACK)
                if (result.isFailure) {
                    android.util.Log.d("PillSenseAI", "fallo Gemini: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        geminiResponse = GeminiResponse(
                            summary = if (language == "es") "Consulta tu historial de medicamentos y mantén la consistencia." 
                                     else "Check your medication history and stay consistent.",
                            patterns = emptyList(),
                            recommendations = emptyList()
                        ),
                        source = "Reglas locales"
                    )
                    return@launch
                }
                
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null && response.summary.isNotBlank()) {
                        // Parsear respuesta JSON
                        val parsed = GeminiResponseValidator.parseGeminiResponse(response.summary)
                        if (parsed == null) {
                            if (BuildConfig.DEBUG) {
                                android.util.Log.d("PillSenseAI", "parseo falló en nivel 2, JSON crudo: ${response.summary.take(200)}")
                            }
                            _uiState.value = _uiState.value.copy(
                                geminiResponse = GeminiResponse(
                                    summary = if (language == "es") "Consulta tu historial de medicamentos y mantén la consistencia." 
                                             else "Check your medication history and stay consistent.",
                                    patterns = emptyList(),
                                    recommendations = emptyList()
                                ),
                                source = "Reglas locales"
                            )
                            return@launch
                        }
                        
                        // Validar respuesta parseada
                        val adherencePct = (intakes.count { it.status == "TAKEN" } * 100) / intakes.size
                        val omitidas = intakes.count { it.status == "SKIPPED" }
                        val validated = GeminiResponseValidator.validate(parsed.resumen, parsed.recomendaciones, adherencePct, omitidas, language)
                        
                        if (!validated.isValid) {
                            if (BuildConfig.DEBUG) {
                                val textPreview = response.summary.take(200)
                                android.util.Log.d("PillSenseAI", "validador rechazó nivel 2: ${validated.rejectionReason} | Text: $textPreview")
                            }
                            _uiState.value = _uiState.value.copy(
                                geminiResponse = GeminiResponse(
                                    summary = if (language == "es") "Consulta tu historial de medicamentos y mantén la consistencia." 
                                             else "Check your medication history and stay consistent.",
                                    patterns = emptyList(),
                                    recommendations = emptyList()
                                ),
                                source = "Reglas locales"
                            )
                            return@launch
                        }
                        
                        if (AnonymousAdherence.containsForbiddenKeyword(response.summary)) {
                            android.util.Log.w("PillSenseAI", "respuesta bloqueada por seguridad, usando respaldo local")
                            _uiState.value = _uiState.value.copy(
                                geminiResponse = GeminiResponse(
                                    summary = if (language == "es") "Consulta tu historial de medicamentos y mantén la consistencia." 
                                             else "Check your medication history and stay consistent.",
                                    patterns = emptyList(),
                                    recommendations = emptyList()
                                ),
                                source = "Reglas locales"
                            )
                            return@launch
                        }
                        
                        
                        val cleanedResponse = GeminiResponse(
                            summary = parsed.resumen,
                            patterns = emptyList(),
                            recommendations = parsed.recomendaciones.mapIndexed { index, rec ->
                                GeminiRecommendation(type = "recommendation", medicationLabel = "Rec. ${index + 1}", reason = rec)
                            }
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            geminiResponse = cleanedResponse,
                            source = "IA"
                        )
                        
                        database.dao().saveAiCache(AiCache(
                            owner = currentEmail,
                            summary = parsed.resumen,
                            dataHash = dataHash,
                            cachedAt = now
                        ))
                        
                        consentStorage.setLastAdviceText(cleanedResponse.summary)
                        consentStorage.setLastAdviceDate(LocalDate.now().toString())
                        android.util.Log.d("PillSenseAI", "consejo nivel 2 generado exitosamente")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("PillSenseAI", "error: ${e.javaClass.simpleName}")
            } finally {
                loadMutex.unlock()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun setConsentGranted(granted: Boolean) {
        consentStorage.setConsent(currentEmail, granted)
        _uiState.value = _uiState.value.copy(isAiEnabled = granted)
    }
}
