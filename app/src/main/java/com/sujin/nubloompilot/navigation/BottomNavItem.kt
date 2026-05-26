package com.sujin.nubloompilot.navigation

import com.sujin.nubloompilot.R

//sealed class BottomNavItem(
//    val route: String,
//    val iconRes: Int
//) {
//    object Home : BottomNavItem(Routes.HOME, Icons.Default.Home)
//    object Sleep : BottomNavItem(Routes.SLEEP, Icons.Default.Face)
//    object MyInfo : BottomNavItem(Routes.MYINFO, Icons.Default.DateRange)
////    object CheckIn : BottomNavItem(Routes.CHECK_IN, Icons.Default.DateRange)
//
//}

sealed class BottomNavItem(
    val route: String,
    val iconRes: Int
) {
    object Home : BottomNavItem(
        Routes.HOME,
        R.drawable.ic_home
    )

    object Sleep : BottomNavItem(
        Routes.SLEEP,
        R.drawable.ic_sleep
    )

    object MyInfo : BottomNavItem(
        Routes.MYINFO,
        R.drawable.ic_myinfo
    )
}