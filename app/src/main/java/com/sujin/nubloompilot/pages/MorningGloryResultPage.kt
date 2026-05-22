package com.sujin.nubloompilot.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.ui.theme.*

@Composable
fun MorningGloryResultPage(
    participantName: String,
    type: MorningGloryType,
    onBackHome: () -> Unit
) {
    val resultInfo = when (type) {
        MorningGloryType.TYPE_1 -> ResultContent(
            title = "힘찬 나팔 불어라, 나팔꽃",
            description = "객관적인 수면 상태도 괜찮고, 몸 상태도 비교적 안정적이에요."
        )
        MorningGloryType.TYPE_2 -> ResultContent(
            title = "덜 깬 나팔꽃",
            description = "몸은 괜찮게 느껴질 수 있지만, 수면 데이터상 회복이 더 필요해 보여요."
        )
        MorningGloryType.TYPE_3 -> ResultContent(
            title = "가볍게 깨어난 나팔꽃",
            description = "수면 데이터는 괜찮지만, 지금 몸은 피로를 느끼고 있어요."
        )
        MorningGloryType.TYPE_4 -> ResultContent(
            title = "지친 나팔꽃",
            description = "객관적인 수면 상태와 몸 상태 모두 회복이 더 필요해 보여요."
        )
    }

    // Dark Gradient Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2C2C2C),
            Color(0xFF111111)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "오늘의 수면 나팔꽃이 폈어요!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )


            Spacer(modifier = Modifier.weight(0.5f))

            // Flower Placeholder
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(100.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TODO FLOWER\n(IMAGE)",
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            Text(
                text = resultInfo.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = resultInfo.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Gray300,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "홈으로 돌아가기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class ResultContent(
    val title: String,
    val description: String
)

@Preview(showBackground = true)
@Composable
fun MorningGloryResultPagePreview() {
    NubloomPilotTheme {
        MorningGloryResultPage(
            participantName = "수진",
            type = MorningGloryType.TYPE_1,
            onBackHome = {}
        )
    }
}
