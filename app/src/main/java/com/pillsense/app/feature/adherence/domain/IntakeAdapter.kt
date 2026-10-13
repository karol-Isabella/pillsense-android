package com.pillsense.app.feature.adherence.domain

import com.pillsense.app.core.database.Intake as DbIntake
import com.pillsense.app.core.database.Medication
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Adapter to convert core.database models to domain models for adherence analysis.
 */
object IntakeAdapter {
    
    fun toDomainIntake(
        intake: DbIntake,
        zone: ZoneId = ZoneId.systemDefault()
    ): Intake {
        val localDate = Instant.ofEpochMilli(intake.scheduledAt).atZone(zone).toLocalDate()
        val hour = Instant.ofEpochMilli(intake.scheduledAt).atZone(zone).hour
        return Intake(
            id = intake.id,
            medicationId = intake.medicationId,
            scheduleId = intake.id,
            date = localDate,
            hour = hour
        )
    }

    fun toDomainSchedule(
        medication: Medication,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<Schedule> {
        if (!medication.active) return emptyList()
        val startDate = LocalDate.parse(medication.startDate)
        val endDate = medication.durationDays?.let { startDate.plusDays(it.toLong() - 1) }
        return listOf(
            Schedule(
                id = medication.id,
                medicationId = medication.id,
                hour = 0,
                startDate = startDate,
                endDate = endDate
            )
        )
    }

    fun toDomainIntakes(
        intakes: List<DbIntake>,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<Intake> = intakes.map { toDomainIntake(it, zone) }

    fun toDomainSchedules(
        medications: List<Medication>,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<Schedule> = medications.flatMap { toDomainSchedule(it, zone) }
}
