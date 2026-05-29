package com.sujin.nubloompilot.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Scheduler for sleep check-in notifications based on MAIN_SLEEP end time.
 */
object SleepCheckInNotificationScheduler {
    private const val TAG = "SleepNotificationScheduler"

    fun scheduleAfterMainSleep(
        context: Context,
        mainSleepEndTime: LocalDateTime
    ) {
        val notificationTime = mainSleepEndTime.plusMinutes(10)
        val now = LocalDateTime.now()

        if (notificationTime.isBefore(now)) {
            Log.d(TAG, "Notification time ($notificationTime) is in the past. Skipping schedule.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SleepCheckInNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Successfully scheduled sleep check-in notification:")
            Log.d(TAG, " - Main sleep end time: $mainSleepEndTime")
            Log.d(TAG, " - Target notification time: $notificationTime ($triggerTime)")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException: Exact alarm permission not granted. Falling back to non-exact alarm.", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.w(TAG, "Exception during scheduling: Falling back to standard set.", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelScheduledCheckIn(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SleepCheckInNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Successfully canceled scheduled sleep check-in notification.")
    }

    fun isCheckInNotificationScheduled(context: Context): Boolean {
        val intent = Intent(context, SleepCheckInNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
}
