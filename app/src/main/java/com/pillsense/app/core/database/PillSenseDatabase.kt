package com.pillsense.app.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "medications", indices = [Index("owner")])
data class Medication(
    @PrimaryKey val id: String, val owner: String, val name: String, val dose: String,
    val form: String, val intervalHours: Int, val firstTime: String, val instructions: String,
    val startDate: String, val durationDays: Int?, val createdAt: Long = System.currentTimeMillis(),
    val active: Boolean = true,
)
@Entity(tableName = "intakes", indices = [Index("owner"), Index("medicationId")])
data class Intake(
    @PrimaryKey val id: String, val owner: String, val medicationId: String,
    val scheduledAt: Long, val status: String = "PENDING", val actedAt: Long? = null,
    val postponedUntil: Long? = null, val notifiedAt: Long? = null,
)
@Entity(tableName = "contacts", indices = [Index("owner")])
data class EmergencyContact(
    @PrimaryKey val id: String, val owner: String, val name: String, val relation: String, val phone: String,
)
@Dao
interface PillSenseDao {
    @Query("SELECT * FROM medications WHERE owner = :owner ORDER BY createdAt")
    fun observeMedications(owner: String): Flow<List<Medication>>
    @Query("SELECT * FROM intakes WHERE owner = :owner ORDER BY scheduledAt DESC")
    fun observeIntakes(owner: String): Flow<List<Intake>>
    @Query("SELECT * FROM contacts WHERE owner = :owner ORDER BY name")
    fun observeContacts(owner: String): Flow<List<EmergencyContact>>
    @Query("SELECT * FROM medications WHERE owner = :owner AND active = 1")
    suspend fun activeMedications(owner: String): List<Medication>
    @Query("SELECT MAX(scheduledAt) FROM intakes WHERE medicationId = :id")
    suspend fun lastScheduled(id: String): Long?
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun medication(id: String): Medication?
    @Query("SELECT * FROM intakes WHERE id = :id")
    suspend fun intake(id: String): Intake?
    @Query("SELECT * FROM intakes WHERE owner = :owner AND status IN ('PENDING', 'POSTPONED')")
    suspend fun pending(owner: String): List<Intake>
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMedication(medication: Medication)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIntakes(intakes: List<Intake>)
    @Query("UPDATE intakes SET status = :status, actedAt = :now, postponedUntil = :until, notifiedAt = NULL WHERE id = :id AND owner = :owner AND status IN ('PENDING', 'POSTPONED')")
    suspend fun act(id: String, owner: String, status: String, now: Long, until: Long?): Int
    @Query("UPDATE intakes SET scheduledAt = :time WHERE id = :id AND status = 'PENDING' AND notifiedAt IS NULL AND scheduledAt > :now AND scheduledAt != :time")
    suspend fun reschedulePending(id: String, time: Long, now: Long)
    @Query("UPDATE intakes SET notifiedAt = :now WHERE id = :id")
    suspend fun markNotified(id: String, now: Long)
    @Query("UPDATE medications SET active = 0 WHERE id = :id AND owner = :owner")
    suspend fun archive(id: String, owner: String)
    @Query("DELETE FROM intakes WHERE medicationId = :id AND status IN ('PENDING', 'POSTPONED') AND scheduledAt > :now")
    suspend fun removeFuture(id: String, now: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveContact(contact: EmergencyContact)
    @Query("DELETE FROM contacts WHERE id = :id AND owner = :owner")
    suspend fun deleteContact(id: String, owner: String)
}
@Database(entities = [Medication::class, Intake::class, EmergencyContact::class], version = 1, exportSchema = true)
abstract class PillSenseDatabase : RoomDatabase() { abstract fun dao(): PillSenseDao }
