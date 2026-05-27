package com.sujin.nubloompilot.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sujin.nubloompilot.MainActivity
import com.sujin.nubloompilot.R
import android.util.Log



object SleepCheckInNotificationHelper {

    private const val CHANNEL_ID = "sleep_check_in_channel"
    private const val CHANNEL_NAME = "수면 체크인 알림"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "수면 후 체크인을 유도하는 알림입니다."
                enableVibration(true)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showSleepCheckInNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val hasPermission = ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.POST_NOTIFICATIONS

            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {

                return

            }

        }
        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "sleep_check_in")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("잘 주무셨나요?")
            .setContentText("오늘의 수면 상태를 체크하고 회복 계획을 받아보세요.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("오늘의 수면 상태를 체크하고 회복 계획을 받아보세요.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID,
                notification
            )
        } catch (e: SecurityException) {
            Log.w("SleepNotification", "알림 권한이 없어 알림을 표시하지 못했습니다.", e)
        }
    }
}