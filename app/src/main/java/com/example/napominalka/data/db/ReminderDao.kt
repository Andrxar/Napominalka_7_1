package com.example.napominalka.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE statusString='ACTIVE'")
    suspend fun getActive(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE statusString='COMPLETED'")
    suspend fun getCompleted(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("UPDATE reminders SET statusString = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Insert
    suspend fun insert(entity: ReminderEntity): Long

    @Update
    suspend fun update(entity: ReminderEntity)

    @Delete
    suspend fun delete(entity: ReminderEntity)
}