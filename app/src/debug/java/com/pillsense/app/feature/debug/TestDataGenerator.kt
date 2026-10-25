package com.pillsense.app.feature.debug

import com.pillsense.app.core.database.*
import java.time.*

object TestDataGenerator {
    
    suspend fun generateTestData(adherenceProfile: String): Boolean {
        try {
            // Generate test medications and intakes
            val meds = generateTestMedications()
            val intakes = generateTestIntakes(adherenceProfile)
            
            // Save to DB (would need context/DB instance here)
            // For now, this is a stub that should be called from a ViewModel
            // that has access to the database
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    suspend fun clearTestData(): Boolean {
        try {
            // Clear all test data marked with DEBUG owner
            // This would need DB context
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    fun generateTestMedications(): List<Medication> {
        val today = LocalDate.now()
        return listOf(
            Medication(
                id = "debug_med_1",
                owner = "debug@test",
                name = "Test Med A",
                dose = "500mg",
                form = "tablet",
                intervalHours = 8,
                firstTime = "08:00",
                instructions = "Test medication for adherence tracking",
                startDate = today.minusDays(30).toString(),
                durationDays = null,
                active = true
            ),
            Medication(
                id = "debug_med_2",
                owner = "debug@test",
                name = "Test Med B",
                dose = "250mg",
                form = "capsule",
                intervalHours = 12,
                firstTime = "06:00",
                instructions = "Test medication for adherence tracking",
                startDate = today.minusDays(30).toString(),
                durationDays = null,
                active = true
            ),
            Medication(
                id = "debug_med_3",
                owner = "debug@test",
                name = "Test Med C",
                dose = "100mg",
                form = "liquid",
                intervalHours = 6,
                firstTime = "09:00",
                instructions = "Test medication for adherence tracking",
                startDate = today.minusDays(30).toString(),
                durationDays = null,
                active = true
            ),
            Medication(
                id = "debug_med_4",
                owner = "debug@test",
                name = "Test Med D",
                dose = "20mg",
                form = "patch",
                intervalHours = 24,
                firstTime = "07:00",
                instructions = "Test medication for adherence tracking",
                startDate = today.minusDays(30).toString(),
                durationDays = null,
                active = true
            )
        )
    }
    
    fun generateTestIntakes(adherenceProfile: String): List<Intake> {
        val zone = ZoneId.systemDefault()
        val base = mutableListOf<Intake>()
        val today = LocalDate.now()
        
        val takeRate = when (adherenceProfile) {
            "HIGH" -> 0.9
            "MEDIUM" -> 0.65
            "LOW" -> 0.35
            else -> 0.65
        }
        
        (0..20).forEach { dayOffset ->
            val day = today.minusDays(dayOffset.toLong())
            val dayStart = day.atStartOfDay(zone).toInstant().toEpochMilli()
            
            repeat(4) { medIndex ->
                val scheduled = dayStart + (medIndex * 6 * 3600000L)
                val status = if (Math.random() < takeRate) "TAKEN" else "SKIPPED"
                base.add(Intake(
                    id = "debug_intake_${dayOffset}_$medIndex",
                    owner = "debug@test",
                    medicationId = "debug_med_$medIndex",
                    scheduledAt = scheduled,
                    status = status,
                    actedAt = if (status == "TAKEN") scheduled + 300000 else null,
                    postponedUntil = null,
                    notifiedAt = scheduled - 60000
                ))
            }
        }
        
        return base
    }
}
