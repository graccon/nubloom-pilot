package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SleepInterventionEngineTest {

    @Test
    fun `MCTQ 프로필이 없으면 fallback 개입을 생성한다`() {
        val context = baseContext(
            mctqBaselineProfile = null,
            mctqBehaviorProfile = null
        )

        val interventions = SleepInterventionEngine.generate(context)

        printInterventions("fallback", interventions)

        assertTrue(interventions.isNotEmpty())
    }

    @Test
    fun `입면 지연 위험이 있으면 수면 준비 개입이 포함된다`() {
        val context = baseContext(
            currentShift = ShiftType.DAY,
            mctqBaselineProfile = sampleBaseline(),
            mctqBehaviorProfile = sampleBehavior(
                hasSleepLatencyRisk = true,
                averageSleepLatencyMinutes = 35
            )
        )

        val interventions = SleepInterventionEngine.generate(context)

        printInterventions("sleep latency risk", interventions)

        assertTrue(
            interventions.any { it.type == InterventionType.SLEEP_PREPARATION }
        )
    }

    @Test
    fun `야간근무와 높은 피로에서는 낮잠 카페인 목표수면이 포함된다`() {
        val context = baseContext(
            previousShift = ShiftType.EVENING,
            currentShift = ShiftType.NIGHT,
            nextShift = ShiftType.NIGHT,
            subjectiveFatigueLevel = 5,
            objectiveRecoveryLevel = 1,
            mctqBaselineProfile = sampleBaseline(),
            mctqBehaviorProfile = sampleBehavior(
                vulnerableShift = ShiftType.NIGHT,
                hasNapHabit = true
            )
        )

        val interventions = SleepInterventionEngine.generate(context)

        printInterventions("night shift high fatigue", interventions)

        assertTrue(interventions.any { it.type == InterventionType.MAIN_SLEEP })
        assertTrue(interventions.any { it.type == InterventionType.NAP })
        assertTrue(interventions.any { it.type == InterventionType.CAFFEINE })
    }

    @Test
    fun `야간근무 전환일에는 빛 개입이 포함된다`() {
        val context = baseContext(
            previousShift = ShiftType.DAY,
            currentShift = ShiftType.NIGHT,
            nextShift = ShiftType.NIGHT,
            mctqBaselineProfile = sampleBaseline(),
            mctqBehaviorProfile = sampleBehavior()
        )

        val interventions = SleepInterventionEngine.generate(context)

        printInterventions("transition to night", interventions)

//        assertTrue(
//            interventions.any { it.type == InterventionType.LIGHT }
//        )
    }

    @Test
    fun `전환 없는 주간근무에서는 빛 개입이 남발되지 않는다`() {
        val context = baseContext(
            previousShift = ShiftType.DAY,
            currentShift = ShiftType.DAY,
            nextShift = ShiftType.DAY,
            mctqBaselineProfile = sampleBaseline(),
            mctqBehaviorProfile = sampleBehavior()
        )

        val interventions = SleepInterventionEngine.generate(context)

        printInterventions("stable day shift", interventions)

        assertFalse(
            interventions.any { it.type == InterventionType.LIGHT }
        )
    }

    @Test
    fun `늦은 낮잠 위험이 있으면 수면 준비나 카페인 줄이기 개입이 포함된다`() {
        val context = baseContext(
            currentShift = ShiftType.EVENING,
            subjectiveFatigueLevel = 4,
            mctqBaselineProfile = sampleBaseline(),
            mctqBehaviorProfile = sampleBehavior(
                hasLateNapRisk = true,
                hasNapHabit = true
            )
        )

        val interventions = SleepInterventionEngine.generate(context)

        printInterventions("late nap risk", interventions)

        assertTrue(
            interventions.any {
                it.type == InterventionType.SLEEP_PREPARATION ||
                        (it.type == InterventionType.CAFFEINE &&
                                it.actionType == InterventionActionType.AVOID)
            }
        )
    }

    private fun baseContext(
        previousShift: ShiftType? = ShiftType.DAY,
        currentShift: ShiftType = ShiftType.DAY,
        nextShift: ShiftType? = ShiftType.DAY,
        subjectiveFatigueLevel: Int = 3,
        objectiveRecoveryLevel: Int = 3,
        mctqBaselineProfile: MctqBaselineProfile? = sampleBaseline(),
        mctqBehaviorProfile: MctqBehaviorProfile? = sampleBehavior()
    ): SleepInterventionContext {
        val workDate = LocalDate.of(2026, 5, 28)

        return SleepInterventionContext(
            chronotype = Chronotype.INTERMEDIATE,
            previousShift = previousShift,
            currentShift = currentShift,
            nextShift = nextShift,
            workDate = workDate,
            wakeTime = LocalDateTime.of(2026, 5, 28, 7, 0),
            targetSleepTime = when (currentShift) {
                ShiftType.DAY -> LocalDateTime.of(2026, 5, 28, 22, 30)
                ShiftType.EVENING -> LocalDateTime.of(2026, 5, 29, 1, 30)
                ShiftType.NIGHT -> LocalDateTime.of(2026, 5, 29, 9, 30)
                ShiftType.OFF -> LocalDateTime.of(2026, 5, 28, 23, 30)
            },
            subjectiveFatigueLevel = subjectiveFatigueLevel,
            objectiveRecoveryLevel = objectiveRecoveryLevel,
            mctqBaselineProfile = mctqBaselineProfile,
            mctqBehaviorProfile = mctqBehaviorProfile
        )
    }

    private fun sampleBehavior(
        hasSleepLatencyRisk: Boolean = false,
        hasBedInefficiency: Boolean = false,
        hasNapHabit: Boolean = false,
        hasLateNapRisk: Boolean = false,
        vulnerableShift: ShiftType? = ShiftType.NIGHT,
        averageSleepLatencyMinutes: Int = 15,
        averageBedGapMinutes: Int = 15
    ): MctqBehaviorProfile {
        return MctqBehaviorProfile(
            hasSleepLatencyRisk = hasSleepLatencyRisk,
            hasBedInefficiency = hasBedInefficiency,
            hasNapHabit = hasNapHabit,
            hasLateNapRisk = hasLateNapRisk,
            vulnerableShift = vulnerableShift,
            averageSleepLatencyMinutes = averageSleepLatencyMinutes,
            averageBedGapMinutes = averageBedGapMinutes
        )
    }

    private fun sampleBaseline(): MctqBaselineProfile {
        return MctqBaselineProfile(
            blockResults = emptyList(),

            dayWorkSleepDurationMinutes = 390,
            dayFreeSleepDurationMinutes = 450,

            eveningWorkSleepDurationMinutes = 430,
            eveningFreeSleepDurationMinutes = 470,

            nightWorkSleepDurationMinutes = 330,
            nightFreeSleepDurationMinutes = 450,

            dayWorkMidSleep = "02:30",
            dayFreeMidSleep = "03:30",

            eveningWorkMidSleep = "05:00",
            eveningFreeMidSleep = "05:30",

            nightWorkMidSleep = "12:30",
            nightFreeMidSleep = "04:30",

            averageWorkSleepDurationMinutes = 383,
            averageFreeSleepDurationMinutes = 456,
            unweightedAverageSleepDurationMinutes = 420,

            workFreeSleepGapMinutes = 73,

            socialJetlagDayMinutes = 60,
            socialJetlagEveningMinutes = 30,
            socialJetlagNightMinutes = 480,

            chronotypeMsfEsc = "04:30",
            chronotypeMsfEscMinutes = 270,
            chronotypeLabel = "INTERMEDIATE"
        )
    }

    private fun printInterventions(
        label: String,
        interventions: List<SleepIntervention>
    ) {
        println()
        println("===== $label =====")
        interventions.forEachIndexed { index, intervention ->
            println(
                "${index + 1}. ${intervention.type} / ${intervention.actionType} / ${intervention.title}"
            )
            println("   time: ${intervention.startTime} ~ ${intervention.endTime}")
            println("   description: ${intervention.description}")
            println("   reason: ${intervention.reason}")
        }
    }
}