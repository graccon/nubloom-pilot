package com.sujin.nubloompilot.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.sujin.nubloompilot.components.BottomBar
import com.sujin.nubloompilot.pages.CheckInPage
import com.sujin.nubloompilot.pages.HomePage
import com.sujin.nubloompilot.pages.ParticipantPage
import com.sujin.nubloompilot.pages.SleepPage

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

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
            Scaffold(
                bottomBar = {
                    BottomBar(
                        navController = navController,
                        currentRoute = currentRoute
                    )
                }
            ) { innerPadding ->
                HomePage(
                    onCheckInClick = {
                        navController.navigate(Routes.CHECK_IN)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(Routes.CHECK_IN) {
            Scaffold(
                bottomBar = {
                    BottomBar(
                        navController = navController,
                        currentRoute = currentRoute
                    )
                }
            ) { padding ->
                CheckInPage(
                    onDone = {
                        navController.navigate(Routes.HOME)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(Routes.SLEEP) {
            Scaffold(
                bottomBar = {
                    BottomBar(
                        navController = navController,
                        currentRoute = currentRoute
                    )
                }
            ) { innerPadding ->
                SleepPage(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}