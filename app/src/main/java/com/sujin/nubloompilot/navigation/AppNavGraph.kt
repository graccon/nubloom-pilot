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
import com.sujin.nubloompilot.pages.HomePage
import com.sujin.nubloompilot.pages.MyInfoPage
import com.sujin.nubloompilot.pages.OnboardingPage
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

    val shouldShowBottomBar = currentRoute != Routes.OnboardingPage

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
                    onNavigateToSleepCheckIn = { duration, heartRate ->
                        navController.navigate("sleep_check_in/$duration/$heartRate")
                    }
                )
            }

            composable(
                route = Routes.SLEEP_CHECK_IN,
            ) { backStackEntry ->
                val duration = backStackEntry.arguments?.getString("duration")?.toLong() ?: 0L
                val heartRate = backStackEntry.arguments?.getString("heartRate")?.toLong() ?: -1L
                
                // TODO: Replace with actual SleepCheckInPage
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text("Sleep Check In: $duration min, HR: $heartRate")
                }
            }

            composable(Routes.MYINFO) {
                MyInfoPage()
            }

            composable(Routes.SLEEP) {
                SleepPage()
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