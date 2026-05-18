package com.sujin.nubloompilot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sujin.nubloompilot.pages.CheckInPage
import com.sujin.nubloompilot.pages.HomePage
import com.sujin.nubloompilot.pages.ParticipantPage

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.PARTICIPANT
    ) {
        composable(Routes.PARTICIPANT) {
            ParticipantPage(
                onNext = {
                    navController.navigate(Routes.HOME)
                }
            )
        }

        composable(Routes.HOME) {
            HomePage(
                onCheckInClick = {
                    navController.navigate(Routes.CHECK_IN)
                }
            )
        }

        composable(Routes.CHECK_IN) {
            CheckInPage(
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}