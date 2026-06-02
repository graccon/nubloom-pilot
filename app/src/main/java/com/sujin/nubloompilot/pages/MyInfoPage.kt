package com.sujin.nubloompilot.pages


import android.util.Log
import com.sujin.nubloompilot.components.BugReportSection
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sujin.nubloompilot.components.AppDiagnosticsSection
import com.sujin.nubloompilot.components.DutyScheduleSection
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.local.ShiftScheduleLocalStore
import com.sujin.nubloompilot.local.ShiftTimingLocalStore
import com.sujin.nubloompilot.local.SleepInterventionLocalStore
import com.sujin.nubloompilot.repository.AppDiagnosticsRepository
import com.sujin.nubloompilot.repository.BugReportRepository
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.components.MctqBaselineSection
import com.sujin.nubloompilot.components.MctqBehaviorSection
import com.sujin.nubloompilot.components.TopBannerManager
import com.sujin.nubloompilot.models.AppDiagnosticsState
import com.sujin.nubloompilot.models.BugReport
import com.sujin.nubloompilot.shared.models.ShiftTimingConfig
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth

@Composable
fun MyInfoPage(
    modifier: Modifier = Modifier,
    topBannerManager: TopBannerManager
) {
    val context = LocalContext.current
    val scheduleRepository = remember {
        ShiftScheduleRepository(context)
    }
    val healthConnectRepository = remember {
        HealthConnectRepository(context)
    }
    val participantLocalStore = remember {
        ParticipantLocalStore(context)
    }
    val timingLocalStore = remember {
        ShiftTimingLocalStore(context)
    }

    val bugReportRepository = remember {
        BugReportRepository()
    }

    val appDiagnosticsRepository = remember {
        AppDiagnosticsRepository(
            context = context,
            healthConnectRepository = healthConnectRepository,
            shiftScheduleRepository = scheduleRepository,
            interventionLocalStore = SleepInterventionLocalStore(context)
        )
    }

    val scope = rememberCoroutineScope()

    val baselineProfile = remember {
        participantLocalStore.getBaselineProfile()
    }

    val mctqBehaviorProfile = remember {
        participantLocalStore.getMctqBehaviorProfile()
    }

    var currentYearMonth by remember {
        mutableStateOf(YearMonth.now())
    }

    var shifts by remember(currentYearMonth) {
        mutableStateOf(
            scheduleRepository.getLocalSchedule(
                year = currentYearMonth.year,
                month = currentYearMonth.monthValue
            )
        )
    }

    var originalShifts by remember(currentYearMonth) {
        mutableStateOf(shifts)
    }


    var isEditMode by remember {
        mutableStateOf(false)
    }

    var selectedDay by remember(currentYearMonth) {
        mutableStateOf(1)
    }

    var timingConfig by remember {
        mutableStateOf(timingLocalStore.getConfig())
    }

    var isBugReportSubmitting by remember {
        mutableStateOf(false)
    }

    var diagnosticsState by remember { mutableStateOf(AppDiagnosticsState()) }

    LaunchedEffect(baselineProfile, shifts, currentYearMonth) {
        diagnosticsState = appDiagnosticsRepository.getDiagnosticsState(
            baselineProfileExists = baselineProfile != null
        )
    }

    val daysInMonth = currentYearMonth.lengthOfMonth()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            DutyScheduleSection(
                currentYearMonth = currentYearMonth,
                shifts = shifts,
                isEditMode = isEditMode,
                selectedDay = selectedDay,
                timingConfig = timingConfig,
                onPrevMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
                onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
                onEditStart = {
                    originalShifts = shifts
                    selectedDay = 1
                    isEditMode = true
                },
                onCancelEdit = {
                    shifts = originalShifts
                    timingConfig = timingLocalStore.getConfig()
                    isEditMode = false
                },
                onSave = {
                    scheduleRepository.saveSchedule(
                        year = currentYearMonth.year,
                        month = currentYearMonth.monthValue,
                        shifts = shifts,
                        onSuccess = {
                            originalShifts = shifts
                            timingLocalStore.saveConfig(timingConfig)
                            isEditMode = false
                            topBannerManager.show("듀티표가 저장되었습니다")
                        },
                        onFailure = {
                            // TODO: 실패 배너 처리
                        }
                    )
                },
                onDayClick = { day ->
                    selectedDay = day
                },
                onShiftSelected = { shift ->
                    shifts = shifts.toMutableMap().apply {
                        this[selectedDay] = shift
                    }
                    selectedDay = if (selectedDay < daysInMonth) {
                        selectedDay + 1
                    } else {
                        1
                    }
                },
                onTimingConfigChange = { newConfig ->
                    timingConfig = newConfig
                },
                onResetTimingConfig = {
                    timingLocalStore.resetToDefault()
                    timingConfig = ShiftTimingConfig.Default
                }
            )

//            MctqBaselineSection(baselineProfile)
//
//            MctqBehaviorSection(mctqBehaviorProfile)

            BugReportSection(
                isSubmitting = isBugReportSubmitting,
                onSubmit = { description ->
                    if (isBugReportSubmitting) return@BugReportSection

                    isBugReportSubmitting = true

                    scope.launch {
                        try {
                            val report = BugReport(
                                participantId = participantLocalStore.getParticipantId()
                                    ?: "unknown",
                                participantName = participantLocalStore.getParticipantName()
                                    ?: "간호사",
                                description = description,
                                createdAt = Instant.now().toString()
                            )

                            bugReportRepository.submitBugReport(report)

                            topBannerManager.show("버그 리포트가 전송되었습니다")
                            Log.d("BugReport", "Bug report submitted successfully")
                        } catch (e: Exception) {
                            Log.e("BugReport", "Failed to submit bug report", e)
                        } finally {
                            isBugReportSubmitting = false
                        }
                    }
                }
            )

            AppDiagnosticsSection(state = diagnosticsState)

            Spacer(modifier = Modifier.height(220.dp))
        }
    }
}
