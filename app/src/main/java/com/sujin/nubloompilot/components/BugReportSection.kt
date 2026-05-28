package com.sujin.nubloompilot.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.ui.theme.DarkRed
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Gray900

//@Composable
//fun BugReportSection(
//    repository: BugReportRepository,
//    scope: CoroutineScope,
//    participantId: String,
//    participantName: String,
//    onSuccess: () -> Unit
//) {
//    var bugDescription by remember { mutableStateOf("") }
//    var isSubmitting by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 24.dp, vertical = 20.dp)
//    ) {
//        Text(
//            text = "버그 리포트",
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold,
//            color = Gray900
//        )
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = Gray300),
//            shape = MaterialTheme.shapes.medium
//        ) {
//            Column(modifier = Modifier.padding(20.dp, vertical = 18.dp)) {
//                Text(
//                    text = "앱 사용 중 불편했던 점이나 오류가 있었나요?\n사용하면서 느꼈던 경험을 자유롭게 공유해주세요.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Gray800
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                BulletItem("어떤 화면에서 발생했나요?")
//                BulletItem("어떤 행동 이후 발생했나요?")
//                BulletItem("어떤 문제가 있었나요?")
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(160.dp)
//                ) {
//                    OutlinedTextField(
//                        value = bugDescription,
//                        onValueChange = { bugDescription = it },
//                        placeholder = {
//                            Text(
//                                text = "예: 홈 화면에서 수면 체크인 버튼을 눌렀는데 다음 화면으로 넘어가지 않았어요.",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = Gray700
//                            )
//                        },
//                        modifier = Modifier.matchParentSize(),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedContainerColor = Color.White,
//                            unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
//                            focusedBorderColor = Gray900,
//                            unfocusedBorderColor = Gray400
//                        ),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//
//                    BugReportSendButton(
//                        isEnabled = bugDescription.isNotBlank() && !isSubmitting,
//                        isSubmitting = isSubmitting,
//                        onClick = {
//                            if (bugDescription.isBlank() || isSubmitting) return@BugReportSendButton
//
//                            isSubmitting = true
//                            scope.launch {
//                                try {
//                                    val report = BugReport(
//                                        participantId = participantId,
//                                        participantName = participantName,
//                                        description = bugDescription,
//                                        createdAt = Instant.now().toString()
//                                    )
//                                    repository.submitBugReport(report)
//                                    bugDescription = ""
//                                    onSuccess()
//                                    Log.d("BugReport", "Bug report submitted successfully")
//                                } catch (e: Exception) {
//                                    Log.e("BugReport", "Failed to submit bug report", e)
//                                } finally {
//                                    isSubmitting = false
//                                }
//                            }
//                        },
//                        modifier = Modifier.align(Alignment.BottomEnd)
//                    )
//                }
//            }
//        }
//    }
//}


@Composable
fun BugReportSection(
    isSubmitting: Boolean,
    onSubmit: (String) -> Unit
) {
    var bugDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "버그 리포트",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Gray300),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(20.dp, vertical = 18.dp)) {
                Text(
                    text = "앱 사용 중 불편했던 점이나 오류가 있었나요?\n사용하면서 느꼈던 경험을 자유롭게 공유해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                )

                Spacer(modifier = Modifier.height(12.dp))

                BulletItem("어떤 화면에서 발생했나요?")
                BulletItem("어떤 행동 이후 발생했나요?")
                BulletItem("어떤 문제가 있었나요?")

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    OutlinedTextField(
                        value = bugDescription,
                        onValueChange = { bugDescription = it },
                        placeholder = {
                            Text(
                                text = "예: 홈 화면에서 수면 체크인 버튼을 눌렀는데 다음 화면으로 넘어가지 않았어요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray700
                            )
                        },
                        modifier = Modifier.matchParentSize(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                            focusedBorderColor = Gray900,
                            unfocusedBorderColor = Gray400
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    BugReportSendButton(
                        isEnabled = bugDescription.isNotBlank() && !isSubmitting,
                        isSubmitting = isSubmitting,
                        onClick = {
                            val description = bugDescription.trim()
                            if (description.isBlank() || isSubmitting) return@BugReportSendButton

                            onSubmit(description)
                            bugDescription = ""
                        },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            modifier = Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = DarkRed
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkRed
        )
    }
}


@Composable
private fun BugReportSendButton(
    isEnabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(
                width = 64.dp,
                height = 40.dp
            )
            .padding(end = 2.dp, bottom = 2.dp)
            .clickable(
                enabled = isEnabled,
                onClick = onClick
            ),
        color = if (isEnabled) Gray900 else Gray500,
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 10.dp
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "버그 리포트 전송",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

