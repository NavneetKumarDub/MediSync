package com.example.medisync.appointment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medisync.data.TokenManager
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.viewmodels.AppointmentViewModel
import com.example.medisync.viewmodels.formatIsoDate

val ScreenBg = Color(0xFFF6F7F9)
val CardBg = Color(0xFFFFFFFF)
val TopBarBg = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF111827)
val TextSecondary = Color(0xFF6B7280)
val NatureGreen = natureGreen

data class AppointmentCardUiModel(
    val id: String,
    val name: String,
    val subtitle: String,
    val dateGroup: String,
    val time: String,
    val type: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppointmentListScreen(
    navController: NavController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    val context = LocalContext.current
    var userRole by remember { mutableStateOf("patient") }

    LaunchedEffect(Unit) {
        userRole = TokenManager.getRole(context) ?: "patient"
        if (userRole == "doctor") {
            viewModel.fetchDoctorAppointments(context)
        } else {
            viewModel.fetchPatientAppointments(context)
        }
    }

    val navItems = if (userRole == "doctor") NavItems.doctor else NavItems.patient
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "History")

    val mappedAppointments = viewModel.appointments.map { item ->
        AppointmentCardUiModel(
            id = item.appointmentId.toString(),
            name = item.displayName ?: "Unknown",
            subtitle = item.speciality ?: if (userRole == "doctor") "Patient" else "Speciality",
            dateGroup = formatIsoDate(item.date),
            time = item.startTime ?: "00:00",
            type = item.type ?: "online",
            status = item.status ?: "pending"
        )
    }

    val filteredAppointments = mappedAppointments
        .filter { model ->
            val isHistorical = model.status.lowercase() == "completed" || model.status.lowercase() == "cancelled"
            if (selectedTabIndex == 0) !isHistorical else isHistorical
        }
        .groupBy { it.dateGroup }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Appointments", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TopBarBg)
            )
        },
        bottomBar = {
            BottomNavBar(
                navItems = navItems,
                selectedIndex = selectedTab,
                onItemSelected = onTabSelected
            )
        },
        containerColor = ScreenBg
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = TopBarBg,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NatureGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) NatureGreen else TextSecondary
                            )
                        }
                    )
                }
            }

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NatureGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (filteredAppointments.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No appointments found", color = TextSecondary)
                            }
                        }
                    }

                    filteredAppointments.forEach { (dateHeader, appointmentsForDate) ->
                        stickyHeader {
                            Text(
                                text = dateHeader,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ScreenBg)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        items(appointmentsForDate, key = { it.id }) { appointment ->
                            CompactAppointmentCard(
                                model = appointment,
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactAppointmentCard(
    model: AppointmentCardUiModel,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = NatureGreen.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = model.name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NatureGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.subtitle,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (model.type.lowercase() == "online") Icons.Rounded.Videocam else Icons.Rounded.LocalHospital,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (model.type.lowercase() == "online") "Video Call" else "In-Clinic",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = model.time,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCompactAppointmentCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenBg)
    ) {
        CompactAppointmentCard(
            model = AppointmentCardUiModel(
                id = "1",
                name = "Dr. Ravi Sharma",
                subtitle = "Cardiologist",
                dateGroup = "TODAY, 20 MAY",
                time = "10:00 AM",
                type = "online",
                status = "confirmed"
            ),
            onClick = {}
        )
        CompactAppointmentCard(
            model = AppointmentCardUiModel(
                id = "2",
                name = "Rahul Kumar",
                subtitle = "Patient",
                dateGroup = "TODAY, 20 MAY",
                time = "02:30 PM",
                type = "in_person",
                status = "pending"
            ),
            onClick = {}
        )
    }
}