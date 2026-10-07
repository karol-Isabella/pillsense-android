package com.pillsense.app.feature.reminders

import android.app.*
import android.content.*
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.pillsense.app.MainActivity
import com.pillsense.app.R
import com.pillsense.app.core.data.HealthRepository
import com.pillsense.app.core.database.Intake
import com.pillsense.app.core.model.ReminderCoordinator
import com.pillsense.app.core.security.SessionStore
import com.pillsense.app.core.util.permissions.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HealthRepository,
    private val session: SessionStore,
) : ReminderCoordinator {
    private val mutex = Mutex()
    private val alarm get() = context.getSystemService(AlarmManager::class.java)
    private val manager get() = context.getSystemService(NotificationManager::class.java)
    private fun alarmIntent() = PendingIntent.getBroadcast(context, 0,
        Intent(context, ReminderReceiver::class.java).setAction("com.pillsense.REMIND"),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    override suspend fun refresh() = mutex.withLock {
        val owner = session.user.value ?: return@withLock
        repository.refresh(owner)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("pillsense-maintenance",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES).build())
        schedule(owner)
    }
    private suspend fun schedule(owner: String) {
        alarm.cancel(alarmIntent())
        if (!PermissionManager.isGranted(context, AppPermission.NOTIFICATIONS) || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val now = System.currentTimeMillis()
        val active = repository.dao.activeMedications(owner).map { it.id }.toSet()
        val next = repository.dao.pending(owner)
            .filter { it.medicationId in active && it.notifiedAt == null && (it.postponedUntil ?: it.scheduledAt) >= now - TimeUnit.DAYS.toMillis(1) }
            .minOfOrNull { it.postponedUntil ?: it.scheduledAt } ?: return
        val time = maxOf(next, now + 1000)
        if (PermissionManager.isGranted(context, AppPermission.EXACT_ALARM)) {
            try { alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, alarmIntent()) }
            catch (_: SecurityException) { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, alarmIntent()) }
        } else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, alarmIntent())
    }
    suspend fun deliver() = mutex.withLock {
        val owner = session.user.value ?: return@withLock
        repository.refresh(owner)
        val now = System.currentTimeMillis()
        if (PermissionManager.isGranted(context, AppPermission.NOTIFICATIONS) && NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            manager.createNotificationChannel(NotificationChannel("doses", "Recordatorios de medicamentos", NotificationManager.IMPORTANCE_HIGH))
            repository.dao.pending(owner).filter {
                it.notifiedAt == null && (it.postponedUntil ?: it.scheduledAt) in (now - TimeUnit.DAYS.toMillis(1))..now
            }.forEach { intake ->
                show(intake)
                repository.dao.markNotified(intake.id, now)
            }
        }
        schedule(owner)
    }
    private suspend fun show(intake: Intake) {
        val med = repository.dao.medication(intake.medicationId) ?: return
        if (!med.active) return
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val scheduledLabel = java.time.Instant.ofEpochMilli(intake.scheduledAt).atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("d MMM, HH:mm"))
        val late = System.currentTimeMillis() - intake.scheduledAt > TimeUnit.MINUTES.toMillis(15)
        val notification = NotificationCompat.Builder(context, "doses")
            .setSmallIcon(R.drawable.ic_lucide_pill).setContentTitle(if (late) "Toma pendiente de registrar" else "Hora de tu medicamento")
            .setContentText("${med.name} · ${med.dose} · $scheduledLabel")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${med.name} · ${med.dose}\n$scheduledLabel\n${med.instructions}"))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
        listOf("TAKEN" to "Tomada", "POSTPONED" to "Posponer 15 min", "SKIPPED" to "Omitida").forEach { (status, label) ->
            val intent = Intent(context, ReminderReceiver::class.java).setAction("com.pillsense.ACTION")
                .setData(Uri.parse("pillsense://intake/${Uri.encode(intake.id)}/$status"))
                .putExtra("id", intake.id).putExtra("status", status)
            val action = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            notification.addAction(0, label, action)
        }
        manager.notify(intake.id, 0, notification.build())
    }
    override suspend fun act(id: String, status: String) = mutex.withLock {
        require(status in listOf("TAKEN", "POSTPONED", "SKIPPED"))
        val owner = session.user.value ?: return@withLock
        val now = System.currentTimeMillis()
        val intake = repository.dao.intake(id) ?: return@withLock
        if (intake.owner != owner || intake.scheduledAt > now) return@withLock
        val until = if (status == "POSTPONED") now + TimeUnit.MINUTES.toMillis(15) else null
        if (repository.dao.act(id, owner, status, now, until) > 0) {
            manager.cancel(id, 0)
            schedule(owner)
        }
    }
    override fun stop() {
        alarm.cancel(alarmIntent())
        manager.cancelAll()
        WorkManager.getInstance(context).cancelUniqueWork("pillsense-maintenance")
    }
}
