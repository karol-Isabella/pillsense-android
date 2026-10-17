package com.pillsense.app.feature.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.pillsense.app.core.data.HealthRepository
import com.pillsense.app.core.security.SessionStore
import com.pillsense.app.core.util.SmsConstants
import com.pillsense.app.core.util.permissions.AppPermission
import com.pillsense.app.core.util.permissions.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsNotificationEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HealthRepository,
    private val session: SessionStore,
) {
    private val alarm get() = context.getSystemService(AlarmManager::class.java)
    private val sharedPrefs: SharedPreferences
        get() = context.getSharedPreferences("sms-sent", Context.MODE_PRIVATE)
    
    fun scheduleSmsAlarm(intakeId: String) {
        val owner = session.user.value ?: return
        val smsTime = System.currentTimeMillis() + (SmsConstants.SMS_DELAY_MINUTES * 60 * 1000)
        
        // Check if exact alarm permission is available
        if (!PermissionManager.isGranted(context, AppPermission.EXACT_ALARM)) {
            Timber.tag("PillSenseSMS").d("programada para %s (inexacta)", formatTime(smsTime))
        } else {
            Timber.tag("PillSenseSMS").d("programada para %s", formatTime(smsTime))
        }
        
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction("com.pillsense.SMS")
            .putExtra("intakeId", intakeId)
            .putExtra("owner", owner)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intakeId.hashCode() + 2000000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (PermissionManager.isGranted(context, AppPermission.EXACT_ALARM)) {
            try { alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, smsTime, pendingIntent) }
            catch (_: SecurityException) { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, smsTime, pendingIntent) }
        } else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, smsTime, pendingIntent)
    }
    
    private fun formatTime(timeMs: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timeMs))
    }
    
    fun sendSms(intakeId: String, owner: String) {
        Timber.tag("PillSenseSMS").d("disparada")
        
        // Check if already sent (avoid duplicates)
        if (sharedPrefs.getBoolean("sent_$intakeId", false)) {
            Timber.tag("PillSenseSMS").d("toma ya resuelta")
            return
        }
        
        // Check SMS permission
        if (!PermissionManager.isGranted(context, AppPermission.SEND_SMS)) {
            Timber.tag("PillSenseSMS").d("sin permiso")
            return
        }
        
        // Check if SMS notifications are enabled for this account
        val smsStorage = context.getSharedPreferences("sms-notifications", Context.MODE_PRIVATE)
        val normalizedEmail = owner.lowercase().trim()
        if (!smsStorage.getBoolean("enabled_$normalizedEmail", false)) {
            Timber.tag("PillSenseSMS").d("interruptor apagado")
            return
        }
        
        // Check intake status
        val intake = runBlocking { repository.dao.intake(intakeId) }
        if (intake == null || intake.status != "PENDING") {
            Timber.tag("PillSenseSMS").d("toma ya resuelta")
            return
        }
        
        // Get first contact
        val contacts = runBlocking { repository.dao.contacts(owner) }
        if (contacts.isEmpty()) {
            Timber.tag("PillSenseSMS").d("sin contactos")
            return
        }
        
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            val contact = contacts.first()
            val firstName = contact.name.split(" ").first().takeIf { it.isNotEmpty() } ?: "Tu contacto"
            val message = "PillSense: $firstName no ha confirmado una toma de su medicación. Por favor comunícate con esa persona."
            
            smsManager.sendTextMessage(contact.phone, null, message, null, null)
            
            // Mark as sent
            sharedPrefs.edit().putBoolean("sent_$intakeId", true).apply()
            Timber.tag("PillSenseSMS").d("enviado")
        } catch (e: Exception) {
            Timber.tag("PillSenseSMS").d("error: %s", e.javaClass.simpleName)
        }
    }
    
    fun cancelSmsAlarm(intakeId: String) {
        val intent = Intent(context, ReminderReceiver::class.java).setAction("com.pillsense.SMS")
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intakeId.hashCode() + 2000000,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarm.cancel(pendingIntent)
        }
    }
}
