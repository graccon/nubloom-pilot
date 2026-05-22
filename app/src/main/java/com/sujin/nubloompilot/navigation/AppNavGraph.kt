package com.sujin.nubloompilot.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sujin.nubloompilot.components.BottomBar
import com.sujin.nubloompilot.local.ParticipantLocalStore
import com.sujin.nubloompilot.models.MorningGloryType
import com.sujin.nubloompilot.pages.HomePage
import com.sujin.nubloompilot.pages.MorningGloryResultPage
import com.sujin.nubloompilot.pages.MyInfoPage
import com.sujin.nubloompilot.pages.OnboardingPage
import com.sujin.nubloompilot.pages.SleepCheckInPage
import com.sujin.nubloompilot.pages.SleepPage
import com.sujin.nubloompilot.repository.ParticipantRepository
import com.sujin.nubloompilot.repository.ShiftScheduleRepository
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment

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

    val hasParticipant = remember {
        localStore.getParticipantId() != null
    }

    val participantName = remember {
        localStore.getParticipantName() ?: "간호사"
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

                HomePage(
                    participantName = participantName,
                    yesterdayShift = shiftsAroundToday.yesterdayShift,
                    todayShift = shiftsAroundToday.todayShift,
                    tomorrowShift = shiftsAroundToday.tomorrowShift,
                    dayAfterTomorrowShift = shiftsAroundToday.dayAfterTomorrowShift,
                    onNavigateToSleepCheckIn = { duration, heartRate, baselineDuration, baselineHeartRate ->
                        navController.navigate(
                            Routes.sleepCheckInRoute(
                                duration = duration,
                                heartRate = heartRate,
                                baselineDuration = baselineDuration,
                                baselineHeartRate = baselineHeartRate
                            )
                        )
                    }
                )
            }
            composable(
                route = Routes.SLEEP_CHECK_IN
            ) { backStackEntry ->
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
                    sleepDurationMinutes = duration,
                    wakeHeartRate = heartRate.takeIf { it != -1L },
                    baselineSleepDurationMinutes = baselineDuration.takeIf { it != -1L },
                    baselineWakeHeartRate = baselineHeartRate.takeIf { it != -1L },
                    onSubmitClick = { type ->
                        navController.navigate(Routes.morningGloryResultRoute(type))
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
                val type = MorningGloryType.valueOf(
                    backStackEntry.arguments?.getString("type") ?: MorningGloryType.TYPE_1.name
                )

                MorningGloryResultPage(
                    participantName = participantName,
                    type = type,
                    onBackHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
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