package com.sujin.nubloompilot.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.sujin.nubloompilot.pages.CheckInPage
import com.sujin.nubloompilot.pages.HomePage
import com.sujin.nubloompilot.pages.OnboardingPage
import com.sujin.nubloompilot.pages.SleepPage
import com.sujin.nubloompilot.repository.ParticipantRepository
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

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

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
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
                HomePage(
                    participantName = participantName,
                    onCheckInClick = {
                        navController.navigate(Routes.CHECK_IN)
                    }
                )
            }

            composable(Routes.CHECK_IN) {
                CheckInPage(
                    onDone = {
                        navController.navigate(Routes.HOME)
                    }
                )
            }

            composable(Routes.SLEEP) {
                SleepPage()
            }
        }
    }
}