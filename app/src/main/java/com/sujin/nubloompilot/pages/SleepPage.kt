package com.sujin.nubloompilot.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sujin.nubloompilot.notifications.SleepCheckInNotificationHelper

@Composable
fun SleepPage() {
    val context = LocalContext.current
    var permissionStatus by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                "이 버전에서는 알림 권한 요청이 필요하지 않습니다"
            } else {
                "알림 권한 미확인"
            }
        )
    }
    var notificationRequestStatus by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = if (isGranted) "알림 권한 허용됨" else "알림 권한 거부됨"
    }

    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "알림 권한 테스트",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = permissionStatus)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permissionStatus = "이 버전에서는 알림 권한 요청이 필요하지 않습니다"
                }
            }
        ) {
            Text("알림 권한 요청")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isNotificationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionStatus == "알림 권한 허용됨"
        } else {
            true
        }

        Button(
            onClick = {
                val hasNotificationPermission =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                if (hasNotificationPermission) {
                    SleepCheckInNotificationHelper.showSleepCheckInNotification(context)
                    notificationRequestStatus = "수면 체크인 알림을 요청했습니다"
                } else {
                    notificationRequestStatus = "알림 권한이 없어 알림을 표시할 수 없습니다"
                }
            },
            enabled = isNotificationEnabled
        ) {
            Text("수면 체크인 알림 테스트")
        }

        if (notificationRequestStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notificationRequestStatus,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
