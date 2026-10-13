package com.pillsense.app.feature.adherence.domain

import com.pillsense.app.core.database.Intake as DbIntake
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WeekCounts(
    val taken: Int,
    val postponed: Int,
    val omitted: Int,
    val pending: Int
)

fun calculateWeekCounts(
    intakes: List<DbIntake>,
    weekStart: LocalDate,
    now: Long,
    zone: ZoneId = ZoneId.systemDefault()
): WeekCounts {
    val weekEnd = weekStart.plusDays(6)
    val weekStartEpoch = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
    val weekEndEpoch = weekEnd.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    
    val weekIntakes = intakes.filter { it.scheduledAt in weekStartEpoch..weekEndEpoch }
    
    var taken = 0
    var postponed = 0
    var omitted = 0
    var pending = 0
    
    for (intake in weekIntakes) {
        when (intake.status) {
            "TAKEN" -> taken++
            "POSTPONED" -> postponed++
            "SKIPPED" -> omitted++
            "PENDING" -> pending++
        }
    }
    
    return WeekCounts(taken = taken, postponed = postponed, omitted = omitted, pending = pending)
}
