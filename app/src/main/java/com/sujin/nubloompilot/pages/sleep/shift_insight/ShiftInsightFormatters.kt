package com.sujin.nubloompilot.pages.sleep.shift_insight

import com.sujin.nubloompilot.models.ShiftInsightType
import androidx.compose.ui.graphics.Color
import java.util.Locale

fun getSeriesColor(shiftType: ShiftInsightType): Color {
    return when (shiftType) {
        ShiftInsightType.DAY -> Color(0xFF4A90E2)
        ShiftInsightType.EVENING -> Color(0xFFF5A623)
        ShiftInsightType.NIGHT -> Color(0xFF7B61FF)
        ShiftInsightType.OFF -> Color(0xFF7ED321)
    }
}

fun formatMinutesToHourMinute(minutes: Long?): String {
    if (minutes == null || minutes == 0L) return "데이터 준비 중"
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "${hours}시간 ${mins}분"
    } else {
        "${mins}분"
    }
}

// TODO: formatFatigueLevel (??) 여기에 괄호로 높음/ 낮음
fun formatFatigueLevel(value: Double?): String {
    if (value == null) return "데이터 준비 중"
    return String.format(Locale.getDefault(), "%.1f점", value)
}

fun formatMorningGloryType(type: String?): String {
    return when (type) {
        null -> "데이터 준비 중"
        "TYPE_1" -> "충분 회복형"
        "TYPE_2" -> "데이터상 회복 부족형"
        "TYPE_3" -> "체감 피로형"
        "TYPE_4" -> "회복 주의형"
        else -> type
    }
}

fun formatHourToTimeLabel(hour: Float): String {
    val totalMinutes = (hour * 60).toInt()
    val h = (totalMinutes / 60) % 24
    val m = totalMinutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", h, m)
}
