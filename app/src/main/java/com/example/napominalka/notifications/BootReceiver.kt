package com.example.napominalka.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.napominalka.data.ReminderRepository
import com.example.napominalka.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val repo = ReminderRepository(db.reminders())
                val active = repo.getActive()
                val now = System.currentTimeMillis()
                for (reminder in active) {
                    if (reminder.timeMillis > now) {
                        ReminderScheduler.schedule(context, reminder.id, reminder.timeMillis)
                    }
                }
            } catch (t: Throwable) {
                Log.e("BootReceiver", "Failed to reschedule reminders after reboot", t)
            }
        }
    }
}