package com.example.medisync.ui.screens.patient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@OptIn(ExperimentalMaterial3Api::class)@Composable
fun PatientNavigationScreen(
    navController: NavController,
    name: String = "",
    phone: String = "",
    userId: Int
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // No Scaffold here — each tab manages its own
    when (selectedTab) {
        0 -> HomeContent(
            navController = navController,
            name = name,
            phone = phone,
            userId = userId,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        1 -> AppointmentContent(
            navController = navController,
            selectedTab,
            onTabSelected = { selectedTab = it })
        2 -> AiChatContent(

        )
        3 -> RecordsContent(

        )
    }
}




@Composable
fun AiChatContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("AI Chat")
    }
}

@Composable
fun RecordsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Records")
    }
}


// ── BOTTOM NAV ITEMS ──



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