package com.example.remider.data

import com.example.remider.domain.RemindersEntity

data class RemiderState(
    val task: String = "",
    val time: String = "",
    val repeatType: String = "",
    val taskType: String = "",
    val description: String = "",
    val priority: String = "",
    val selectedFilter: String = "All",
    val reminders: List<RemindersEntity> = emptyList()
)
