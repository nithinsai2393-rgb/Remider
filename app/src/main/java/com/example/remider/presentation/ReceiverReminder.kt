package com.example.remider.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.remider.MainActivity
import com.example.remider.R

import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator

class ReceiverReminder: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: android.content.Intent?) {
        val message = intent?.getStringExtra("message") ?: "Reminder"
        val priority = intent?.getStringExtra("priority") ?: "Medium"
        val ringtoneUri = intent?.getStringExtra("ringtone") ?: ""
        
        createNotificationChannel(context, priority)
        showNotification(context, message, priority, ringtoneUri)
    }
}

fun createNotificationChannel(context: Context?, priority: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context != null) {
        val importance = when (priority) {
            "Low" -> NotificationManager.IMPORTANCE_LOW
            "Medium" -> NotificationManager.IMPORTANCE_DEFAULT
            "High" -> NotificationManager.IMPORTANCE_HIGH
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }
        val channel = NotificationChannel(
            "reminder_$priority",
            "Reminders $priority",
            importance
        ).apply {
            description = "Channel for $priority priority reminders"
            if (priority == "High") {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            } else if (priority == "Medium") {
                enableVibration(true)
            }
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun showNotification(context: Context?, message: String, priority: String, ringtoneUriString: String) {
    if (context == null) return
    val intent = Intent(context, MainActivity::class.java)

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val ringtoneUri = if (ringtoneUriString.isNotEmpty()) {
        Uri.parse(ringtoneUriString)
    } else {
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    val builder = NotificationCompat.Builder(context, "reminder_$priority")
        .setSmallIcon(R.drawable.outline_nest_clock_farsight_analog_24)
        .setContentTitle("Reminder")
        .setContentText(message)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setSound(ringtoneUri)

    if (priority == "High") {
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setVibrate(longArrayOf(0, 500, 200, 500))
        if (ringtoneUriString.isNotEmpty()) {
            try {
                val r = RingtoneManager.getRingtone(context, ringtoneUri)
                if (r != null) {
                    r.play()
                } else {
                    Log.e("ReceiverReminder", "Ringtone is null for URI: $ringtoneUriString")
                }
            } catch (e: Exception) {
                Log.e("ReceiverReminder", "Error playing ringtone", e)
            }
        }
    } else if (priority == "Medium") {
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setVibrate(longArrayOf(0, 250, 250, 250))
    } else {
        builder.setPriority(NotificationCompat.PRIORITY_LOW)
    }

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(System.currentTimeMillis().toInt(), builder.build())
}
