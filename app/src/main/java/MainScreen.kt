package com.kokila.jalsanchay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {

    val items = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home),
        BottomNavItem("History", Icons.Default.Build),
        BottomNavItem("Settings", Icons.Default.Settings),
        BottomNavItem("Tips", Icons.Default.Info),
        BottomNavItem("Weather", Icons.Default.Cloud)
    )

    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues)) {

            when (selectedItem) {
                0 -> DashboardScreen(
                    onLogout = onLogout)
                1 -> HistoryScreen()
                2 -> SettingsScreen()
                3 -> TipsScreen()
                4 -> WeatherScreen()
            }
        }
    }
}