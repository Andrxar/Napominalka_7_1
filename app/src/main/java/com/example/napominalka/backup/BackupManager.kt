package com.example.napominalka.backup

import android.content.Context
import java.io.File
import com.example.napominalka.data.Reminder
import com.example.napominalka.data.RepeatOption

object BackupManager {
    fun exportToTxt(context: Context, reminders: List<Reminder>): File {
        val dir = File(context.getExternalFilesDir(null), "backup").apply { mkdirs() }
        val file = File(dir, "reminders.txt")
        file.bufferedWriter().use { out ->
            reminders.forEach { r ->
                // Формат: id|title|description|timeMillis|repeat|status
                out.write(listOf(r.id, r.title, r.description, r.timeMillis, r.repeat.name, r.status.name).joinToString("|") )
                out.newLine()
            }
        }
        return file
    }

    fun importFromTxt(context: Context, file: File): List<Reminder> {
        if (!file.exists()) return emptyList()
        val list = mutableListOf<Reminder>()
        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split("|")
                if (parts.size >= 6) {
                    val id = parts[0].toLongOrNull() ?: 0L
                    val title = parts[1]
                    val description = parts[2]
                    val timeMillis = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                    val repeat = runCatching { RepeatOption.valueOf(parts[4]) }.getOrDefault(RepeatOption.Once)
                    val status = runCatching { com.example.napominalka.data.ReminderStatus.valueOf(parts[5]) }.getOrDefault(com.example.napominalka.data.ReminderStatus.ACTIVE)
                    list.add(com.example.napominalka.data.Reminder(id, title, description, timeMillis, repeat, status))
                }
            }
        }
        return list
    }
}