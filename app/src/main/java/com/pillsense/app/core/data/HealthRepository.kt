package com.pillsense.app.core.data

import androidx.room.withTransaction
import com.pillsense.app.core.database.*
import com.pillsense.app.core.model.Schedule
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor(private val db: PillSenseDatabase) {
    val dao get() = db.dao()
    suspend fun refresh(owner: String) = db.withTransaction {
        dao.activeMedications(owner).forEach { med ->
            val from = dao.lastScheduled(med.id)?.let {
                java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate().minusDays(2)
            } ?: LocalDate.parse(med.startDate)
            val occurrences = Schedule.occurrences(med, LocalDate.now().plusDays(2), from = from)
            dao.insertIntakes(occurrences)
            val now = System.currentTimeMillis()
            occurrences.forEach { dao.reschedulePending(it.id, it.scheduledAt, now) }
        }
    }
    suspend fun save(med: Medication) = db.withTransaction {
        require(med.name.isNotBlank() && med.dose.isNotBlank())
        require(med.durationDays == null || med.durationDays in 1..3650)
        Schedule.times(med.firstTime, med.intervalHours)
        dao.insertMedication(med)
        dao.insertIntakes(Schedule.occurrences(med, LocalDate.now().plusDays(2)))
    }
    suspend fun archive(id: String, owner: String) = db.withTransaction {
        require(dao.medication(id)?.owner == owner)
        dao.archive(id, owner)
        dao.removeFuture(id, System.currentTimeMillis())
    }
}
