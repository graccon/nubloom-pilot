package com.sujin.nubloompilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sujin.nubloompilot.navigation.BottomNavItem
import com.sujin.nubloompilot.ui.theme.Gray600
import com.sujin.nubloompilot.ui.theme.Gray900

@Composable
fun BottomBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Sleep,
        BottomNavItem.MyInfo
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(
                color = Color(0xFF1F1F1F),
                shape = RoundedCornerShape(40.dp)
            )
            .padding(7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) Color(0xFFF7C53A) else Color.Transparent,
                        shape = RoundedCornerShape(40.dp)
                    )
                    .clickable {
                        navController.navigate(item.route)
                    }
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (selected) Gray900 else Gray600
                )
            }
        }
    }
}