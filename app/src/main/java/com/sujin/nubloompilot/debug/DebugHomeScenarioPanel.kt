package com.sujin.nubloompilot.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sujin.nubloompilot.ui.theme.Gray300
import com.sujin.nubloompilot.ui.theme.Gray500
import com.sujin.nubloompilot.ui.theme.Gray800
import com.sujin.nubloompilot.ui.theme.Primary

@Composable
fun DebugHomeScenarioPanel(
    state: DemoHomeControlState,
    onStateChange: (DemoHomeControlState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, Primary, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "DEMO ONLY",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Demo Scenario Control",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Chronotype
        DemoButtonGroup(
            label = "크로노타입",
            options = listOf(
                DemoChronotype.MORNING to "아침형",
                DemoChronotype.INTERMEDIATE to "중간형",
                DemoChronotype.EVENING to "저녁형"
            ),
            selected = state.chronotype,
            onSelected = { onStateChange(state.copy(chronotype = it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Condition
        DemoButtonGroup(
            label = "현재 상태",
            options = listOf(
                DemoCondition.TIRED to "피곤한",
                DemoCondition.FRESH to "쌩쌩한"
            ),
            selected = state.condition,
            onSelected = { onStateChange(state.copy(condition = it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Work Scenario
        DemoButtonGroup(
            label = "근무 상황",
            options = listOf(
                DemoWorkScenario.FIRST_NIGHT to "첫 나이트",
                DemoWorkScenario.CONSECUTIVE_DAY to "연속 데이"
            ),
            selected = state.workScenario,
            onSelected = { onStateChange(state.copy(workScenario = it)) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Enabled Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "데모 모드 활성화",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = state.enabled,
                onCheckedChange = { onStateChange(state.copy(enabled = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Gray500,
                    uncheckedBorderColor = Gray500
                )
            )
        }
    }
}

@Composable
private fun <T> DemoButtonGroup(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Gray800)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, text) ->
                val isSelected = value == selected
                Button(
                    onClick = { onSelected(value) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Primary else Gray300,
                        contentColor = if (isSelected) Color.White else Gray800
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = text, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
