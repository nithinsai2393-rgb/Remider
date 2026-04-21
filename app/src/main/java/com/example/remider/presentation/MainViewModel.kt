package com.example.remider.presentation

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remider.data.RemiderState
import com.example.remider.domain.RemindersEntity
import com.example.remider.domain.RemindersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(
    private val repository: RemindersRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RemiderState())
    val state: StateFlow<RemiderState> = _state.asStateFlow()

    init {
        observeReminders()
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
    ) {
        if (task.isBlank()) return

        val calendar = parseTime(time)
        val timeInMillis = calendar.timeInMillis
        val adjustedTimeInMillis = if (timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.timeInMillis
        } else {
            timeInMillis
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
                timeInMillis = adjustedTimeInMillis,
                message = "Reminder: $task",
                priority = priority,
                ringtone = ringtone
            )
        }
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
                        ringtone = reminder.ringtone
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
        val intent = Intent(context, ReceiverReminder::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
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
        ringtone: String
    ) {
        val intent = Intent(context, ReceiverReminder::class.java).apply {
            putExtra("message", message)
            putExtra("priority", priority)
            putExtra("ringtone", ringtone)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback for devices where SCHEDULE_EXACT_ALARM is denied
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }
}
