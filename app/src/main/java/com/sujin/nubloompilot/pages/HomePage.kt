package com.sujin.nubloompilot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.SpiralTimeline
import com.sujin.nubloompilot.components.rememberCurrentTime
import com.sujin.nubloompilot.components.rememberRotatingMessage
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.ui.theme.Gray800

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
    val context = LocalContext.current
    var currentTime = rememberCurrentTime()
    val greetingMessage = rememberRotatingMessage()
    val healthConnectRepository = remember {
        HealthConnectRepository(context)
    }

    val isAvailable = remember {
        healthConnectRepository.isHealthConnectAvailable()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$participantName 선생님",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = greetingMessage,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray800
        )

        Spacer(modifier = Modifier.height(44.dp))

        Text(
            text = "Health Connect 사용 가능: $isAvailable",
            style = MaterialTheme.typography.bodyMedium
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