package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray700

@Composable
fun ShiftTypeSelector(
    onShiftSelected: (String) -> Unit
) {
    val shiftTypes = listOf("D", "E", "N", "O")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        shiftTypes.forEach { shift ->
            ShiftTypeButton(
                shift = shift,
                onClick = {
                    onShiftSelected(shift)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ShiftTypeButton(
    shift: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (shift) {
        "D" -> Color(0xFFA9C9EA)
        "E" -> Color(0xFFF4A249)
        "N" -> Color(0xFFEAB0D6)
        else -> Gray300
    }

    val textColor = when (shift) {
        "D" -> Color(0xFF24496E)
        "E" -> Color(0xFF9A4E2A)
        "N" -> Color(0xFF8A2A8A)
        else -> Gray700
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shift,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}