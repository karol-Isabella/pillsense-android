package com.pillsense.app.feature.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillsense.app.core.database.PillSenseDatabase
import com.pillsense.app.core.database.Medication
import com.pillsense.app.core.database.Intake
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugDataViewModel @Inject constructor(private val db: PillSenseDatabase) : ViewModel() {
    
    fun generateTestData(adherenceProfile: String) {
        viewModelScope.launch {
            val meds = TestDataGenerator.generateTestMedications()
            val intakes = TestDataGenerator.generateTestIntakes(adherenceProfile)
            
            val dao = db.dao()
            
            // Insert test medications
            meds.forEach { dao.insertMedication(it) }
            
            // Insert test intakes
            dao.insertIntakes(intakes)
        }
    }
    
    fun clearTestData() {
        viewModelScope.launch {
            val dao = db.dao()
            dao.deleteByOwner("debug@test")
            dao.deleteIntakesByOwner("debug@test")
        }
    }
}
