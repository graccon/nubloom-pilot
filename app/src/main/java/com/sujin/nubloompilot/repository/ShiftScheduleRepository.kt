package com.sujin.nubloompilot.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.local.ShiftScheduleLocalStore
import java.time.LocalDate

class ShiftScheduleRepository(
    context: Context
) {
    private val db = FirebaseFirestore.getInstance()

    private val participantLocalStore = ParticipantLocalStore(context)
    private val scheduleLocalStore = ShiftScheduleLocalStore(context)

    fun saveSchedule(
        year: Int,
        month: Int,
        shifts: Map<Int, String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val participantId = participantLocalStore.getParticipantId()

        if (participantId == null) {
            onFailure(IllegalStateException("Participant ID not found"))
            return
        }

        scheduleLocalStore.saveSchedule(
            year = year,
            month = month,
            shifts = shifts
        )

        val yearMonth = getYearMonthKey(year, month)
        val firestoreShifts = shifts.mapKeys { it.key.toString() }

        val scheduleData = hashMapOf(
            "year" to year,
            "month" to month,
            "shifts" to firestoreShifts,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("participants")
            .document(participantId)
            .collection("schedules")
            .document(yearMonth)
            .set(scheduleData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getLocalSchedule(
        year: Int,
        month: Int
    ): Map<Int, String> {
        return scheduleLocalStore.getSchedule(year, month)
    }

    fun getShiftForDate(
        date: LocalDate
    ): String? {
        val schedule = getLocalSchedule(
            year = date.year,
            month = date.monthValue
        )

        return schedule[date.dayOfMonth]
    }

    fun getTodayAndTomorrowShifts(): TodayTomorrowShifts {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        return TodayTomorrowShifts(
            todayDate = today,
            tomorrowDate = tomorrow,
            todayShift = getShiftForDate(today),
            tomorrowShift = getShiftForDate(tomorrow)
        )
    }

    fun getShiftsAroundToday(): ShiftAroundToday {
        val today = java.time.LocalDate.now()

        fun getShift(date: java.time.LocalDate): String? {
            val schedule = getLocalSchedule(
                year = date.year,
                month = date.monthValue
            )

            return schedule[date.dayOfMonth]
        }

        return ShiftAroundToday(
            yesterdayShift = getShift(today.minusDays(1)),
            todayShift = getShift(today),
            tomorrowShift = getShift(today.plusDays(1)),
            dayAfterTomorrowShift = getShift(today.plusDays(2))
        )
    }

    private fun getYearMonthKey(year: Int, month: Int): String {
        return "%04d-%02d".format(year, month)
    }
}

data class TodayTomorrowShifts(
    val todayDate: LocalDate,
    val tomorrowDate: LocalDate,
    val todayShift: String?,
    val tomorrowShift: String?
)

data class ShiftAroundToday(
    val yesterdayShift: String?,
    val todayShift: String?,
    val tomorrowShift: String?,
    val dayAfterTomorrowShift: String?
)