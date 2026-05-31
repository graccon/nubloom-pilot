package com.sujin.nubloompilot.pages

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.sujin.nubloompilot.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningGloryTypeInfoPage(
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "수면 나팔꽃 유형",
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

            // (1) 유형 설명
            Text(
                text = "Q. 나의 수면 나팔꽃은 어떻게 정해질까요?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(R.drawable.info_img),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "수면 나팔꽃은 객관적인 수면 데이터와 선생님이 느끼는 주관적 피로도를 결합해 결정돼요."+
                        "\n객관적 수면 데이터는 스마트워치 수면 시간, 수면 단계, 기상 심박수 등을 바탕으로 계산됩니다." +
                        "주관적 피로도는 아침에 느끼는 피곤함과 컨디션을 바탕으로 평가됩니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800
            )
            Spacer(modifier = Modifier.height(36.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = Gray300
            )
            Spacer(modifier = Modifier.height(24.dp))

            // (2) 세부 유형 설명
            Text(
                text = "각 수면 유형을 알아볼까요?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(16.dp))

            MorningGloryType.entries.forEach { type ->
                TypeInfoCard(type = type)
                Spacer(modifier = Modifier.height(16.dp))
            }

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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TypeInfoCard(
    type: MorningGloryType,
) {
    val (keyword, title, description, detail, imageRes) = when (type) {
        MorningGloryType.TYPE_1 -> TypeInfoContent(
            keyword = "힘찬 나팔 불어라!",
            title = "나팔꽃",
            description = "수면 데이터와 현재 컨디션이\n모두 안정적인 상태",
            detail = "오늘 하루를 활기차게 시작하기 좋은 날이에요. 평소의 리듬을 유지하며 활동해보세요.",
            imageRes = R.drawable.grass_grid_1
        )

        MorningGloryType.TYPE_2 -> TypeInfoContent(
            keyword = "가볍게 깨어난",
            title = "나팔꽃",
            description = "컨디션은 괜찮지만 \n수면 데이터가 부족한 상태",
            detail = "몸은 괜찮게 느껴질 수 있지만 누적 피로가 있을 수 있어요. 중간중간 휴식을 섞어주세요.",
            imageRes = R.drawable.grass_grid_2
        )

        MorningGloryType.TYPE_3 -> TypeInfoContent(
            keyword = "덜 깬",
            title = "나팔꽃",
            description = "수면 데이터는 괜찮지만\n주관적 피로를 느끼는 상태",
            detail = "기록상 수면은 나쁘지 않지만 몸이 무겁게 느껴지는 날이에요. 천천히 몸을 깨워보세요.",
            imageRes = R.drawable.grass_grid_3
        )

        MorningGloryType.TYPE_4 -> TypeInfoContent(
            keyword = "웅크린",
            title = "나팔꽃",
            description = "수면 데이터와 현재 컨디션\n모두 회복이 필요한 상태",
            detail = "에너지가 낮은 상태예요. 오늘은 무리하지 말고 휴식과 컨디션 조절에 집중해보세요.",
            imageRes = R.drawable.grass_grid_4
        )
    }

    val tagBackgroundColor = when (type) {
        MorningGloryType.TYPE_1,
        MorningGloryType.TYPE_2 -> Primary.copy(alpha = 0.12f)

        MorningGloryType.TYPE_3,
        MorningGloryType.TYPE_4 -> Blue.copy(alpha = 0.12f)
    }

    val tagTextColor = when (type) {
        MorningGloryType.TYPE_1,
        MorningGloryType.TYPE_2 -> DarkRed

        MorningGloryType.TYPE_3,
        MorningGloryType.TYPE_4 -> DarkBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Gray400
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(92.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = tagBackgroundColor,
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = keyword,
                        modifier = Modifier.padding(
                            horizontal = 2.dp,
                            vertical = 1.dp
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = (-0.5).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = tagTextColor
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray900,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray800,
                )
            }
        }
    }
}

data class TypeInfoContent(
    val keyword: String,
    val title: String,
    val description: String,
    val detail: String,
    val imageRes: Int
)

@Preview(showBackground = true)
@Composable
fun MorningGloryTypeInfoPagePreview() {
    NubloomPilotTheme {
        MorningGloryTypeInfoPage()
    }
}
