package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.AppDiagnosticsState
import com.sujin.nubloompilot.ui.theme.*

@Composable
fun AppDiagnosticsSection(
    state: AppDiagnosticsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "앱 자가진단",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )

            Text(
                text = if (state.isReady) "앱 사용 준비 완료" else "확인 필요 항목 ${state.warningCount}개",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (state.isReady) Color(0xFF2E7D32) else DarkRed
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Gray300),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp, vertical = 14.dp)
            ) {
                DiagnosticRow("알림 권한", state.notificationEnabled)
                DiagnosticRow("Health Connect 설치", state.healthConnectInstalled)
                DiagnosticRow("Health Connect 권한", state.healthConnectPermissionGranted)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DiagnosticRow("오늘 수면 동기화", state.todaySleepSynced)
                if (state.lastSleepSyncTimeText != null) {
                    InfoRow("  - 마지막 동기화", state.lastSleepSyncTimeText)
                }
                
                DiagnosticRow("심박수 데이터", state.heartRateDataAvailable)
                DiagnosticRow("걸음 수 데이터", state.stepsDataAvailable)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DiagnosticRow("MCTQ 완료", state.mctqCompleted)
                DiagnosticRow("오늘 듀티 등록", state.todayDutyRegistered)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray700
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Gray800
        )
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Gray900
        )

        Text(
            text = if (isOk) "정상" else "확인 필요",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isOk) HighlightsYellow else Gray700,
            modifier = Modifier
                .background(
                    color = if (isOk) Gray900 else Gray200,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}