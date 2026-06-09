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

@Composable
fun ShiftTypeSelector(
    selectedShift: ShiftInsightType,
    onShiftSelected: (ShiftInsightType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ShiftInsightType.entries.forEach { type ->
            val isSelected = selectedShift == type
            val label = when(type) {
                ShiftInsightType.DAY -> "Day"
                ShiftInsightType.EVENING -> "Evening"
                ShiftInsightType.NIGHT -> "Night"
                ShiftInsightType.OFF -> "Off"
            }
            
            Surface(
                onClick = { onShiftSelected(type) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Gray800 else Gray300.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Gray600
                    )
                }
            }
        }
    }
}
