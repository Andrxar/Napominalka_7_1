package com.example.napominalka.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.napominalka.data.Reminder
import com.example.napominalka.data.ReminderStatus
import com.example.napominalka.data.RepeatOption
import com.example.napominalka.data.ReminderRepository
import kotlinx.coroutines.launch

class ReminderViewModel(private val repo: ReminderRepository) : ViewModel() {
    private val items = mutableStateListOf<Reminder>()
    val reminders: List<Reminder> get() = items

    init {
        viewModelScope.launch {
            items.clear()
            items.addAll(repo.getAll())
        }
    }

    suspend fun addReminder(
        title: String,
        description: String,
        timeMillis: Long,
        repeat: RepeatOption
    ): Long {
        val id = repo.add(title, description, timeMillis, repeat)
        items.add(
            Reminder(
                id = id,
                title = title,
                description = description,
                timeMillis = timeMillis,
                repeat = repeat,
                status = ReminderStatus.ACTIVE
            )
        )
        return id
    }

    suspend fun updateReminder(
        id: Long,
        title: String,
        description: String,
        timeMillis: Long,
        repeat: RepeatOption
    ) {
        repo.update(id, title, description, timeMillis, repeat)
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) {
            items[idx] = items[idx].copy(
                title = title,
                description = description,
                timeMillis = timeMillis,
                repeat = repeat
            )
        }
    }

    suspend fun deleteReminder(id: Long) {
        repo.delete(id)
        items.removeAll { it.id == id }
    }

    suspend fun markCompleted(id: Long) {
        repo.markCompleted(id)
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) {
            items[idx] = items[idx].copy(status = ReminderStatus.COMPLETED)
        }
    }

    fun refreshOverdue(nowMillis: Long) {
        for (i in items.indices) {
            val r = items[i]
            if (r.status == ReminderStatus.ACTIVE && r.timeMillis < nowMillis) {
                items[i] = r.copy(status = ReminderStatus.OVERDUE)
            }
        }
    }

    fun getById(id: Long): Reminder? = items.find { it.id == id }
}