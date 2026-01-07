package com.kroslabs.myjournal.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.kroslabs.myjournal.MainActivity
import com.kroslabs.myjournal.R
import java.util.Calendar

object NotificationHelper {
    private const val CHANNEL_ID = "weekly_review_channel"
    private const val NOTIFICATION_ID = 1001
    private const val REQUEST_CODE = 1001

    fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.notification_channel_name)
        val description = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            this.description = description
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showWeeklyReviewNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_weekly_review", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.weekly_review_notification_title))
            .setContentText(context.getString(R.string.weekly_review_notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelWeeklyReviewNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun scheduleWeeklyReview(context: Context, dayOfWeek: Int, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, WeeklyReviewReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel any existing alarm
        alarmManager.cancel(pendingIntent)

        // Calculate next occurrence
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Map our day (1=Monday) to Calendar day (2=Monday)
            val calendarDay = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
            set(Calendar.DAY_OF_WEEK, calendarDay)

            // If the time has passed this week, schedule for next week
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        // Schedule repeating alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }

    fun cancelScheduledReview(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WeeklyReviewReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
