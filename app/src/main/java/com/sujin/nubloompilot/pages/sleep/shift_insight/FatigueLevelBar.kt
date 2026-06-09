package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray700
import com.sujin.nubloompilot.ui.theme.Gray800
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.sujin.nubloompilot.R

@Composable
fun FatigueLevelBar(
    fatigueLevel: Double?,
    label: String,
    maxLevel: Double = 7.0
) {
    val progress = if (fatigueLevel != null) {
        (fatigueLevel / maxLevel).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Gray700
            )

            Text(
                text = formatFatigueLevel(fatigueLevel),
                style = MaterialTheme.typography.labelMedium,
                color = Gray800
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FatigueSegmentedBar(
            fatigueLevel = fatigueLevel
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FatigueIcon(
                resId = R.drawable.sleep_good,
                contentDescription = "낮은 피로도"
            )

            FatigueIcon(
                resId = R.drawable.sleep_normal,
                contentDescription = "보통 피로도"
            )

            FatigueIcon(
                resId = R.drawable.sleep_bad,
                contentDescription = "높은 피로도"
            )
        }
        Spacer(modifier = Modifier.height(4.dp))


        Text(
            text = getFatigueInterpretation(fatigueLevel),
            style = MaterialTheme.typography.bodyMedium,
            color = Gray700,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

private fun getFatigueInterpretation(
    fatigueLevel: Double?
): String {
    if (fatigueLevel == null) {
        return "피로도 기록이 아직 충분하지 않아요. 체크인이 쌓이면 근무별 피로 흐름을 더 정확히 볼 수 있어요."
    }

    return when {
        fatigueLevel < 2.5 -> {
            "전반적으로 주관적으로 느끼는 피로도가 낮은 편이에요. 현재 근무 패턴에서는 수면 후 회복감이 비교적 잘 유지된 것으로 볼 수 있어요."
        }

        fatigueLevel < 4.0 -> {
            "주관적으로 느끼는 피로도가 비교적 안정적인 편이에요. 다만 근무 전환이나 수면 시간이 짧은 날에는 피로가 달라질 수 있어요."
        }

        fatigueLevel < 5.5 -> {
            "보통보다 주관적으로 느끼는 피로가 조금 높게 나타났어요. 수면 시간이 충분했더라도 근무 부담이나 수면의 질이 영향을 줬을 수 있어요."
        }

        else -> {
            "주관적으로 느끼는 피로도가 높은 편이에요. 회복 시간을 더 신경 쓰고, 다음 근무 전 수면 확보와 휴식 루틴을 확인해보는 것이 좋아요."
        }
    }
}

@Composable
private fun FatigueSegmentedBar(
    fatigueLevel: Double?,
    maxLevel: Int = 7
) {
    val filledLevel = fatigueLevel
        ?.toInt()
        ?.coerceIn(0, maxLevel)
        ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (level in 1..maxLevel) {
            val isFilled = level <= filledLevel

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isFilled) Gray800 else Gray300
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isFilled) Gray300 else Gray700
                )
            }
        }
    }
}

@Composable
private fun FatigueIcon(
    resId: Int,
    contentDescription: String
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = Modifier
            .width(40.dp)
            .height(36.dp)
    )
}