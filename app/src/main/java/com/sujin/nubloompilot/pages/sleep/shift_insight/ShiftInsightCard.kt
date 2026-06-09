package com.sujin.nubloompilot.pages.sleep.shift_insight

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.ui.theme.Gray200
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray800
import androidx.compose.ui.graphics.Color
@Composable
fun ShiftInsightCard(
    title: String,
    description: String,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding( horizontal = 16.dp, vertical = 0.dp)) {
            if (content != null) {
                content()
            }
        }
    }
}
