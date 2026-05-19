package com.sujin.nubloompilot.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Routes.HOME, Icons.Default.Home)
    object Sleep : BottomNavItem(Routes.SLEEP, Icons.Default.Face)
    object MyInfo : BottomNavItem(Routes.MYINFO, Icons.Default.DateRange)
//    object CheckIn : BottomNavItem(Routes.CHECK_IN, Icons.Default.DateRange)
}