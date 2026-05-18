package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sujin.nubloompilot.navigation.BottomNavItem

@Composable
fun BottomBar(
    navController: NavController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Sleep,
        BottomNavItem.CheckIn
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(
                color = Color(0xFF1F1F1F),
                shape = RoundedCornerShape(40.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) Color(0xFFF7C53A) else Color.Transparent,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable {
                        navController.navigate(item.route)
                    }
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (selected) Color.Black else Color.Gray
                )
            }
        }
    }
}