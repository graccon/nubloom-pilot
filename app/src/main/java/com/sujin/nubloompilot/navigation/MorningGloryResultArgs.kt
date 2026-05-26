package com.sujin.nubloompilot.navigation

import androidx.navigation.NavBackStackEntry
import com.sujin.nubloompilot.models.MorningGloryType

data class MorningGloryResultArgs(
    val type: MorningGloryType,
    val endTime: String,
    val duration: Long,
    val heartRate: Long,
    val fatigueLevel: Int
) {
    companion object {
        fun from(backStackEntry: NavBackStackEntry): MorningGloryResultArgs {
            val arguments = backStackEntry.arguments
            
            val typeStr = arguments?.getString("type")
            val type = runCatching { 
                MorningGloryType.valueOf(typeStr ?: MorningGloryType.TYPE_1.name) 
            }.getOrElse { MorningGloryType.TYPE_1 }

            val endTime = arguments?.getString("endTime") ?: "NONE"
            val duration = arguments?.getString("duration")?.toLongOrNull() ?: 0L
            val heartRate = arguments?.getString("heartRate")?.toLongOrNull() ?: -1L
            val fatigueLevel = arguments?.getString("fatigueLevel")?.toIntOrNull() ?: 0

            return MorningGloryResultArgs(
                type = type,
                endTime = endTime,
                duration = duration,
                heartRate = heartRate,
                fatigueLevel = fatigueLevel
            )
        }
    }
}
