package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.models.ShiftInsightType
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.shared.models.ShiftType
import com.sujin.nubloompilot.ui.theme.Gray100
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Night
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.getValue
@Composable
fun ShiftTypeSelector(
    selectedShift: ShiftInsightType,
    onShiftSelected: (ShiftInsightType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ShiftInsightType.entries.forEach { type ->
            val isSelected = selectedShift == type
            val yOffset by animateDpAsState(
                targetValue = if (isSelected) (-10).dp else 0.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "shift_selector_y_offset"
            )
            val label = when(type) {
                ShiftInsightType.DAY -> "Day"
                ShiftInsightType.EVENING -> "Evening"
                ShiftInsightType.NIGHT -> "Night"
                ShiftInsightType.OFF -> "Off"
            }
            val shiftColor = when (type) {
                ShiftInsightType.OFF -> Gray500
                ShiftInsightType.NIGHT -> Night
                else -> type.toShiftType().color
            }

            Surface(
                onClick = { onShiftSelected(type) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) {
                    shiftColor
                } else {
                    shiftColor.copy(alpha = 0.4f)
                },
                modifier = Modifier
                    .weight(1f)
                    .offset(y = yOffset)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Gray800 else shiftColor
                    )
                }
            }
        }
    }
}

private fun ShiftInsightType.toShiftType(): ShiftType {
    return when (this) {
        ShiftInsightType.DAY -> ShiftType.DAY
        ShiftInsightType.EVENING -> ShiftType.EVENING
        ShiftInsightType.NIGHT -> ShiftType.NIGHT
        ShiftInsightType.OFF -> ShiftType.OFF
    }
}
