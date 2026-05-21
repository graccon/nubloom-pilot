package com.sujin.nubloompilot.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.HealthSummaryRepository
import kotlinx.coroutines.launch

@Composable
fun SleepPage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val healthRepository = remember {
        HealthConnectRepository(context)
    }

    val healthSummaryRepository = remember {
        HealthSummaryRepository(healthRepository)
    }

    var hasPermission by remember {
        mutableStateOf(false)
    }

    var sleepResult by remember {
        mutableStateOf("아직 데이터 없음")
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        hasPermission = grantedPermissions.containsAll(
            healthRepository.healthPermissions
        )
    }

    LaunchedEffect(Unit) {
        hasPermission = healthRepository.hasHealthPermissions()
    }

    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "Health Connect 사용 가능: ${healthRepository.isHealthConnectAvailable()}"
        )

        Text(
            text = "건강 데이터 권한 허용됨: $hasPermission"
        )

        Button(
            onClick = {
                permissionLauncher.launch(
                    healthRepository.healthPermissions
                )
            }
        ) {
            Text("건강 데이터 권한 요청")
        }

        Spacer(modifier = Modifier.height(44.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val summary =
                            healthSummaryRepository.getLatestHealthSummary()

                        sleepResult =
                            if (summary == null) {
                                "최근 수면 없음"
                            } else {
                                """
                                최근 수면 요약

                                수면 시간: ${summary.sleepDurationMinutes / 60}시간 ${summary.sleepDurationMinutes % 60}분
                                깊은 수면: ${summary.deepSleepMinutes}분
                                기상 직후 HR: ${summary.wakeHeartRate ?: "없음"} bpm
                                HRV(RMSSD): ${summary.averageHrvMillis ?: "없음"} ms
                                걸음 수(24시간): ${summary.stepsLast24Hours}
                                """.trimIndent()
                            }

                    } catch (e: Exception) {
                        sleepResult = "에러: ${e.message}"
                    }
                }
            }
        ) {
            Text("건강 요약 데이터 읽기")
        }

        Text(
            text = sleepResult
        )
    }
}