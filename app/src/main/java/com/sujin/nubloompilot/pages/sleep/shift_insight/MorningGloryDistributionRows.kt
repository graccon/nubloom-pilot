package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray800
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.sujin.nubloompilot.ui.theme.Gray200
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray700

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.ui.theme.Blue
import com.sujin.nubloompilot.ui.theme.Primary

@Composable
fun MorningGloryRankingBarChart(
    counts: Map<String, Int>,
    label: String,
    totalCount: Int
) {
    val items = listOf("TYPE_1", "TYPE_2", "TYPE_3", "TYPE_4")
        .map { type ->
            MorningGloryRankItem(
                type = type,
                label = getMorningGloryTypeLabel(type),
                count = counts[type] ?: 0
            )
        }
        .sortedWith(
            compareByDescending<MorningGloryRankItem> { it.count }
                .thenBy { it.type }
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        if (totalCount == 0) {
            Text(
                text = "아직 나팔꽃 타입 기록이 충분하지 않아요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            return@Column
        }

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
                text = "",
                style = MaterialTheme.typography.labelMedium,
                color = Gray800
            )
        }

        val maxCount = items.maxOfOrNull { it.count } ?: 0
        val topItems = items.filter { it.count == maxCount && it.count > 0 }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                val heightRatio = if (maxCount > 0) {
                    item.count.toFloat() / maxCount.toFloat()
                } else {
                    0f
                }

                val cardHeight = when {
                    item.count == 0 -> 120.dp
                    else -> (120 + 56 * heightRatio).dp
                }

                val rank = if (item.count == maxCount && maxCount > 0) {
                    1
                } else {
                    index + 1
                }

                MorningGloryVerticalRankCard(
                    rank = rank,
                    item = item,
                    totalCount = totalCount,
                    modifier = Modifier
                        .weight(1f)
                        .height(cardHeight)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = getMorningGloryRankingInsightText(topItems, totalCount),
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun MorningGloryVerticalRankCard(
    rank: Int,
    item: MorningGloryRankItem,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val ratio = if (totalCount > 0) {
        item.count.toFloat() / totalCount.toFloat()
    } else {
        0f
    }

    val percentage = (ratio * 100).toInt()

    val cardBackgroundColor = when (item.type) {
        "TYPE_1" -> Primary.copy(alpha = 0.18f)
        "TYPE_2" -> Primary.copy(alpha = 0.18f)
        else -> Blue.copy(alpha = 0.18f)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(cardBackgroundColor)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${rank}위",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )

        Spacer(modifier = Modifier.height(6.dp))

        val imageSize = if (rank == 1) 52.dp else 44.dp

        Image(
            painter = painterResource(id = getMorningGloryImageRes(item.type)),
            contentDescription = item.label,
            modifier = Modifier
                .width(imageSize)
                .height(imageSize)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "${item.count}건",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )

        Text(
            text = "${percentage}%",
            style = MaterialTheme.typography.bodySmall,
            color = Gray700
        )
    }
}

private fun getMorningGloryImageRes(type: String): Int {
    return when (type) {
        "TYPE_1" -> R.drawable.grass_grid_1
        "TYPE_2" -> R.drawable.grass_grid_2
        "TYPE_3" -> R.drawable.grass_grid_3
        "TYPE_4" -> R.drawable.grass_grid_4
        else -> R.drawable.grass_grid_1
    }
}


private data class MorningGloryRankItem(
    val type: String,
    val label: String,
    val count: Int
)

private fun getMorningGloryTypeLabel(type: String): String {
    return when (type) {
        "TYPE_1" -> "힘찬 나팔꽃"
        "TYPE_2" -> "가볍게 깨어난 나팔꽃"
        "TYPE_3" -> "덜 깬 나팔꽃"
        "TYPE_4" -> "지친 나팔꽃"
        else -> "알 수 없는 타입"
    }
}

private fun getMorningGloryRankingInsightText(
    topItems: List<MorningGloryRankItem>,
    totalCount: Int
): String {
    if (totalCount == 0 || topItems.isEmpty()) {
        return "아직 나팔꽃 타입 기록이 충분하지 않아요. 기록이 더 쌓이면 근무별 회복 패턴을 더 안정적으로 확인할 수 있어요."
    }

    val topLabels = topItems
        .map { getMorningGloryTypeSentenceLabel(it.type) }

    val topLabelText = when (topLabels.size) {
        1 -> "‘${topLabels.first()}’"
        2 -> "‘${topLabels[0]}’와 ‘${topLabels[1]}’"
        else -> topLabels.joinToString(", ") { "‘$it’" }
    }

    val baseSentence = "가장 많이 핀 나팔꽃은 $topLabelText 입니다."

    val suggestion = when {
        topItems.any { it.type == "TYPE_4" } -> {
            "회복이 부족한 날이 반복되었을 수 있어 다음 근무 전 수면 시간과 휴식 루틴을 먼저 점검해보세요."
        }

        topItems.any { it.type == "TYPE_3" } -> {
            "피로가 남은 상태로 하루를 시작한 경우가 있었을 수 있어 근무 전후 짧은 휴식이나 낮잠 시간을 확보해보세요."
        }

        topItems.any { it.type == "TYPE_2" } -> {
            "겉으로는 괜찮아도 회복이 완전히 충분하지 않았을 수 있어 다음 근무 전 수면 시간을 조금 더 확보해보세요."
        }

        topItems.any { it.type == "TYPE_1" } -> {
            "이 근무에서는 회복감이 비교적 잘 유지된 편이므로 비슷한 근무 전에는 현재 수면 루틴을 유지해보세요."
        }

        else -> {
            "기록이 더 쌓이면 근무별 회복 패턴을 더 안정적으로 확인할 수 있어요."
        }
    }

    return "$baseSentence $suggestion"
}

private fun getMorningGloryTypeSentenceLabel(type: String): String {
    return when (type) {
        "TYPE_1" -> "힘찬 나팔꽃"
        "TYPE_2" -> "가볍게 깨어난 나팔꽃"
        "TYPE_3" -> "덜 깬 나팔꽃"
        "TYPE_4" -> "지친 나팔꽃"
        else -> "알 수 없는 나팔꽃"
    }
}