package com.pillsense.app.core.model

import com.pillsense.app.core.database.Intake
import com.pillsense.app.core.database.Medication
import java.time.*
import java.time.format.DateTimeFormatter

object Schedule {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    fun times(first: String, interval: Int): List<String> {
        require(interval in listOf(6, 8, 12, 24))
        require(first.matches(Regex("([01][0-9]|2[0-3]):[0-5][0-9]")))
        val start = LocalTime.parse(first)
        return (0 until 24 / interval).map { start.plusHours((it * interval).toLong()).format(formatter) }.sorted()
    }
    fun occurrences(med: Medication, through: LocalDate, zone: ZoneId = ZoneId.systemDefault(), from: LocalDate = LocalDate.parse(med.startDate)): List<Intake> {
        if (!med.active) return emptyList()
        val start = LocalDate.parse(med.startDate)
        val end = med.durationDays?.let { minOf(through, start.plusDays(it.toLong() - 1)) } ?: through
        if (end < start) return emptyList()
        val result = mutableListOf<Intake>()
        var day = maxOf(start, from)
        while (day <= end) {
            times(med.firstTime, med.intervalHours).forEach { time ->
                val epoch = day.atTime(LocalTime.parse(time)).atZone(zone).toInstant().toEpochMilli()
                if (epoch >= med.createdAt && (day > start || LocalTime.parse(time) >= LocalTime.parse(med.firstTime))) result += Intake(
                    id = "${med.id}:$day:$time", owner = med.owner, medicationId = med.id, scheduledAt = epoch,
                )
            }
            day = day.plusDays(1)
        }
        return result
    }
    fun adherence(intakes: List<Intake>, now: Long): Int? {
        val due = intakes.filter { it.scheduledAt <= now }
        return if (due.isEmpty()) null else (100 * due.count { it.status == "TAKEN" } / due.size)
    }
}
