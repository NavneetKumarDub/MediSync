package com.example.medisync.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.theme.natureGreen
private val ScreenBg      = Color(0xFFEEF5F3)   // same as screen background
private val TopBarBg1= natureGreen
private val ScreenBg1 = Color(0xFFF6F7F9)
private val CardBg1 = Color(0xFFFFFFFF)
private val TextPrimary1 = Color(0xFF111827)
private val TextSecondary1 = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScheduleScreen(
    navController: NavController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userId: Int
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(TopBarBg1)
                    .statusBarsPadding()
            ) {

                Box(
                    modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(
                        text = "Slots Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                navItems = NavItems.doctor,
                selectedIndex = selectedTab,
                onItemSelected = onTabSelected
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg1)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            ScheduleOptionCard(
                title = "Weekly Template",
                onClick = {
                    navController.navigate("doctorWeeklyTemplate/$userId")
                }
            )

            ScheduleOptionCard(
                title = "Custom Schedule",
                onClick = {
                    navController.navigate("CustomEditSlot/$userId")
                }
            )

            // future content below cards (reminders etc) goes here
        }
    }
}

@Composable
fun ScheduleOptionCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg1),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary1
            )
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = TextSecondary1,
                modifier = Modifier
                    .size(18.dp)
                    .then(Modifier.padding(0.dp))
                    .run {
                        this
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorSchedulePreview() {
    DoctorScheduleScreen(
        navController = rememberNavController(),
        selectedTab = 2,
        onTabSelected = {},
        userId = 1
    )
}