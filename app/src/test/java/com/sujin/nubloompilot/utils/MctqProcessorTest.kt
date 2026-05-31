package com.sujin.nubloompilot.utils

import com.sujin.nubloompilot.models.MCTQShiftResponse
import com.sujin.nubloompilot.shared.models.ShiftType
import org.junit.Test

class MctqProcessorTest {

    @Test
    fun `MCTQ baseline 계산 결과 확인`() {
        val responses = listOf(
            MCTQShiftResponse(
                shiftType = ShiftType.DAY,
                isWorkday = true,
                bedTime = "23:00",
                tryToSleepTime = "23:30",
                sleepLatencyMinutes = 20,
                wakeUpTime = "05:30",
                alarmUsed = true,
                outOfBedLatencyMinutes = 10,
                napTaken = false,
                canChooseSleepFreely = false,
                reasonIfCannotChoose = "출근"
            ),

            MCTQShiftResponse(
                shiftType = ShiftType.DAY,
                isWorkday = false,
                bedTime = "00:30",
                tryToSleepTime = "01:00",
                sleepLatencyMinutes = 20,
                wakeUpTime = "09:00",
                alarmUsed = false,
                outOfBedLatencyMinutes = 20,
                napTaken = false,
                canChooseSleepFreely = true
            ),

            MCTQShiftResponse(
                shiftType = ShiftType.EVENING,
                isWorkday = true,
                bedTime = "02:00",
                tryToSleepTime = "02:30",
                sleepLatencyMinutes = 20,
                wakeUpTime = "10:30",
                alarmUsed = false,
                outOfBedLatencyMinutes = 20,
                napTaken = false,
                canChooseSleepFreely = true
            ),

            MCTQShiftResponse(
                shiftType = ShiftType.EVENING,
                isWorkday = false,
                bedTime = "02:00",
                tryToSleepTime = "02:30",
                sleepLatencyMinutes = 20,
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

        val result = MctqProcessor.process(responses)

        println("result = $result")
        println("MSFEsc = ${result?.chronotypeMsfEsc}")
        println("MSFEsc minutes = ${result?.chronotypeMsfEscMinutes}")
        println("Day social jetlag = ${result?.socialJetlagDayMinutes}")
        println("Evening social jetlag = ${result?.socialJetlagEveningMinutes}")
        println("Night social jetlag = ${result?.socialJetlagNightMinutes}")
    }
}