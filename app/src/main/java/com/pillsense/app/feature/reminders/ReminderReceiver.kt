package com.pillsense.app.feature.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var engine: ReminderEngine
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    "com.pillsense.ACTION" -> engine.act(intent.getStringExtra("id") ?: return@launch,
                        intent.getStringExtra("status") ?: return@launch)
                    "com.pillsense.REMIND" -> engine.deliver()
                    else -> engine.refresh()
                }
            } catch (_: Exception) {
                // Durable maintenance retries after transient storage/scheduling failures.
            } finally { pending.finish() }
        }
    }
}
