package com.sujin.nubloompilot.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInNotificationHelpPage(
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "체크인 알림 안내",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Gray100
                )
            )
        },
        containerColor = Gray100
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            HelpSectionCard(
                title = "수면 후 알림이 바로 뜨지 않을 수 있어요",
                imageRes = R.drawable.health_connect,
                content = "수면 체크인 알림은 Health Connect에서 수면 종료 데이터가 확인된 뒤 예약됩니다. 스마트워치나 삼성헬스의 동기화가 늦어지면 앱이 수면 종료를 바로 알지 못할 수 있어요."
            )

            Spacer(modifier = Modifier.height(16.dp))

            HelpSectionCard(
                title = "이럴 때는 이렇게 해보세요"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HelpStepItem("*) 데이터가 들어올 때까지 조금만 기다려 주세요.")
                    HelpStepItem("1) 워치와 휴대폰이 동기화되었는지 확인해주세요.")
                    HelpStepItem("2) 삼성헬스 또는 Health Connect에 수면 기록이 들어왔는지 확인해주세요.")
                    HelpStepItem("3) 내 정보 > 앱 자가진단에서 알림 권한과 체크인 알림 예약 상태를 확인해주세요.")
                    HelpStepItem("4) 필요하면 앱 안의 알림 테스트 버튼으로 알림이 정상적으로 뜨는지 확인해주세요.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HelpSectionCard(
                content = "알림이 뜨지 않았더라도 수면 데이터가 동기화되면 앱에서 수면 체크인을 진행할 수 있습니다.",
                contentColor = Gray800
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gray900),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "돌아가기",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HelpSectionCard(
    title: String? = null,
    content: String? = null,
    contentColor: Color = Gray700,
    imageRes: Int? = null,
    extraContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )

                if (content != null || extraContent != null || imageRes != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            imageRes?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (content != null) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }

            extraContent?.invoke()
        }
    }
}
@Composable
private fun HelpStepItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Gray700,
        lineHeight = 20.sp
    )
}

@Preview(showBackground = true)
@Composable
fun CheckInNotificationHelpPagePreview() {
    NubloomPilotTheme {
        CheckInNotificationHelpPage()
    }
}
