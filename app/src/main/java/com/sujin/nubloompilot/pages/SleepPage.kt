package com.sujin.nubloompilot.pages
import java.time.LocalDate
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.sujin.nubloompilot.repository.HealthConnectRepository
import kotlinx.coroutines.launch

@Composable
fun SleepPage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val healthRepository = remember {
        HealthConnectRepository(context)
    }

    var hasPermission by remember {
        mutableStateOf(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        hasPermission = grantedPermissions.containsAll(
            healthRepository.sleepPermissions
        )
    }

    LaunchedEffect(Unit) {
        hasPermission = healthRepository.hasSleepPermission()
    }

    Column (
        modifier = Modifier.padding(24.dp)
    ){
        Text(
            text = "Health Connect 사용 가능: ${healthRepository.isHealthConnectAvailable()}"
        )

        Text(
            text = "수면 권한 허용됨: $hasPermission"
        )

        Button(
            onClick = {
                permissionLauncher.launch(
                    healthRepository.sleepPermissions
                )
            }
        ) {
            Text("수면 데이터 권한 요청")
        }

        Spacer(modifier = Modifier.height(44.dp))

        var sleepResult by remember {
            mutableStateOf("아직 데이터 없음")
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        val sessions = healthRepository.readSleepSessions(
                            startDate = LocalDate.now().minusDays(7),
                            endDate = LocalDate.now().plusDays(1)
                        )

                        sleepResult =
                            if (sessions.isEmpty()) {
                                "최근 7일 수면 데이터 없음"
                            } else {
                                sessions.joinToString("\n\n") {
                                    "시작: ${it.startTime}\n종료: ${it.endTime}"
                                }
                            }

                    } catch (e: Exception) {
                        sleepResult = "에러: ${e.message}"
                    }
                }
            }
        ) {
            Text("최근 7일 수면 데이터 읽기")
        }

        Text(
            text = sleepResult
        )


    }
}