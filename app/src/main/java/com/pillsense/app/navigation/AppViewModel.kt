package com.pillsense.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillsense.app.core.data.*
import com.pillsense.app.core.database.*
import com.pillsense.app.core.model.ReminderCoordinator
import com.pillsense.app.core.security.SessionStore
import com.pillsense.app.feature.medication.ui.confirm.ConfirmMedicationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class HealthState(val medications: List<Medication> = emptyList(), val intakes: List<Intake> = emptyList(), val contacts: List<EmergencyContact> = emptyList())
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppViewModel @Inject constructor(
    private val session: SessionStore, private val repository: HealthRepository,
    private val reminders: ReminderCoordinator, private val preferences: Preferences,
) : ViewModel() {
    val user = session.user
    val dark = preferences.dark.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val language = preferences.language.stateIn(viewModelScope, SharingStarted.Eagerly, "es")
    val health = user.flatMapLatest { owner ->
        if (owner == null) flowOf(HealthState()) else combine(
            repository.dao.observeMedications(owner), repository.dao.observeIntakes(owner), repository.dao.observeContacts(owner),
        ) { medications, intakes, contacts -> HealthState(medications, intakes, contacts) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HealthState())
    val busy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    init {
        viewModelScope.launch { user.collectLatest { owner ->
            if (owner != null) while (isActive) { safely { reminders.refresh() }; delay(60_000) }
        } }
    }
    private suspend fun safely(block: suspend () -> Unit) {
        try { block() } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error.value = e.message ?: "No se pudo completar la acción. Intenta de nuevo." }
    }
    private fun action(block: suspend () -> Unit) { viewModelScope.launch { safely(block) } }
    fun authenticate(email: String, password: String, register: Boolean) {
        if (busy.value) return
        busy.value = true; error.value = null
        viewModelScope.launch { try { safely { session.authenticate(email, password, register) } } finally { busy.value = false } }
    }
    fun save(data: ConfirmMedicationData, onSaved: () -> Unit) {
        if (busy.value) return
        val owner = user.value ?: return
        busy.value = true; error.value = null
        viewModelScope.launch {
            try { safely {
                repository.save(Medication(UUID.randomUUID().toString(), owner, data.name, data.dose, data.form,
                    data.intervalHours, data.firstTime, data.instructions, LocalDate.now().toString(), data.durationDays))
                onSaved()
                reminders.refresh()
            } } finally { busy.value = false }
        }
    }
    fun act(id: String, status: String) = action { reminders.act(id, status) }
    fun refresh() = action { reminders.refresh() }
    fun toggleTheme() = action { preferences.setDark(!dark.value) }
    fun toggleLanguage() = action { preferences.setLanguage(if (language.value == "es") "en" else "es") }
    fun archive(id: String) = action { user.value?.let { repository.archive(id, it); reminders.refresh() } }
    fun saveContact(name: String, relation: String, phone: String, onSaved: () -> Unit) = action {
        val owner = user.value ?: return@action
        require(name.isNotBlank() && phone.count { it.isDigit() } in 7..15 && phone.matches(Regex("[+0-9 ()-]+"))) { "Revisa el nombre y el teléfono (7 a 15 dígitos)." }
        repository.dao.saveContact(EmergencyContact(UUID.randomUUID().toString(), owner, name.trim(), relation.trim(), phone.trim()))
        onSaved()
    }
    fun removeContact(id: String) = action { user.value?.let { repository.dao.deleteContact(id, it) } }
    fun logout() = action {
        withContext(Dispatchers.IO) { session.logout() }
        reminders.stop(); error.value = null
    }
}
