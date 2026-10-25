package com.pillsense.app.feature.debug

import com.pillsense.app.core.database.*
import java.time.*

object TestDataGenerator {
    
    enum class AdherenceLevel {
        HIGH, MEDIUM, LOW
    }
    
    fun generateTestIntakes(adherenceLevel: AdherenceLevel): List<Intake> {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val base = mutableListOf<Intake>()
        val today = LocalDate.now()
        
        val takeRate = when (adherenceLevel) {
            AdherenceLevel.HIGH -> 0.9
            AdherenceLevel.MEDIUM -> 0.65
            AdherenceLevel.LOW -> 0.35
        }
        
        (0..20).forEach { dayOffset ->
            val day = today.minusDays(dayOffset.toLong())
            val dayStart = day.atStartOfDay(zone).toInstant().toEpochMilli()
            
            repeat(4) { medIndex ->
                val scheduled = dayStart + (medIndex * 6 * 3600000L)
                val status = if (Math.random() < takeRate) "TAKEN" else "SKIPPED"
                base.add(Intake(
                    id = "test_${dayOffset}_$medIndex",
                    owner = "test@local",
                    medicationId = "test_med_$medIndex",
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
