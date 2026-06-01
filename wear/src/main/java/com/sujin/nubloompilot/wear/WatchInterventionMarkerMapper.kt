package com.sujin.nubloompilot.wear

import androidx.compose.ui.graphics.Color
import com.sujin.nubloompilot.shared.models.TimelineMarker
import com.sujin.nubloompilot.shared.models.WatchInterventionPayload
import com.sujin.nubloompilot.wear.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object WatchInterventionMarkerMapper {

    fun map(
        interventions: List<WatchInterventionPayload>,
        referenceDateString: String?
    ): List<TimelineMarker> {
        val referenceDate = runCatching { 
            LocalDate.parse(referenceDateString) 
        }.getOrElse { LocalDate.now() }

        return interventions.mapNotNull { intervention ->
            val start = runCatching { 
                LocalDateTime.parse(intervention.startTime) 
            }.getOrNull() ?: return@mapNotNull null

            val end = runCatching {
                LocalDateTime.parse(intervention.endTime)
            }.getOrNull() ?: return@mapNotNull null

            if (end.isBefore(start)) return@mapNotNull null

            val targetTime = if (intervention.type == "CAFFEINE") {
                val durationSeconds = Duration.between(start, end).seconds
                start.plusSeconds(durationSeconds / 2)
            } else {
                start
            }

            val absoluteHour = toAbsoluteHour(targetTime, referenceDate)

            TimelineMarker(
                id = "${intervention.type}-${intervention.startTime}",
                absoluteHour = absoluteHour,
                color = getMarkerColor(intervention.type),
                label = getShortLabel(intervention.title),
                iconRes = getIconRes(intervention.type)
            )
        }
    }

    private fun toAbsoluteHour(dateTime: LocalDateTime, referenceDate: LocalDate): Float {
        val daysBetween = ChronoUnit.DAYS.between(referenceDate, dateTime.toLocalDate())
        return (daysBetween * 24f) + dateTime.hour + dateTime.minute / 60f
    }

    private fun getMarkerColor(type: String): Color {
        return when (type) {
            "MAIN_SLEEP" -> Color(0xFF1E3A5F)
            "SLEEP_PREPARATION" -> Color(0xFF6A5ACD)
            "CAFFEINE" -> Color(0xFF795548)
            "NAP" -> Color(0xFF3F51B5)
            "LIGHT" -> Color(0xFFFFEB3B)
            else -> Color.Gray
        }
    }

    private fun getIconRes(type: String): Int {
        return when (type) {
            "CAFFEINE" -> R.drawable.ic_coffee
            "NAP" -> R.drawable.ic_sleep_face
            "LIGHT" -> R.drawable.ic_light
            "MAIN_SLEEP" -> R.drawable.ic_sleep_face
            "SLEEP_PREPARATION" -> R.drawable.ic_prepare
            else -> R.drawable.ic_intervention
        }
    }

    private fun getShortLabel(title: String): String {
        return when {
            title.contains("목표 수면") -> "수면"
            title.contains("수면 환경") -> "준비"
            title.contains("카페인") -> "카페인"
            title.contains("낮잠") -> "낮잠"
            title.contains("빛") -> "빛"
            else -> title.take(3)
        }
    }
}
