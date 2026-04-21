package com.example.remider.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class RemindersEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val task: String = "",
    val time: String = "",
    val repeatType: String = "",
    val taskType: String = "",
    val description: String = "",
    val priority: String = "",
    val ringtone: String = "",
    val isDone: Boolean = false
)
