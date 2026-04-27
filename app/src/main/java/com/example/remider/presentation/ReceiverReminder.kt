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
import android.media.Ringtone

class ReceiverReminder: BroadcastReceiver() {
    companion object {
        private var currentRingtone: Ringtone? = null
        private var vibrator: Vibrator? = null
        
        fun stopAlerts() {
            currentRingtone?.stop()
            currentRingtone = null
            vibrator?.cancel()
            vibrator = null
        }
        
        fun startAlerts(context: Context, ringtone: Ringtone?, priority: String) {
            stopAlerts()
            
            if (priority == "High") {
                ringtone?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        it.isLooping = true
                    }
                    currentRingtone = it
                    it.play()
                }
                
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val pattern = longArrayOf(0, 1000, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means repeat
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(pattern, 0)
                }
                vibrator = v
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "DISMISS_NOTIFICATION") {
            val notificationId = intent.getIntExtra("notificationId", -1)
            
            // Stop alerts
            stopAlerts()

            if (notificationId != -1 && context != null) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(notificationId)
            }
            return
        }

        val message = intent?.getStringExtra("message") ?: "Reminder"
        val priority = intent?.getStringExtra("priority") ?: "Medium"
        val ringtoneUri = intent?.getStringExtra("ringtone") ?: ""
        
        createNotificationChannel(context, priority)
        showNotification(context, 0, message, priority, ringtoneUri)
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
                val pattern = LongArray(81)
                pattern[0] = 0
                for (i in 1..80 step 2) {
                    pattern[i] = 1000
                    pattern[i+1] = 500
                }
                vibrationPattern = pattern
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
    val intent = Intent(context, MainActivity::class.java)

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
        .setOngoing(priority == "High") // High priority is ongoing until clicked
        .setSound(if (priority == "High") null else ringtoneUri) // We play manually for high
        .setColor(if (priority == "High") 0xFFFF5252.toInt() else 0xFF4361EE.toInt())
        .addAction(R.drawable.baseline_circle_24, "OK", dismissPendingIntent)

    if (priority == "High") {
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        builder.setCategory(NotificationCompat.CATEGORY_ALARM)
        builder.setFullScreenIntent(pendingIntent, true)
        
        try {
            val r = RingtoneManager.getRingtone(context, ringtoneUri)
            ReceiverReminder.startAlerts(context, r, priority)
        } catch (e: Exception) {
            Log.e("ReceiverReminder", "Error starting alerts", e)
        }
    } else if (priority == "Medium") {
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setVibrate(longArrayOf(0, 500, 250, 500))
    } else {
        builder.setPriority(NotificationCompat.PRIORITY_LOW)
    }

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(id, builder.build())
}
