package com.sujin.nubloompilot.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.components.QuestionTextField
import com.sujin.nubloompilot.components.SpeechBubble
import com.sujin.nubloompilot.components.WeightedColumnSection
import com.sujin.nubloompilot.ui.theme.NubloomPilotTheme
import com.sujin.nubloompilot.components.FloatingIcon
import com.sujin.nubloompilot.components.PrimaryButton


@Composable
fun OnboardingPage(
    onSubmit: (name: String, birthYear: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && birthYear.length == 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        WeightedColumnSection(
            weight = 5f,
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                FloatingIcon(
                    iconRes = R.drawable.icon_flower
                )
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "안녕하세요, 간호사님",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                SpeechBubble(
                    text = "실험에 참여해주셔서 감사합니다"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SpeechBubble(
                    text = "시작하기 전에 간단한 정보를 입력해주세요"
                )
            }
        }

        // INPUT
        WeightedColumnSection(
            weight = 6f,
            contentAlignment = Alignment.TopCenter
        ) {
            Column {

                QuestionTextField(
                    question = "이름을 입력해주세요",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "예) 홍길동"
                )

                Spacer(modifier = Modifier.height(28.dp))

                QuestionTextField(
                    question = "출생년도를 입력해주세요",
                    value = birthYear,
                    onValueChange = {
                        if (it.length <= 4 && it.all(Char::isDigit)) {
                            birthYear = it
                        }
                    },
                    placeholder = "예) 1990",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        }

        // BUTTON (1f)
        WeightedColumnSection(
            weight = 2f,
            contentAlignment = Alignment.Center
        ) {
            PrimaryButton(
                text = "시작할게요",
                onClick = {
                    onSubmit(name, birthYear.toInt())
                },
                enabled = isValid
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun OnboardingPagePreview() {
    NubloomPilotTheme {
        OnboardingPage(
            onSubmit = { _, _ -> }
        )
    }
}