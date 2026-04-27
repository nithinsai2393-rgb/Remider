package com.example.remider.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.remider.MainActivity
import com.example.remider.R

class ReceiverReminder : BroadcastReceiver() {
    companion object {
        private var currentRingtone: Ringtone? = null
        private var vibrator: Vibrator? = null

        fun stopAlerts() {
            try {
                currentRingtone?.stop()
                currentRingtone = null
                vibrator?.cancel()
                vibrator = null
            } catch (e: Exception) {
                Log.e("ReceiverReminder", "Error stopping alerts", e)
            }
        }

        fun startAlerts(context: Context, ringtoneUri: Uri, priority: String) {
            stopAlerts()

            if (priority == "High") {
                try {
                    val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
                    if (ringtone != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ringtone.isLooping = true
                        }
                        currentRingtone = ringtone
                        ringtone.play()
                    }

                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val pattern = longArrayOf(0, 1000, 500) // Vibrate 1s, pause 0.5s
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means repeat
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(pattern, 0)
                    }
                    vibrator = v
                } catch (e: Exception) {
                    Log.e("ReceiverReminder", "Error starting alerts", e)
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == "DISMISS_NOTIFICATION") {
            val notificationId = intent.getIntExtra("notificationId", -1)
            stopAlerts()
            if (notificationId != -1) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(notificationId)
            }
            return
        }
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
                // This pattern is for the channel itself, but manual vibrator overrides it for High
                vibrationPattern = longArrayOf(0, 1000, 500)
            } else if (priority == "Medium") {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun showNotification(context: Context?, id: Int, message: String, priority: String, ringtoneUriString: String) {
    if (context == null) return
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        id,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val dismissIntent = Intent(context, ReceiverReminder::class.java).apply {
        action = "DISMISS_NOTIFICATION"
        putExtra("notificationId", id)
    }
    val dismissPendingIntent = PendingIntent.getBroadcast(
        context,
        id + 1000,
        dismissIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val ringtoneUri = if (ringtoneUriString.isNotEmpty()) {
        Uri.parse(ringtoneUriString)
    } else {
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    val builder = NotificationCompat.Builder(context, "reminder_$priority")
        .setSmallIcon(R.drawable.outline_nest_clock_farsight_analog_24)
        .setContentTitle("Task Reminder")
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setContentIntent(pendingIntent)
        .setAutoCancel(false)
        .setOngoing(true) // Not swippable for any priority as requested
        .setColor(if (priority == "High") 0xFFFF5252.toInt() else 0xFF4361EE.toInt())
        .addAction(R.drawable.baseline_circle_24, "OK", dismissPendingIntent)

    if (priority == "High") {
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        builder.setCategory(NotificationCompat.CATEGORY_ALARM)
        // Insistent flag makes sound repeat
        builder.setFullScreenIntent(pendingIntent, true)
        
        ReceiverReminder.startAlerts(context, ringtoneUri, priority)
    } else if (priority == "Medium") {
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setSound(ringtoneUri)
        builder.setVibrate(longArrayOf(0, 500, 250, 500))
    } else {
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setSound(ringtoneUri)
    }

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = builder.build()
    
    if (priority == "High") {
        notification.flags = notification.flags or android.app.Notification.FLAG_INSISTENT
    }

    manager.notify(id, notification)
}
