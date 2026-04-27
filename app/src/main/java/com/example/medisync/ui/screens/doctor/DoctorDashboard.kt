package com.example.medisync.ui.screens.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardContent(
    navController: NavController,
    userId: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                navItems = NavItems.doctor,
                selectedIndex = selectedTab,
                onItemSelected = onTabSelected
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Dashboard — stats & analytics")
        }
    }
}