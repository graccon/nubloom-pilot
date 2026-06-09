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

@Composable
fun MorningGloryDistributionRows(
    counts: Map<String, Int>,
    totalCount: Int
) {
    if (counts.isEmpty() || totalCount == 0) {
        Text(
            text = "데이터 준비 중",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
        return
    }

    val types = listOf("TYPE_1", "TYPE_2", "TYPE_3", "TYPE_4")
    
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        types.forEach { type ->
            val count = counts[type] ?: 0
            val percent = if (totalCount > 0) (count.toFloat() / totalCount * 100).toInt() else 0
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMorningGloryType(type),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Text(
                    text = "${count}건 · ${percent}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
            }
        }
    }
}
