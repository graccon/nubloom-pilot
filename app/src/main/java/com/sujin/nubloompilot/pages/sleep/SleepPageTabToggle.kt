package com.sujin.nubloompilot.pages.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Gray800

@Composable
fun SleepPageTabToggle(
    selectedTab: SleepPageTab,
    onTabSelected: (SleepPageTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border((1.5).dp, Gray600, RoundedCornerShape(26.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. "최근 회복"
        val isRecentSelected = selectedTab == SleepPageTab.RECENT_RECOVERY
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (isRecentSelected) Gray800 else Color.Transparent,
                    RoundedCornerShape(22.dp)
                )
                .clickable { onTabSelected(SleepPageTab.RECENT_RECOVERY) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "최근 회복",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isRecentSelected) Color.White else Gray700,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 2. "근무별 인사이트"
        val isInsightSelected = selectedTab == SleepPageTab.SHIFT_INSIGHT
        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .background(
                    if (isInsightSelected) Gray800 else Color.Transparent,
                    RoundedCornerShape(22.dp)
                )
                .clickable { onTabSelected(SleepPageTab.SHIFT_INSIGHT) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "근무별 인사이트",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isInsightSelected) Color.White else Gray700,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
