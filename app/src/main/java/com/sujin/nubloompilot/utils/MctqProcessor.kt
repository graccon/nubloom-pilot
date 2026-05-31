package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import com.sujin.nubloompilot.shared.models.ShiftType
import kotlin.math.abs

object MctqProcessor {

    fun process(responses: List<MCTQShiftResponse>): MctqBaselineProfile? {
        if (responses.size < 6) return null

        val blockResults = responses.map { computeBlock(it) }

        // Group by shift type and workday status
        val dayWork = blockResults.find { it.shiftType == ShiftType.DAY && it.isWorkday } ?: return null
        val dayFree = blockResults.find { it.shiftType == ShiftType.DAY && !it.isWorkday } ?: return null
        val eveWork = blockResults.find { it.shiftType == ShiftType.EVENING && it.isWorkday } ?: return null
        val eveFree = blockResults.find { it.shiftType == ShiftType.EVENING && !it.isWorkday } ?: return null
        val nightWork = blockResults.find { it.shiftType == ShiftType.NIGHT && it.isWorkday } ?: return null
        val nightFree = blockResults.find { it.shiftType == ShiftType.NIGHT && !it.isWorkday } ?: return null

        val avgWorkSD = (dayWork.sleepDurationMinutes + eveWork.sleepDurationMinutes + nightWork.sleepDurationMinutes) / 3
        val avgFreeSD = (dayFree.sleepDurationMinutes + eveFree.sleepDurationMinutes + nightFree.sleepDurationMinutes) / 3
        
        // Use unweighted average of all 6 blocks for baseline profile
        // TODO: Replace with schedule-weighted ØSD when actual shift schedule counts are available.
        val unweightedAvgSD = blockResults.map { it.sleepDurationMinutes }.average().toInt()

        // Social Jetlag calculation (Circular distance)
        val sjDay = calculateCircularDifference(dayWork.midSleepTime, dayFree.midSleepTime)
        val sjEve = calculateCircularDifference(eveWork.midSleepTime, eveFree.midSleepTime)
        val sjNight = calculateCircularDifference(nightWork.midSleepTime, nightFree.midSleepTime)

        // Chronotype calculation (MSFEsc)
        // MSFEsc = MSFE - (SDFE - SDweek) / 2
        val msfe = timeToMinutes(eveFree.midSleepTime)
        val sdfe = eveFree.sleepDurationMinutes
        val sdwe = eveWork.sleepDurationMinutes
        
        val msfEscMinutes = if (sdfe > sdwe) {
            (msfe - (sdfe - unweightedAvgSD) / 2 + 1440) % 1440
        } else {
            msfe
        }
        val msfEsc = minutesToTime(msfEscMinutes)

        // TODO: Move chronotype labeling to a dedicated classifier/UI logic
        val chronotypeLabel = "TODO: Classification"

        return MctqBaselineProfile(
            blockResults = blockResults,
            dayWorkSleepDurationMinutes = dayWork.sleepDurationMinutes,
            dayFreeSleepDurationMinutes = dayFree.sleepDurationMinutes,
            eveningWorkSleepDurationMinutes = eveWork.sleepDurationMinutes,
            eveningFreeSleepDurationMinutes = eveFree.sleepDurationMinutes,
            nightWorkSleepDurationMinutes = nightWork.sleepDurationMinutes,
            nightFreeSleepDurationMinutes = nightFree.sleepDurationMinutes,
            dayWorkMidSleep = dayWork.midSleepTime,
            dayFreeMidSleep = dayFree.midSleepTime,
            eveningWorkMidSleep = eveWork.midSleepTime,
            eveningFreeMidSleep = eveFree.midSleepTime,
            nightWorkMidSleep = nightWork.midSleepTime,
            nightFreeMidSleep = nightFree.midSleepTime,
            averageWorkSleepDurationMinutes = avgWorkSD,
            averageFreeSleepDurationMinutes = avgFreeSD,
            unweightedAverageSleepDurationMinutes = unweightedAvgSD,
            workFreeSleepGapMinutes = avgFreeSD - avgWorkSD,
            socialJetlagDayMinutes = sjDay,
            socialJetlagEveningMinutes = sjEve,
            socialJetlagNightMinutes = sjNight,
            chronotypeMsfEsc = msfEsc,
            chronotypeMsfEscMinutes = msfEscMinutes,
            chronotypeLabel = chronotypeLabel
        )
    }

    private fun computeBlock(response: MCTQShiftResponse): MctqBlockComputedResult {
        val onset = (timeToMinutes(response.tryToSleepTime) + response.sleepLatencyMinutes) % 1440
        val end = timeToMinutes(response.wakeUpTime)
        
        val duration = (end - onset + 1440) % 1440
        val midSleep = (onset + duration / 2) % 1440
        
        val bed = timeToMinutes(response.bedTime)
        val timeInBed = (end - bed + 1440) % 1440
        
        val napDuration = if (response.napTaken && response.napStartTime != null && response.napEndTime != null) {
            (timeToMinutes(response.napEndTime) - timeToMinutes(response.napStartTime) + 1440) % 1440
        } else 0

        // Validation logic
        var isValid = true
        var warningReason: String? = null
        if (duration < 60) {
            isValid = false
            warningReason = "수면 시간이 너무 짧습니다 (60분 미만)."
        } else if (duration > 900) {
            isValid = false
            warningReason = "수면 시간이 너무 깁니다 (15시간 초과)."
        }

        return MctqBlockComputedResult(
            shiftType = response.shiftType,
            isWorkday = response.isWorkday,
            sleepOnsetTime = minutesToTime(onset),
            sleepEndTime = response.wakeUpTime,
            sleepDurationMinutes = duration,
            midSleepTime = minutesToTime(midSleep),
            timeInBedMinutes = timeInBed,
            napDurationMinutes = napDuration,
            totalSleepDurationWithNapMinutes = duration + napDuration,
            alarmUsed = response.alarmUsed,
            canChooseSleepFreely = response.canChooseSleepFreely,
            isValid = isValid,
            warningReason = warningReason
        )
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }

    private fun minutesToTime(minutes: Int): String {
        val h = (minutes / 60) % 24
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }

    private fun calculateCircularDifference(time1: String, time2: String): Int {
        val m1 = timeToMinutes(time1)
        val m2 = timeToMinutes(time2)
        val diff = abs(m1 - m2)
        return if (diff > 720) 1440 - diff else diff
    }
}
