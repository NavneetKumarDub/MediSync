package com.example.medisync.ui.screens.patient
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.screens.appointment.AppointmentListScreen
import com.example.medisync.ui.screens.chat.ChatListScreen


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)@Composable
fun PatientNavigationScreen(
    navController: NavController,
    name: String = "",
    phone: String = "",
    userId: Int,
    initialTab: Int = 0
) {
    var selectedTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 3)) }

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    
    when (selectedTab) {
        0 -> HomeContent(
            navController = navController,
            name = name,
            phone = phone,
            userId = userId,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        1 -> AppointmentListScreen(
            navController = navController,
            selectedTab = selectedTab,
            onTabSelected = {selectedTab = it}
        )
        2 -> ChatListScreen(
            navController = navController,
            selectedTab = selectedTab,
            onTabSelected = {selectedTab = it}
        )
        3 -> PatientRecordsContent(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}







@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PatientHomePreview() {
    PatientNavigationScreen(
        navController = rememberNavController(),
        name = "Navneet",
        phone = "9122349557",
        userId = 1
    )
}
