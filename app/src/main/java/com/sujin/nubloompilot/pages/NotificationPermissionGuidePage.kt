package com.sujin.nubloompilot.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.components.FloatingIcon
import com.sujin.nubloompilot.components.PrimaryButton
import com.sujin.nubloompilot.components.SpeechBubble
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray800

import androidx.compose.ui.tooling.preview.Preview
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme

@Composable
fun NotificationPermissionGuidePage(
    onPermissionGranted: () -> Unit,
    onPermissionSkipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTiramisuPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 권한 결과와 상관없이 흐름을 이어가기 위해 콜백 호출
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionSkipped()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        FloatingIcon(
            iconRes = R.drawable.icon_flower
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "수면 체크인 알림을 받아보세요",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        SpeechBubble(
            text = "정확한 분석을 위해 수면 상태 기록이 필요해요"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SpeechBubble(
            text = "기상 시점에 맞춰 알림을 보내드려요"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "NubloomPilot은 수면 체크인 알림을 통해 오늘의 수면 상태를 기록하고, 더 정확한 수면 리듬 분석을 도와드려요.",
            style = MaterialTheme.typography.bodyLarge,
            color = Gray800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "알림은 수면 체크인과 맞춤형 수면 관리 안내를 위해 사용됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "알림 권한 허용하기",
            onClick = {
                if (isTiramisuPlus) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        onPermissionGranted()
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    // Android 12 이하는 이미 권한이 있는 것으로 간주
                    onPermissionGranted()
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextButton(
            onClick = onPermissionSkipped,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "나중에 하기",
                color = Gray600,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(
    name = "Notification Permission Guide",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun NotificationPermissionGuidePagePreview() {
    NubloomPilotTheme {
        NotificationPermissionGuidePage(
            onPermissionGranted = {},
            onPermissionSkipped = {}
        )
    }
}
