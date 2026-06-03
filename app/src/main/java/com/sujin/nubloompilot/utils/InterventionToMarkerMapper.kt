package com.sujin.nubloompilot.utils

import androidx.compose.ui.graphics.Color
import com.sujin.nubloompilot.R
import com.sujin.nubloompilot.models.InterventionActionType
import com.sujin.nubloompilot.models.InterventionType
import com.sujin.nubloompilot.models.SavedSleepIntervention
import com.sujin.nubloompilot.shared.models.TimelineMarker
import com.sujin.nubloompilot.ui.theme.Primary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object InterventionToMarkerMapper {

    fun map(
        interventions: List<SavedSleepIntervention>,
        referenceDate: LocalDate
    ): List<TimelineMarker> {
        return interventions.mapNotNull { intervention ->
            val start = runCatching { LocalDateTime.parse(intervention.startTime) }.getOrNull() ?: return@mapNotNull null
            val end = runCatching { LocalDateTime.parse(intervention.endTime) }.getOrNull()
            
            // For caffeine, use the midpoint of the intervention window for the marker (display point)
            val targetTime = if (intervention.type == InterventionType.CAFFEINE.name) {
                if (end != null) {
                    val halfDurationMinutes = ChronoUnit.MINUTES.between(start, end) / 2
                    start.plusMinutes(halfDurationMinutes)
                } else {
                    start
                }
            } else {
                start
            }

            val absoluteHour = targetTime.toAbsoluteHour(referenceDate)
            val endAbsoluteHour = end?.toAbsoluteHour(referenceDate)

            android.util.Log.d("InterventionMapper", "Mapped: ${intervention.title} / Start: $absoluteHour / End: $endAbsoluteHour")

            TimelineMarker(
                id = intervention.startTime,
                absoluteHour = absoluteHour,
                color = getMarkerColor(intervention.type, intervention.actionType),
                label = getShortLabel(intervention.title),
                iconRes = getIconRes(intervention.type, intervention.actionType),
                endAbsoluteHour = endAbsoluteHour
            )
        }
    }

    private fun LocalDateTime.toAbsoluteHour(referenceDate: LocalDate): Float {
        val daysBetween = ChronoUnit.DAYS.between(referenceDate, this.toLocalDate())
        return (daysBetween * 24f) + this.hour + this.minute / 60f
    }

    private fun getMarkerColor(type: String, actionType: String): Color {
        return when (type) {
            InterventionType.MAIN_SLEEP.name -> Color(0xFF1E3A5F)

            InterventionType.SLEEP_PREPARATION.name -> Color(0xFF6A5ACD)

            InterventionType.CAFFEINE.name -> {
                if (actionType == InterventionActionType.DO.name) Primary else Color(0xFF795548)
            }

            InterventionType.NAP.name -> Color(0xFF3F51B5)

            InterventionType.LIGHT.name -> {
                if (actionType == InterventionActionType.DO.name) Color(0xFFFFEB3B) else Color(0xFF212121)
            }

            else -> Primary
        }
    }

    fun getIconRes(type: String, actionType: String): Int {
        // MUST return PNG resource IDs, because ImageBitmap.imageResource crashes on XML VectorDrawables
        return when (type) {
            InterventionType.CAFFEINE.name -> R.drawable.ic_coffee // Placeholder PNG
            InterventionType.NAP.name -> R.drawable.ic_sleep_face // PNG
            InterventionType.LIGHT.name -> R.drawable.ic_light // Placeholder PNG
            InterventionType.MAIN_SLEEP.name -> R.drawable.ic_sleep_face
            InterventionType.SLEEP_PREPARATION.name -> R.drawable.ic_prepare
            else -> R.drawable.ic_intervention // Placeholder PNG
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
