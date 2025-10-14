package com.example.napominalka.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val timeMillis: Long,
    val repeatString: String, // Once, Daily, Weekly, Monthly, Yearly
    val statusString: String // ACTIVE, COMPLETED, OVERDUE
)