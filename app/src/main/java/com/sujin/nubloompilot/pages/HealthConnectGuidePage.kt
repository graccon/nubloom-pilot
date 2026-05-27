package com.sujin.nubloompilot.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.components.FloatingIcon
import com.sujin.nubloompilot.components.PrimaryButton
import com.sujin.nubloompilot.components.SpeechBubble
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme

@Composable
fun HealthConnectGuidePage(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { HealthConnectRepository(context) }
    
    val isAvailable = remember { repository.isHealthConnectAvailable() }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(repository.healthPermissions)) {
            onNext()
        } else {
            showPermissionDeniedMessage = true
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
            text = "건강 데이터 연동",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        SpeechBubble(
            text = "더 정확한 분석을 위해 건강 데이터가 필요해요"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SpeechBubble(
            text = "갤럭시 워치 등에서 기록된 데이터가 활용됩니다"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isAvailable) {
                "연동을 승인하시면 자동으로 오늘 아침 수면 시간을 불러와 분석을 시작할 수 있습니다."
            } else {
                "현재 기기에서 Health Connect를 사용할 수 없습니다. 수동으로 데이터를 입력하여 진행할 수 있습니다."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Gray800,
            textAlign = TextAlign.Center
        )

        if (showPermissionDeniedMessage) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "모든 권한이 허용되지 않았습니다. 일부 기능이 제한될 수 있습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isAvailable) {
            PrimaryButton(
                text = "건강 데이터 연결하기",
                onClick = {
                    permissionLauncher.launch(repository.healthPermissions)
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        TextButton(
            onClick = onNext,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isAvailable) "나중에 하기" else "다음으로",
                color = Gray600,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
