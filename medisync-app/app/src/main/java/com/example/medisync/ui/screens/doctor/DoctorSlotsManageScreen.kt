package com.example.medisync.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.theme.natureGreen

private val ScheduleScreenBg = Color(0xFFF6F8FA)
private val ScheduleCardBg = Color.White
private val ScheduleBorder = Color(0xFFE5E7EB)
private val ScheduleTextPrimary = Color(0xFF111827)
private val ScheduleTextSecondary = Color(0xFF6B7280)
private val ScheduleSoftBlue = natureGreen.copy(alpha = 0.10f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScheduleScreen(
    navController: NavController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userId: Int
) {
    Scaffold(
        containerColor = ScheduleScreenBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(natureGreen)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        text = "Slot Management",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
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
                .background(ScheduleScreenBg)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ScheduleActionCard(
                title = "Weekly Template",
                leadingIcon = Icons.Default.Repeat,
                onClick = {
                    navController.navigate("doctorWeeklyTemplate/$userId")
                }
            )

            ScheduleActionCard(
                title = "Custom Schedule",
                leadingIcon = Icons.Default.EditCalendar,
                onClick = {
                    navController.navigate("CustomEditSlot/$userId")
                }
            )
        }
    }
}

@Composable
private fun ScheduleActionCard(
    title: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ScheduleCardBg)
            .border(1.dp, ScheduleBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ScheduleSoftBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = natureGreen,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = ScheduleTextPrimary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = ScheduleTextSecondary,
            modifier = Modifier.size(22.dp)
        )
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
