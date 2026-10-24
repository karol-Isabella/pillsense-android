package com.pillsense.app.core.model

import com.pillsense.app.core.database.Intake
import java.time.*

sealed class AdviceResult {
    data object NoHistory : AdviceResult()
    data class MissedIntakes(val count: Int, val mostCommonHour: Int?) : AdviceResult()
    data class PostponedIntakes(val count: Int) : AdviceResult()
    data class SomeIncomplete(val count: Int) : AdviceResult()
    data object AllCompleted : AdviceResult()
}

object AdherenceAdvice {
    fun generate(intakes: List<Intake>, now: Long, zone: ZoneId = ZoneId.systemDefault()): AdviceResult {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val start = today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        val recent = intakes.filter { it.scheduledAt in start..now }
        if (recent.isEmpty()) return AdviceResult.NoHistory
        
        val missed = recent.count { it.status in listOf("PENDING", "SKIPPED") }
        val postponed = recent.count { it.status == "POSTPONED" }
        
        if (missed >= 3) {
            val hour = recent.filter { it.status in listOf("PENDING", "SKIPPED") }
                .groupingBy { Instant.ofEpochMilli(it.scheduledAt).atZone(zone).hour }.eachCount().maxByOrNull { it.value }?.key
            return AdviceResult.MissedIntakes(missed, hour)
        }
        if (postponed >= 2) return AdviceResult.PostponedIntakes(postponed)
        if (missed > 0) return AdviceResult.SomeIncomplete(missed)
        
        return AdviceResult.AllCompleted
    }
}
