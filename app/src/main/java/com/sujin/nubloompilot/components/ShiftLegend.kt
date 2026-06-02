package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.shared.models.ShiftTimingConfig
import com.sujin.nubloompilot.shared.models.ShiftType
import com.sujin.nubloompilot.shared.models.getTimeRange
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Primary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ShiftLegend(
    modifier: Modifier = Modifier,
    config: ShiftTimingConfig = ShiftTimingConfig.Default
) {
    val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
    val today = LocalDate.now()

    fun formatRange(type: ShiftType): String {
        val range = type.getTimeRange(today, config)
        val startText = range.startTime?.format(timeFormatter) ?: ""
        val endText = range.endTime?.format(timeFormatter) ?: ""
        return if (startText.isNotEmpty()) "$startText - $endText" else ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Gray500,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        ShiftLegendItem("D", "Day", formatRange(ShiftType.DAY))
        Spacer(modifier = Modifier.height(10.dp))
        ShiftLegendItem("E", "Evening", formatRange(ShiftType.EVENING))
        Spacer(modifier = Modifier.height(10.dp))
        ShiftLegendItem("N", "Night", formatRange(ShiftType.NIGHT))
    }
}

@Composable
private fun ShiftLegendItem(
    code: String,
    label: String,
    time: String
) {
    val backgroundColor = when (code) {
        "D" -> Color(0xFFA9C9EA)
        "E" -> Color(0xFFF4A249)
        "N" -> Color(0xFFEAB0D6)
        else -> Gray300
    }

    val textColor = when (code) {
        "D" -> Color(0xFF24496E)
        "E" -> Color(0xFF9A4E2A)
        "N" -> Color(0xFF8A2A8A)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 24.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$label  $time",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}