package com.example.napominalka.data

enum class RepeatOption { Once, Daily, Weekly, Monthly, Yearly }
enum class ReminderStatus { ACTIVE, COMPLETED, OVERDUE }

data class Reminder(
    val id: Long,
    val title: String,
    val description: String,
    val timeMillis: Long,
    val repeat: RepeatOption = RepeatOption.Once,
    val status: ReminderStatus = ReminderStatus.ACTIVE
)