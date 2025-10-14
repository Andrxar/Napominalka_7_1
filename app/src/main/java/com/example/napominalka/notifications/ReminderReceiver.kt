package com.example.napominalka.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Напоминание сработало: ${intent.extras}")
        val id = intent.getLongExtra("id", -1L)
        val service = Intent(context, OverlayService::class.java).apply {
            putExtra("id", id)
        }
        context.startForegroundService(service)
    }
}