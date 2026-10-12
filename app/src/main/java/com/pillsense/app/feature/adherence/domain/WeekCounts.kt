package com.pillsense.app.feature.adherence.domain

import java.time.LocalDate

data class WeekCounts(
    val taken: Int,
    val postponed: Int,
    val omitted: Int,
    val pending: Int
)

fun calculateWeekCounts(
    intakes: List<Intake>,
    schedules: List<Schedule>,
    weekStart: LocalDate
): WeekCounts {
    val weekEnd = weekStart.plusDays(6)
    val weekIntakes = intakes.filter { it.date in weekStart..weekEnd }
    val weekSchedules = schedules.filter { it.startDate <= weekEnd && (it.endDate == null || it.endDate >= weekStart) }
    
    var taken = 0
    var postponed = 0
    var omitted = 0
    var pending = 0
    
    // Count intakes by status (if Intake objects had status, which they should in real usage)
    for (intake in weekIntakes) {
        // Note: Domain Intake doesn't have status field yet; this is a placeholder
        // for when we integrate with core.database.Intake which has: TAKEN, POSTPONED, SKIPPED, PENDING
        taken++
    }
    
    // Count scheduled doses that have no corresponding intake as omitted
    val intakesByScheduleId = weekIntakes.groupBy { it.scheduleId }
    for (schedule in weekSchedules) {
        if (!intakesByScheduleId.containsKey(schedule.id)) {
            omitted++
        }
    }
    
    return WeekCounts(taken = taken, postponed = postponed, omitted = omitted, pending = pending)
}
