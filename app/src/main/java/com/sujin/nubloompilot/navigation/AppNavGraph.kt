package com.sujin.nubloompilot.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sujin.nubloompilot.components.BottomBar
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.local.SleepSurveyLocalStore
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.models.SleepResult
import com.sujin.nubloompilot.pages.HomePage
import com.sujin.nubloompilot.pages.MorningGloryResultPage
import com.sujin.nubloompilot.pages.MyInfoPage
import com.sujin.nubloompilot.pages.OnboardingPage
import com.sujin.nubloompilot.pages.SleepCheckInPage
import com.sujin.nubloompilot.pages.SleepPage
import com.sujin.nubloompilot.repository.ParticipantRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import com.sujin.nubloompilot.repository.SleepResultRepository
import com.sujin.nubloompilot.repository.SleepStatusRepository
import com.sujin.nubloompilot.repository.HealthConnectRepository
import com.sujin.nubloompilot.repository.SleepInterventionRepository
import com.sujin.nubloompilot.local.SleepInterventionLocalStore
import com.sujin.nubloompilot.utils.SleepInterventionMapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import com.sujin.nubloompilot.models.Chronotype
import com.sujin.nubloompilot.models.ShiftType
import com.sujin.nubloompilot.models.SleepInterventionContext
import com.sujin.nubloompilot.utils.MainSleepDurationCalculator
import com.sujin.nubloompilot.utils.TargetSleepTimeCalculator
import java.time.ZoneId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val localStore = remember {
        ParticipantLocalStore(context)
    }

    val participantRepository = remember {
        ParticipantRepository(context)
    }

    val shiftScheduleRepository = remember {
        ShiftScheduleRepository(context)
    }

    val participantId = remember {
        localStore.getParticipantId() ?: "unknown"
    }

    val participantName = remember {
        localStore.getParticipantName() ?: "간호사"
    }

    val sleepStatusRepository = remember {
        SleepStatusRepository(
            HealthConnectRepository(context),
            SleepSurveyLocalStore(context)
        )
    }

    val sleepResultRepository = remember(participantId) {
        SleepResultRepository(
            participantId = participantId,
            localStore = SleepSurveyLocalStore(context)
        )
    }

    val sleepInterventionRepository = remember {
        SleepInterventionRepository(SleepInterventionLocalStore(context))
    }

    val hasParticipant = remember {
        localStore.getParticipantId() != null
    }

    val startDestination = if (hasParticipant) {
        Routes.HOME
    } else {
        Routes.OnboardingPage
    }

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val shouldShowBottomBar = currentRoute != null &&
            currentRoute != Routes.OnboardingPage &&
            !currentRoute.startsWith("sleep_check_in") &&
            !currentRoute.startsWith("morning_glory_result")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Routes.OnboardingPage) {
                OnboardingPage(
                    onSubmit = { name, birthYear ->
                        participantRepository.registerParticipant(
                            name = name,
                            birthYear = birthYear,
                            onSuccess = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.OnboardingPage) {
                                        inclusive = true
                                    }
                                }
                            },
                            onFailure = { exception ->
                                println("Participant save failed: ${exception.message}")
                            }
                        )
                    }
                )
            }

            composable(Routes.HOME) {
                val shiftsAroundToday = remember {
                    shiftScheduleRepository.getShiftsAroundToday()
                }

                val latestIntervention = remember {
                    sleepInterventionRepository.getLatestLocal()
                }

                HomePage(
                    participantName = participantName,
                    yesterdayShift = shiftsAroundToday.yesterdayShift,
                    todayShift = shiftsAroundToday.todayShift,
                    tomorrowShift = shiftsAroundToday.tomorrowShift,
                    dayAfterTomorrowShift = shiftsAroundToday.dayAfterTomorrowShift,
                    latestInterventionBundle = latestIntervention,
                    onNavigateToSleepCheckIn = { endTime, duration, heartRate, baselineDuration, baselineHeartRate ->
                        navController.navigate(
                            Routes.sleepCheckInRoute(
                                endTime = endTime,
                                duration = duration,
                                heartRate = heartRate,
                                baselineDuration = baselineDuration,
                                baselineHeartRate = baselineHeartRate
                            )
                        )
                    },
                    onNavigateToResult = { type ->
                        navController.navigate("morning_glory_result/${type.name}/NONE/0/-1/0")
                    }
                )
            }
            composable(
                route = Routes.SLEEP_CHECK_IN
            ) { backStackEntry ->
                val endTime = backStackEntry.arguments?.getString("endTime") ?: ""
                val duration =
                    backStackEntry.arguments
                        ?.getString("duration")
                        ?.toLongOrNull()
                        ?: 0L

                val heartRate =
                    backStackEntry.arguments
                        ?.getString("heartRate")
                        ?.toLongOrNull()
                        ?: -1L

                val baselineDuration =
                    backStackEntry.arguments
                        ?.getString("baselineDuration")
                        ?.toLongOrNull()
                        ?: -1L

                val baselineHeartRate =
                    backStackEntry.arguments
                        ?.getString("baselineHeartRate")
                        ?.toLongOrNull()
                        ?: -1L

                SleepCheckInPage(
                    participantName = participantName,
                    sleepEndTime = endTime,
                    sleepDurationMinutes = duration,
                    wakeHeartRate = heartRate.takeIf { it != -1L },
                    baselineSleepDurationMinutes = baselineDuration.takeIf { it != -1L },
                    baselineWakeHeartRate = baselineHeartRate.takeIf { it != -1L },
                    onSubmitClick = { type, time, fatigue ->
                        navController.navigate(
                            Routes.morningGloryResultRoute(
                                type = type,
                                endTime = time,
                                duration = duration,
                                heartRate = heartRate.takeIf { it != -1L },
                                fatigueLevel = fatigue
                            )
                        )
                    }
                )
            }


            composable(Routes.MYINFO) {
                MyInfoPage()
            }

            composable(Routes.SLEEP) {
                SleepPage()
            }

            composable(
                route = Routes.MORNING_GLORY_RESULT
            ) { backStackEntry ->
                val typeFromArg = MorningGloryType.valueOf(
                    backStackEntry.arguments?.getString("type") ?: MorningGloryType.TYPE_1.name
                )
                val endTimeFromArg = backStackEntry.arguments?.getString("endTime") ?: "NONE"
                val durationFromArg =
                    backStackEntry.arguments?.getString("duration")?.toLongOrNull() ?: 0L
                val heartRateFromArg =
                    backStackEntry.arguments?.getString("heartRate")?.toLongOrNull() ?: -1L
                val fatigueFromArg =
                    backStackEntry.arguments?.getString("fatigueLevel")?.toIntOrNull() ?: 0

                var recoveredResult by remember { mutableStateOf<SleepResult?>(null) }

                LaunchedEffect(endTimeFromArg) {
                    if (endTimeFromArg == "NONE") {
                        recoveredResult = sleepStatusRepository.getLatestSavedSleepResult()
                    }
                }

                val finalEndTime = recoveredResult?.sleepEndTime?.toString() ?: endTimeFromArg
                val finalFatigue = recoveredResult?.fatigueLevel ?: fatigueFromArg
                val finalType = recoveredResult?.morningGloryType ?: typeFromArg

                val objectiveRecoveryLevel = when (finalType) {
                    MorningGloryType.TYPE_1 -> 5
                    MorningGloryType.TYPE_2 -> 2
                    MorningGloryType.TYPE_3 -> 4
                    MorningGloryType.TYPE_4 -> 1
                }

                val shiftsAroundToday = remember {
                    shiftScheduleRepository.getShiftsAroundToday()
                }

                val chronotype = Chronotype.INTERMEDIATE // TODO: 나중에 사용자 설정값으로 교체
                val currentShift = ShiftType.fromString(shiftsAroundToday.todayShift)
                val nextShift = ShiftType.fromString(shiftsAroundToday.tomorrowShift)
                val previousShift = ShiftType.fromString(shiftsAroundToday.yesterdayShift)
                val workDate = LocalDate.now()

                val mainSleepDuration = MainSleepDurationCalculator.calculate(
                    currentShift = currentShift,
                    previousShift = previousShift,
                    nextShift = nextShift,
                    subjectiveFatigueLevel = finalFatigue,
                    objectiveRecoveryLevel = objectiveRecoveryLevel,
                    chronotype = chronotype
                )

                val targetSleepTime = TargetSleepTimeCalculator.calculate(
                    currentShift = currentShift,
                    nextShift = nextShift,
                    workDate = workDate,
                    mainSleepDurationMinutes = mainSleepDuration.toMinutes(),
                    commuteMinutes = 60L, // TODO
                    preWorkPreparationMinutes = 60L // TODO
                )

                val wakeTime =
                    (if (finalEndTime == "NONE") Instant.now() else Instant.parse(finalEndTime))
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                val interventionContext = SleepInterventionContext(
                    chronotype = chronotype,
                    previousShift = previousShift,
                    currentShift = currentShift,
                    nextShift = nextShift,
                    workDate = workDate,
                    wakeTime = wakeTime,
                    targetSleepTime = targetSleepTime,
                    subjectiveFatigueLevel = finalFatigue,
                    objectiveRecoveryLevel = objectiveRecoveryLevel
                )

                MorningGloryResultPage(
                    participantName = participantName,
                    type = finalType,
                    isReviewMode = endTimeFromArg == "NONE",
                    interventionContext = interventionContext,
                    onBackHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                        }
                    },
                    onSaveResult = {
                        if (endTimeFromArg != "NONE") {
                            val result = SleepResult(
                                participantId = participantId,
                                participantName = participantName,
                                sleepEndTime = Instant.parse(endTimeFromArg),
                                sleepDurationMinutes = durationFromArg,
                                wakeHeartRate = if (heartRateFromArg == -1L) null else heartRateFromArg,
                                fatigueLevel = fatigueFromArg,
                                morningGloryType = typeFromArg
                            )
                            sleepResultRepository.saveSleepResult(result)
                        }
                    },
                    onSaveInterventions = { interventions, context ->
                        if (endTimeFromArg != "NONE") {
                            val bundle = SleepInterventionMapper.toBundle(
                                participantId = participantId,
                                morningGloryType = finalType,
                                context = context,
                                interventions = interventions
                            )
                            sleepInterventionRepository.save(bundle)
                        }
                    }
                )
            }
        }


        if (shouldShowBottomBar) {
            BottomBar(
                navController = navController,
                currentRoute = currentRoute,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
