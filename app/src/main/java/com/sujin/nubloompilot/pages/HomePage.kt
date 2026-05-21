package com.sujin.nubloompilot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.SpiralTimeline
import com.sujin.nubloompilot.components.rememberCurrentTime
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme

@Composable
fun HomePage(
    participantName: String,
    yesterdayShift: String?,
    todayShift: String?,
    tomorrowShift: String?,
    dayAfterTomorrowShift: String?,
    onCheckInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime = rememberCurrentTime()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "안녕하세요, $participantName 선생님",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "현재 시간: ${currentTime.hour}:${currentTime.minute.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.titleMedium
        )


        Text(
            text = "오늘 근무: ${formatShift(todayShift)}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "내일 근무: ${formatShift(tomorrowShift)}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(44.dp))

        SpiralTimeline(
            yesterdayShift = yesterdayShift,
            todayShift = todayShift,
            tomorrowShift = tomorrowShift,
            dayAfterTomorrowShift = dayAfterTomorrowShift,
            currentTime = currentTime
        )
    }
}

private fun formatShift(shift: String?): String {
    return when (shift) {
        "D" -> "D · Day"
        "E" -> "E · Evening"
        "N" -> "N · Night"
        "O" -> "O · Off"
        else -> "입력 없음"
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    NubloomPilotTheme {
        HomePage(
            participantName = "수진",
            yesterdayShift = "N",
            todayShift = "N",
            tomorrowShift = "N",
            dayAfterTomorrowShift = "E",
            onCheckInClick = {}
        )
    }
}