package com.sujin.nubloompilot.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * BroadcastReceiver that handles the alarm for sleep check-in notifications.
 */
class SleepCheckInNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SleepCheckInReceiver", "Sleep check-in notification alarm received")
        val hasNotificationPermission =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        // Trigger the notification display
        SleepCheckInNotificationHelper.showSleepCheckInNotification(context)
    }
}
