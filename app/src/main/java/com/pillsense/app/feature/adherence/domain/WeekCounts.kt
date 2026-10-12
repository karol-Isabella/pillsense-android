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
    
    val taken = weekIntakes.size
    val scheduled = weekSchedules.size
    val pending = (scheduled - taken).coerceAtLeast(0)
    
    return WeekCounts(taken = taken, postponed = 0, omitted = pending, pending = 0)
}
