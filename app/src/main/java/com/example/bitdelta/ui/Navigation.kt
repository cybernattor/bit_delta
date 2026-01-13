package com.example.bitdelta.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bitdelta.Screen
import com.example.bitdelta.ui.theme.*

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Achievements, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(NavyBlue)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route
                val color by animateColorAsState(if (selected) SkyBlue else TextWhite.copy(alpha = 0.5f), label = "nav")
                Column(
                    modifier = Modifier.clickable { 
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(screen.icon, null, tint = color)
                    Text(screen.label, color = color, fontSize = 10.sp, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
fun NavigationSideBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Achievements, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .width(110.dp)
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(NavyBlue)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route
                val color by animateColorAsState(if (selected) SkyBlue else TextWhite.copy(alpha = 0.5f), label = "nav")
                Column(
                    modifier = Modifier
                        .clickable { 
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(screen.icon, null, tint = color, modifier = Modifier.size(32.dp))
                    Text(screen.label, color = color, fontSize = 12.sp, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
