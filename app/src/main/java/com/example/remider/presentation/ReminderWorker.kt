package com.example.remider.presentation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getInt("id", 0)
        val message = inputData.getString("message") ?: "Reminder"
        val priority = inputData.getString("priority") ?: "Medium"
        val ringtoneUri = inputData.getString("ringtone") ?: ""
        val repeatType = inputData.getString("repeatType") ?: "No Repeat"
        val timeString = inputData.getString("timeString") ?: ""

        createNotificationChannel(applicationContext, priority)
        showNotification(applicationContext, id, message, priority, ringtoneUri)

        if (repeatType != "No Repeat" && timeString.isNotEmpty()) {
            rescheduleNext(id, message, priority, ringtoneUri, repeatType, timeString)
        }

        return Result.success()
    }

    private fun rescheduleNext(
        id: Int,
        message: String,
        priority: String,
        ringtone: String,
        repeatType: String,
        timeString: String
    ) {
        val calendar = Calendar.getInstance()
        try {
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val date = sdf.parse(timeString)
            if (date != null) {
                val timeCalendar = Calendar.getInstance()
                timeCalendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            }
        } catch (e: Exception) {
            return
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Calculate next occurrence
        when (repeatType) {
            "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "Monthly" -> calendar.add(Calendar.MONTH, 1)
            else -> return
        }

        val nextTimeInMillis = calendar.timeInMillis
        val notificationTime = nextTimeInMillis - 60000
        val delay = notificationTime - System.currentTimeMillis()

        if (delay > 0) {
            val data = Data.Builder()
                .putInt("id", id)
                .putString("message", message)
                .putString("priority", priority)
                .putString("ringtone", ringtone)
                .putString("repeatType", repeatType)
                .putString("timeString", timeString)
                .build()

            val nextWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("reminder_$id")
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "reminder_$id",
                androidx.work.ExistingWorkPolicy.REPLACE,
                nextWorkRequest
            )
        }
    }
}
