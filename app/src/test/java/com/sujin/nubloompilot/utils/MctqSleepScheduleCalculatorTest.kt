package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class MctqSleepScheduleCalculatorTest {

    @Test
    fun `주간근무 중간형 간호사 - rule target과 baseline timing 비교`() {
        val context = baseContext(
            currentShift = ShiftType.DAY,
            targetSleepTime = LocalDateTime.of(2026, 5, 28, 22, 30),
            subjectiveFatigueLevel = 3,
            objectiveRecoveryLevel = 3
        )

        val result = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = intermediateBaseline(),
            behavior = sampleBehavior()
        )

        printSchedule("중간형 주간근무", context, result)

        assertTrue(result.targetSleepDurationMinutes in 300..540)
    }

    @Test
    fun `야간근무 취약 간호사 - 야간 후 수면 시간이 늘어나는지 확인`() {
        val context = baseContext(
            previousShift = ShiftType.DAY,
            currentShift = ShiftType.NIGHT,
            nextShift = ShiftType.NIGHT,
            targetSleepTime = LocalDateTime.of(2026, 5, 29, 9, 30),
            subjectiveFatigueLevel = 5,
            objectiveRecoveryLevel = 1
        )

        val result = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = nightVulnerableBaseline(),
            behavior = sampleBehavior(
                vulnerableShift = ShiftType.NIGHT
            )
        )

        printSchedule("야간 취약 + 피로 높음", context, result)

        assertTrue(result.targetSleepDurationMinutes > result.baselineSleepDurationMinutes)
    }

    @Test
    fun `저녁형 간호사 - 늦은 baseline timing이 rule target과 얼마나 섞이는지 확인`() {
        val context = baseContext(
            currentShift = ShiftType.EVENING,
            targetSleepTime = LocalDateTime.of(2026, 5, 29, 1, 30),
            subjectiveFatigueLevel = 3,
            objectiveRecoveryLevel = 3
        )

        val result = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = eveningTypeBaseline(),
            behavior = sampleBehavior()
        )

        printSchedule("저녁형 이브닝근무", context, result)

        assertTrue(result.targetSleepDurationMinutes in 300..540)
    }

    @Test
    fun `회복 부족한 쉬는 날 - 자유일 평균 수면시간 기준으로 계산`() {
        val context = baseContext(
            currentShift = ShiftType.OFF,
            nextShift = ShiftType.DAY,
            targetSleepTime = LocalDateTime.of(2026, 5, 28, 23, 0),
            subjectiveFatigueLevel = 4,
            objectiveRecoveryLevel = 2
        )

        val result = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = intermediateBaseline(),
            behavior = sampleBehavior()
        )

        printSchedule("쉬는 날 회복 부족", context, result)

        assertTrue(result.targetSleepDurationMinutes > result.baselineSleepDurationMinutes)
    }

    @Test
    fun `엣지케이스 - baseline 시간이 과하게 달라도 rule target에서 크게 벗어나지 않는다`() {
        val context = baseContext(
            currentShift = ShiftType.DAY,
            targetSleepTime = LocalDateTime.of(2026, 5, 28, 22, 30),
            subjectiveFatigueLevel = 3,
            objectiveRecoveryLevel = 3
        )

        val result = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = extremeNightOwlBaseline(),
            behavior = sampleBehavior()
        )

        val diffMinutes = kotlin.math.abs(
            Duration.between(
                context.targetSleepTime,
                result.targetSleepStart
            ).toMinutes()
        )

        printSchedule("엣지 - 과도한 이동 방지", context, result)

        assertTrue(
            "targetSleepStart가 ruleTarget에서 너무 멀리 이동했습니다: ${diffMinutes}분",
            diffMinutes <= 120
        )
    }

    @Test
    fun `엣지케이스 - OFF 다음 DAY 근무를 침범하지 않는다`() {
        val context = baseContext(
            currentShift = ShiftType.OFF,
            nextShift = ShiftType.DAY,
            targetSleepTime = LocalDateTime.of(2026, 5, 28, 23, 0),
            subjectiveFatigueLevel = 5,
            objectiveRecoveryLevel = 1
        )

        val result = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = extremeLateFreeDayBaseline(),
            behavior = sampleBehavior(
                vulnerableShift = ShiftType.OFF
            )
        )

        printSchedule("엣지 - 다음 DAY 근무 침범 방지", context, result)

        val nextDayShiftStart = LocalDateTime.of(2026, 5, 29, 6, 30)
        val latestSafeWakeTime = nextDayShiftStart.minusMinutes(120)

        assertTrue(
            "수면 종료 시간이 다음 주간근무 준비 시간을 침범합니다. sleepEnd=${result.targetSleepEnd}",
            result.targetSleepEnd.isBefore(latestSafeWakeTime) ||
                    result.targetSleepEnd.isEqual(latestSafeWakeTime)
        )
    }

    @Test
    fun `엣지케이스 - 목표 수면 시작은 종료보다 항상 빨라야 한다`() {
        val context = baseContext(
            currentShift = ShiftType.EVENING,
            targetSleepTime = LocalDateTime.of(2026, 5, 29, 1, 30),
            subjectiveFatigueLevel = 5,
            objectiveRecoveryLevel = 1
        )

        val result = MctqSleepScheduleCalculator.calculate(
            context = context,
            baseline = extremeEveningBaseline(),
            behavior = sampleBehavior(
                vulnerableShift = ShiftType.EVENING
            )
        )

        printSchedule("엣지 - 시간 역전 방지", context, result)

        assertTrue(
            result.targetSleepStart.isBefore(result.targetSleepEnd)
        )
    }
    private fun extremeEveningBaseline(): MctqBaselineProfile {
        return sampleBaseline(
            dayWorkSleepDurationMinutes = 360,
            eveningWorkSleepDurationMinutes = 540,
            nightWorkSleepDurationMinutes = 360,
            averageFreeSleepDurationMinutes = 540,
            dayWorkMidSleep = "03:30",
            eveningWorkMidSleep = "08:30",
            nightWorkMidSleep = "14:00",
            chronotypeMsfEsc = "08:00",
            chronotypeMsfEscMinutes = 480
        )
    }

    private fun extremeNightOwlBaseline(): MctqBaselineProfile {
        return sampleBaseline(
            dayWorkSleepDurationMinutes = 420,
            eveningWorkSleepDurationMinutes = 480,
            nightWorkSleepDurationMinutes = 360,
            averageFreeSleepDurationMinutes = 540,
            dayWorkMidSleep = "06:30",
            eveningWorkMidSleep = "08:30",
            nightWorkMidSleep = "14:00",
            chronotypeMsfEsc = "09:00",
            chronotypeMsfEscMinutes = 540
        )
    }

    private fun extremeLateFreeDayBaseline(): MctqBaselineProfile {
        return sampleBaseline(
            dayWorkSleepDurationMinutes = 360,
            eveningWorkSleepDurationMinutes = 480,
            nightWorkSleepDurationMinutes = 360,
            averageFreeSleepDurationMinutes = 540,
            dayWorkMidSleep = "04:00",
            eveningWorkMidSleep = "07:00",
            nightWorkMidSleep = "13:30",
            chronotypeMsfEsc = "08:30",
            chronotypeMsfEscMinutes = 510
        )
    }

    private fun baseContext(
        previousShift: ShiftType? = ShiftType.DAY,
        currentShift: ShiftType = ShiftType.DAY,
        nextShift: ShiftType? = ShiftType.DAY,
        targetSleepTime: LocalDateTime,
        subjectiveFatigueLevel: Int = 3,
        objectiveRecoveryLevel: Int = 3
    ): SleepInterventionContext {
        return SleepInterventionContext(
            chronotype = Chronotype.INTERMEDIATE,
            previousShift = previousShift,
            currentShift = currentShift,
            nextShift = nextShift,
            workDate = LocalDate.of(2026, 5, 28),
            wakeTime = LocalDateTime.of(2026, 5, 28, 7, 0),
            targetSleepTime = targetSleepTime,
            subjectiveFatigueLevel = subjectiveFatigueLevel,
            objectiveRecoveryLevel = objectiveRecoveryLevel
        )
    }

    private fun sampleBehavior(
        vulnerableShift: ShiftType? = ShiftType.NIGHT
    ): MctqBehaviorProfile {
        return MctqBehaviorProfile(
            hasSleepLatencyRisk = false,
            hasBedInefficiency = false,
            hasNapHabit = false,
            hasLateNapRisk = false,
            vulnerableShift = vulnerableShift,
            averageSleepLatencyMinutes = 15,
            averageBedGapMinutes = 15
        )
    }

    private fun intermediateBaseline(): MctqBaselineProfile {
        return sampleBaseline(
            dayWorkSleepDurationMinutes = 405,
            eveningWorkSleepDurationMinutes = 435,
            nightWorkSleepDurationMinutes = 340,
            averageFreeSleepDurationMinutes = 445,
            dayWorkMidSleep = "02:37",
            eveningWorkMidSleep = "04:52",
            nightWorkMidSleep = "12:40",
            chronotypeMsfEsc = "04:44",
            chronotypeMsfEscMinutes = 284
        )
    }

    private fun nightVulnerableBaseline(): MctqBaselineProfile {
        return sampleBaseline(
            dayWorkSleepDurationMinutes = 410,
            eveningWorkSleepDurationMinutes = 420,
            nightWorkSleepDurationMinutes = 310,
            averageFreeSleepDurationMinutes = 450,
            dayWorkMidSleep = "01:35",
            eveningWorkMidSleep = "03:00",
            nightWorkMidSleep = "11:55",
            chronotypeMsfEsc = "02:23",
            chronotypeMsfEscMinutes = 143
        )
    }

    private fun eveningTypeBaseline(): MctqBaselineProfile {
        return sampleBaseline(
            dayWorkSleepDurationMinutes = 305,
            eveningWorkSleepDurationMinutes = 470,
            nightWorkSleepDurationMinutes = 335,
            averageFreeSleepDurationMinutes = 488,
            dayWorkMidSleep = "03:07",
            eveningWorkMidSleep = "06:45",
            nightWorkMidSleep = "13:12",
            chronotypeMsfEsc = "07:25",
            chronotypeMsfEscMinutes = 445
        )
    }

    private fun sampleBaseline(
        dayWorkSleepDurationMinutes: Int,
        eveningWorkSleepDurationMinutes: Int,
        nightWorkSleepDurationMinutes: Int,
        averageFreeSleepDurationMinutes: Int,
        dayWorkMidSleep: String,
        eveningWorkMidSleep: String,
        nightWorkMidSleep: String,
        chronotypeMsfEsc: String,
        chronotypeMsfEscMinutes: Int
    ): MctqBaselineProfile {
        return MctqBaselineProfile(
            blockResults = emptyList(),

            dayWorkSleepDurationMinutes = dayWorkSleepDurationMinutes,
            dayFreeSleepDurationMinutes = averageFreeSleepDurationMinutes,

            eveningWorkSleepDurationMinutes = eveningWorkSleepDurationMinutes,
            eveningFreeSleepDurationMinutes = averageFreeSleepDurationMinutes,

            nightWorkSleepDurationMinutes = nightWorkSleepDurationMinutes,
            nightFreeSleepDurationMinutes = averageFreeSleepDurationMinutes,

            dayWorkMidSleep = dayWorkMidSleep,
            dayFreeMidSleep = chronotypeMsfEsc,

            eveningWorkMidSleep = eveningWorkMidSleep,
            eveningFreeMidSleep = chronotypeMsfEsc,

            nightWorkMidSleep = nightWorkMidSleep,
            nightFreeMidSleep = chronotypeMsfEsc,

            averageWorkSleepDurationMinutes =
                (dayWorkSleepDurationMinutes + eveningWorkSleepDurationMinutes + nightWorkSleepDurationMinutes) / 3,
            averageFreeSleepDurationMinutes = averageFreeSleepDurationMinutes,
            unweightedAverageSleepDurationMinutes =
                (dayWorkSleepDurationMinutes + eveningWorkSleepDurationMinutes + nightWorkSleepDurationMinutes + averageFreeSleepDurationMinutes) / 4,

            workFreeSleepGapMinutes = averageFreeSleepDurationMinutes -
                    ((dayWorkSleepDurationMinutes + eveningWorkSleepDurationMinutes + nightWorkSleepDurationMinutes) / 3),

            socialJetlagDayMinutes = 60,
            socialJetlagEveningMinutes = 30,
            socialJetlagNightMinutes = 480,

            chronotypeMsfEsc = chronotypeMsfEsc,
            chronotypeMsfEscMinutes = chronotypeMsfEscMinutes,
            chronotypeLabel = "TEST"
        )
    }

    private fun printSchedule(
        label: String,
        context: SleepInterventionContext,
        result: SleepScheduleResult
    ) {
        println()
        println("===== $label =====")
        println("currentShift: ${context.currentShift}")
        println("ruleTargetSleepTime: ${context.targetSleepTime}")
        println("baselineDuration: ${result.baselineSleepDurationMinutes} min")
        println("targetDuration: ${result.targetSleepDurationMinutes} min")
        println("targetSleepStart: ${result.targetSleepStart}")
        println("targetSleepEnd: ${result.targetSleepEnd}")
        println("diffFromRuleTarget: ${
            Duration.between(context.targetSleepTime, result.targetSleepStart).toMinutes()
        } min")
        println("reason: ${result.adjustmentReason}")
    }
}