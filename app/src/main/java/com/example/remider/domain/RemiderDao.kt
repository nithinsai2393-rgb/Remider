package com.example.remider.domain

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert
    suspend fun insertReminder(reminder: RemindersEntity): Long

    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<RemindersEntity>>

    @Delete
    suspend fun deleteReminder(reminder: RemindersEntity)

    @androidx.room.Update
    suspend fun updateReminder(reminder: RemindersEntity)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}
