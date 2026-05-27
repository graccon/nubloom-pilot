package com.sujin.nubloompilot.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.components.SpriteAnimation
import com.sujin.nubloompilot.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OnboardingProcessingPage(
    onAction: suspend () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        try {
            onAction()
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = 3000L - elapsed
            if (remaining > 0) delay(remaining)
            onComplete()
        } catch (e: Exception) {
            errorMessage = "등록 중 오류가 발생했습니다: ${e.message}"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Gray800, Gray900, Gray900)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = { /* TODO: Retry logic if needed */ }) {
                Text("확인")
            }
        } else {
            SpriteAnimation(
                frames = listOf(
                    R.drawable.loading_1,
                    R.drawable.loading_2,
                    R.drawable.loading_3,
                    R.drawable.loading_4,
                    R.drawable.loading_5,
                    R.drawable.loading_4,
                    R.drawable.loading_3,
                    R.drawable.loading_2,
                ),
                modifier = Modifier.size(84.dp),
                frameDuration = 180L
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "선생님을 위한 맞춤 설정을 준비하고 있어요...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingProcessingPagePreview() {
    NubloomPilotTheme {
        OnboardingProcessingPage(onAction = {}, onComplete = {})
    }
}
