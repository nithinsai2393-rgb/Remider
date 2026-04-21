package com.example.remider.domain

import kotlinx.coroutines.flow.Flow

class RemindersRepository(
    private val dao: ReminderDao
) {
    suspend fun insertReminder(reminder: RemindersEntity): Long {
        return dao.insertReminder(reminder)
    }
    fun getAllReminders(): Flow<List<RemindersEntity>> {
        return dao.getAllReminders()
    }

    suspend fun deleteReminder(reminder: RemindersEntity) {
        dao.deleteReminder(reminder)
    }

    suspend fun updateReminder(reminder: RemindersEntity) {
        dao.updateReminder(reminder)
    }

    suspend fun deleteAllReminders() {
        dao.deleteAllReminders()
    }
}