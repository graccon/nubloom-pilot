package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.MCTQShiftResponse
import com.sujin.nubloompilot.shared.models.ShiftType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MctqBehaviorAnalyzerTest {

    @Test
    fun `MCTQ 행동 프로파일 분석 결과 확인`() {
        val responses = listOf(
            MCTQShiftResponse(
                shiftType = ShiftType.DAY,
                isWorkday = true,
                bedTime = "23:00",
                tryToSleepTime = "23:40", // 40 min bed gap
                sleepLatencyMinutes = 30, // 30 min latency
                wakeUpTime = "05:30",
                alarmUsed = true,
                outOfBedLatencyMinutes = 10,
                napTaken = true,
                napStartTime = "18:00",
                napEndTime = "18:30", // Late nap
                canChooseSleepFreely = false
            ),
            MCTQShiftResponse(
                shiftType = ShiftType.DAY,
                isWorkday = false,
                bedTime = "00:00",
                tryToSleepTime = "00:00",
                sleepLatencyMinutes = 10,
                wakeUpTime = "09:00",
                alarmUsed = false,
                outOfBedLatencyMinutes = 20,
                napTaken = true,
                napStartTime = "14:00",
                napEndTime = "15:00",
                canChooseSleepFreely = true
            ),
            MCTQShiftResponse(
                shiftType = ShiftType.EVENING,
                isWorkday = true,
                bedTime = "02:00",
                tryToSleepTime = "02:00",
                sleepLatencyMinutes = 10,
                wakeUpTime = "10:30",
                alarmUsed = false,
                outOfBedLatencyMinutes = 20,
                napTaken = true, // 3rd nap
                napStartTime = "14:00",
                napEndTime = "15:00",
                canChooseSleepFreely = true
            ),
            MCTQShiftResponse(
                shiftType = ShiftType.EVENING,
                isWorkday = false,
                bedTime = "02:00",
                tryToSleepTime = "02:00",
                sleepLatencyMinutes = 10,
                wakeUpTime = "12:30",
                alarmUsed = false,
                outOfBedLatencyMinutes = 20,
                napTaken = false,
                canChooseSleepFreely = true
            ),
            MCTQShiftResponse(
                shiftType = ShiftType.NIGHT,
                isWorkday = true,
                bedTime = "09:30",
                tryToSleepTime = "10:00",
                sleepLatencyMinutes = 15,
                wakeUpTime = "16:00",
                alarmUsed = false,
                outOfBedLatencyMinutes = 20,
                napTaken = false,
                canChooseSleepFreely = true
            ),
            MCTQShiftResponse(
                shiftType = ShiftType.NIGHT,
                isWorkday = false,
                bedTime = "03:00",
                tryToSleepTime = "03:30",
                sleepLatencyMinutes = 20,
                wakeUpTime = "11:30",
                alarmUsed = false,
                outOfBedLatencyMinutes = 20,
                napTaken = false,
                canChooseSleepFreely = true
            )
        )

        val baseline = MctqProcessor.process(responses)!!
        val behavior = MctqBehaviorAnalyzer.analyze(responses, baseline)

        println("behavior = $behavior")

        assertFalse(behavior.hasBedInefficiency)
        assertTrue(behavior.hasNapHabit)
        assertFalse(behavior.hasLateNapRisk)
        assertTrue(behavior.hasSleepLatencyRisk)

        assertEquals(15, behavior.averageSleepLatencyMinutes)
        assertEquals(16, behavior.averageBedGapMinutes)
        assertEquals(ShiftType.NIGHT, behavior.vulnerableShift)
    }

    @Test
    fun `아침형 간호사 행동 프로파일`() {
        val responses = listOf(
            mctq(ShiftType.DAY, true, "21:30", "22:00", 10, "05:00", false),
            mctq(ShiftType.DAY, false, "22:00", "22:15", 10, "06:00", false),
            mctq(ShiftType.EVENING, true, "23:00", "23:20", 10, "06:30", false),
            mctq(ShiftType.EVENING, false, "22:30", "22:45", 10, "06:15", false),
            mctq(ShiftType.NIGHT, true, "08:30", "09:00", 20, "14:30", false),
            mctq(ShiftType.NIGHT, false, "21:30", "22:00", 10, "05:45", false)
        )

        val baseline = MctqProcessor.process(responses)!!
        val behavior = MctqBehaviorAnalyzer.analyze(responses, baseline)

        println("morning baseline = $baseline")
        println("morning behavior = $behavior")

//        assertFalse(behavior.hasSleepLatencyRisk)
//        assertFalse(behavior.hasBedInefficiency)
//        assertFalse(behavior.hasNapHabit)
//        assertFalse(behavior.hasLateNapRisk)
    }

    @Test
    fun `중간형 간호사 행동 프로파일`() {
        val responses = listOf(
            mctq(ShiftType.DAY, true, "22:30", "23:00", 15, "06:00", false),
            mctq(ShiftType.DAY, false, "23:30", "00:00", 15, "07:30", false),
            mctq(ShiftType.EVENING, true, "00:30", "01:00", 15, "08:30", false),
            mctq(ShiftType.EVENING, false, "00:30", "01:00", 15, "09:00", false),
            mctq(ShiftType.NIGHT, true, "09:00", "09:30", 20, "15:30", true, "17:00", "17:30"),
            mctq(ShiftType.NIGHT, false, "00:30", "01:00", 15, "08:30", false)
        )

        val baseline = MctqProcessor.process(responses)!!
        val behavior = MctqBehaviorAnalyzer.analyze(responses, baseline)

        println("intermediate baseline = $baseline")
        println("intermediate behavior = $behavior")

//        assertFalse(behavior.hasSleepLatencyRisk)
//        assertFalse(behavior.hasBedInefficiency)
//        assertFalse(behavior.hasNapHabit)
    }

    @Test
    fun `저녁형 간호사 행동 프로파일`() {
        val responses = listOf(
            mctq(ShiftType.DAY, true, "23:30", "00:00", 35, "05:40", true, "20:30", "21:30"),
            mctq(ShiftType.DAY, false, "01:30", "02:00", 20, "10:30", true, "15:00", "16:00"),
            mctq(ShiftType.EVENING, true, "02:00", "02:30", 20, "10:40", true, "14:00", "15:00"),
            mctq(ShiftType.EVENING, false, "03:00", "03:30", 20, "12:00", false),
            mctq(ShiftType.NIGHT, true, "09:30", "10:00", 25, "16:00", false),
            mctq(ShiftType.NIGHT, false, "03:30", "04:00", 25, "12:30", false)
        )

        val baseline = MctqProcessor.process(responses)!!
        val behavior = MctqBehaviorAnalyzer.analyze(responses, baseline)

        println("evening baseline = $baseline")
        println("evening behavior = $behavior")
//
//        assertTrue(behavior.hasSleepLatencyRisk)
//        assertTrue(behavior.hasNapHabit)
//        assertTrue(behavior.hasLateNapRisk)
    }

    private fun mctq(
        shiftType: ShiftType,
        isWorkday: Boolean,
        bedTime: String,
        tryToSleepTime: String,
        sleepLatency: Int,
        wakeUpTime: String,
        napTaken: Boolean,
        napStartTime: String? = null,
        napEndTime: String? = null
    ): MCTQShiftResponse {
        return MCTQShiftResponse(
            shiftType = shiftType,
            isWorkday = isWorkday,
            bedTime = bedTime,
            tryToSleepTime = tryToSleepTime,
            sleepLatencyMinutes = sleepLatency,
            wakeUpTime = wakeUpTime,
            alarmUsed = isWorkday,
            outOfBedLatencyMinutes = 10,
            napTaken = napTaken,
            napStartTime = napStartTime,
            napEndTime = napEndTime,
            canChooseSleepFreely = !isWorkday
        )
    }
}
