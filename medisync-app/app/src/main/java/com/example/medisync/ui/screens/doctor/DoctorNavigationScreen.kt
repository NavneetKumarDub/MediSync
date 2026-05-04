package com.example.medisync.ui.screens.doctor

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.screens.chat.ChatListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorNavigationScreen(
    navController: NavController,
    name: String = "",
    phone: String = "",
    userId: Int
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    when (selectedTab) {
        0 -> DoctorHomeContent(
            navController = navController,
            name = name,
            phone = phone,
            userId = userId,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        1 -> DoctorScheduleContent(
            navController = navController,
            selectedTab,
            onTabSelected = { selectedTab = it }
        )
        2 -> ChatListScreen(
            navController = navController,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        3 -> DoctorScheduleScreen(
            navController = navController,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            userId = userId
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorHomePreview() {
    DoctorNavigationScreen(
        navController = rememberNavController(),
        name = "Sharma",
        phone = "9122349557",
        userId = 1
    )
}