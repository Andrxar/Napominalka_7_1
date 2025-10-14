package com.example.napominalka.data

import com.example.napominalka.data.db.ReminderDao
import com.example.napominalka.data.db.ReminderEntity

class ReminderRepository(private val dao: ReminderDao) {
    suspend fun getAll(): List<Reminder> = dao.getAll().map { it.toDomain() }
    suspend fun getActive(): List<Reminder> = dao.getActive().map { it.toDomain() }
    suspend fun getCompleted(): List<Reminder> = dao.getCompleted().map { it.toDomain() }
    suspend fun getById(id: Long): Reminder? = dao.getById(id)?.toDomain()

    suspend fun add(
        title: String,
        description: String,
        timeMillis: Long,
        repeat: RepeatOption
    ): Long {
        val entity = ReminderEntity(
            id = 0,
            title = title,
            description = description,
            timeMillis = timeMillis,
            repeatString = repeat.name,
            statusString = ReminderStatus.ACTIVE.name
        )
        return dao.insert(entity)
    }

    suspend fun update(
        id: Long,
        title: String,
        description: String,
        timeMillis: Long,
        repeat: RepeatOption
    ) {
        val existing = dao.getById(id) ?: return
        dao.update(
            existing.copy(
                title = title,
                description = description,
                timeMillis = timeMillis,
                repeatString = repeat.name
            )
        )
    }

    suspend fun delete(id: Long) {
        val existing = dao.getById(id) ?: return
        dao.delete(existing)
    }

    suspend fun markCompleted(id: Long) {
        dao.updateStatus(id, ReminderStatus.COMPLETED.name)
    }

    suspend fun markOverdue(id: Long) {
        dao.updateStatus(id, ReminderStatus.OVERDUE.name)
    }
}

private fun ReminderEntity.toDomain(): Reminder =
    Reminder(
        id = this.id,
        title = this.title,
        description = this.description,
        timeMillis = this.timeMillis,
        repeat = RepeatOption.valueOf(this.repeatString),
        status = ReminderStatus.valueOf(this.statusString)
    )