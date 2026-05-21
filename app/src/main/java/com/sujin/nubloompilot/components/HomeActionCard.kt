package com.sujin.nubloompilot.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.ui.theme.DarkBlue
import com.sujin.nubloompilot.ui.theme.DarkRed
import androidx.compose.foundation.border
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray400
import com.sujin.nubloompilot.ui.theme.Gray800
import androidx.compose.foundation.clickable

enum class SleepReportState {
    NONE,
    TYPE_1,
    TYPE_2,
    TYPE_3,
    TYPE_4
}

data class HomeActionCardData(
    @DrawableRes val imageRes: Int,
    @DrawableRes val iconRes: Int,
    val keyword: String,
    val description: String,
    val textColor: Color
)

fun SleepReportState.toHomeActionCardData(): HomeActionCardData {
    return when (this) {
        SleepReportState.NONE -> HomeActionCardData(
            imageRes = R.drawable.sleep_flower,
            iconRes = R.drawable.ic_check,
            keyword = "수면 체크하기",
            description = "오늘 수면 보러가기 >",
            textColor = Gray800
        )

        SleepReportState.TYPE_1 -> HomeActionCardData(
            imageRes = R.drawable.type_1,
            iconRes = R.drawable.icon_flower,
            keyword = "오늘의 나팔꽃",
            description = "힘찬 나팔 불어라, 나팔꽃! >",
            textColor = DarkRed
        )

        SleepReportState.TYPE_2 -> HomeActionCardData(
            imageRes = R.drawable.type_2,
            iconRes = R.drawable.icon_flower,
            keyword = "오늘의 나팔꽃",
            description = "가볍게 깨어난 나팔꽃! >",
            textColor = DarkRed
        )

        SleepReportState.TYPE_3 -> HomeActionCardData(
            imageRes = R.drawable.type_3,
            iconRes = R.drawable.icon_flower,
            keyword = "오늘의 나팔꽃",
            description = "덜 깬 나팔꽃! >",
            textColor = DarkBlue
        )

        SleepReportState.TYPE_4 -> HomeActionCardData(
            imageRes = R.drawable.type_4,
            iconRes = R.drawable.icon_flower,
            keyword = "오늘의 나팔꽃",
            description = "지친 나팔꽃! >",
            textColor = DarkBlue
        )
    }
}

@Composable
fun HomeActionCard(
    state: SleepReportState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    HomeActionCard(
        data = state.toHomeActionCardData(),
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun HomeActionCard(
    data: HomeActionCardData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val cardShape = RoundedCornerShape(20.dp)
    val topHeight = 100.dp
    val bottomHeight = 50.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(topHeight + bottomHeight)
            .clip(cardShape)
            .border(
                width = 1.5.dp,
                color = Gray400,
                shape = cardShape
            )
            .background(Gray300)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topHeight)
        ) {
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = data.keyword,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 12.dp)
                    .background(
                        color = Gray300,
                        shape = RoundedCornerShape(40.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = data.iconRes),
                        contentDescription = null,
                        tint = data.textColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = data.keyword,
                        style = MaterialTheme.typography.bodyLarge,
                        color =  data.textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomHeight)
                .background(Color(0xFFEFEFEF))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = data.description,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = data.textColor
            )
        }
    }
}