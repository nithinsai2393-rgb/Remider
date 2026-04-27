package com.example.remider.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remider.data.RemiderState
import com.example.remider.domain.RemindersEntity
import com.example.remider.domain.RemindersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainViewModel(
    application: Application,
    private val repository: RemindersRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(RemiderState())
    val state: StateFlow<RemiderState> = _state.asStateFlow()

    init {
        observeReminders()
        rescheduleAllReminders()
    }

    private fun rescheduleAllReminders() {
        viewModelScope.launch {
            val reminders = repository.getAllReminders().first()
            reminders.filter { !it.isDone }.forEach { reminder ->
                val calendar = parseTime(reminder.time)
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    scheduleReminder(
                        getApplication(),
                        reminder.id,
                        calendar.timeInMillis,
                        "Reminder: ${reminder.task}",
                        reminder.priority,
                        reminder.ringtone,
                        reminder.repeatType,
                        reminder.time
                    )
                }
            }
        }
    }

    private fun observeReminders() {
        viewModelScope.launch {
            repository.getAllReminders().collect { reminders ->
                _state.update { it.copy(reminders = reminders) }
            }
        }
    }

    fun onFilterChanged(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun saveReminder(
        task: String,
        time: String,
        repeatType: String,
        taskType: String,
        description: String,
        priority: String,
        ringtone: String,
        context: Context
    ): Boolean {
        if (task.isBlank()) return false

        val calendar = parseTime(time)
        var timeInMillis = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()

        if (timeInMillis < currentTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            timeInMillis = calendar.timeInMillis
        }

        // Check if the time is at least 2 minutes from now
        if (timeInMillis - currentTime < 120000) {
            return false
        }

        viewModelScope.launch {
            val reminder = RemindersEntity(
                task = task,
                time = time,
                repeatType = repeatType,
                taskType = taskType,
                description = description,
                priority = priority,
                ringtone = ringtone
            )
            val id = repository.insertReminder(reminder)
            scheduleReminder(
                context = context,
                id = id.toInt(),
                timeInMillis = timeInMillis,
                message = "Reminder: $task",
                priority = priority,
                ringtone = ringtone,
                repeatType = repeatType,
                timeString = time
            )
        }
        return true
    }

    private fun parseTime(time: String): Calendar {
        val calendar = Calendar.getInstance()
        try {
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val date = sdf.parse(time)
            if (date != null) {
                val timeCalendar = Calendar.getInstance()
                timeCalendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            }
        } catch (e: Exception) {
            // Fallback if parsing fails
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 30)
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    fun updateReminder(reminder: RemindersEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
        }
    }

    fun toggleReminderDone(context: Context, reminder: RemindersEntity) {
        viewModelScope.launch {
            val newIsDone = !reminder.isDone
            repository.updateReminder(reminder.copy(isDone = newIsDone))
            if (newIsDone) {
                cancelReminder(context, reminder.id)
            } else {
                val calendar = parseTime(reminder.time)
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    scheduleReminder(
                        context = context,
                        id = reminder.id,
                        timeInMillis = calendar.timeInMillis,
                        message = "Reminder: ${reminder.task}",
                        priority = reminder.priority,
                        ringtone = reminder.ringtone,
                        repeatType = reminder.repeatType,
                        timeString = reminder.time
                    )
                }
            }
        }
    }

    fun deleteReminder(context: Context, reminder: RemindersEntity) {
        viewModelScope.launch {
            cancelReminder(context, reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    private fun cancelReminder(context: Context, id: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$id")
    }

    fun deleteAllReminders(context: Context) {
        viewModelScope.launch {
            // Cancel all alarms first. 
            // Since we don't have a list of all IDs easily without collecting the flow, 
            // and usually Clear All is a rare action, we can fetch the current list once.
            val currentReminders = state.value.reminders
            currentReminders.forEach { cancelReminder(context, it.id) }
            repository.deleteAllReminders()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleReminder(
        context: Context,
        id: Int,
        timeInMillis: Long,
        message: String,
        priority: String,
        ringtone: String,
        repeatType: String,
        timeString: String
    ) {
        // Schedule notification 1 minute before the actual time
        val notificationTime = timeInMillis - 60000
        val delay = notificationTime - System.currentTimeMillis()
        
        if (delay < 0) return

        val data = Data.Builder()
            .putInt("id", id)
            .putString("message", message)
            .putString("priority", priority)
            .putString("ringtone", ringtone)
            .putString("repeatType", repeatType)
            .putString("timeString", timeString)
            .build()

        val reminderWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_$id")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder_$id",
            androidx.work.ExistingWorkPolicy.REPLACE,
            reminderWorkRequest
        )
    }
}
