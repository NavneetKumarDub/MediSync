// ─────────────────────────────────────────────
// AppointmentScreen.kt
// ─────────────────────────────────────────────
package com.example.medisync.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.R
import com.example.medisync.ui.components.AppointmentCard
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems

// ─────────────────────────────────────────────
//  Design System
// ─────────────────────────────────────────────
val ScreenBg         = Color(0xFFF6F7F9)
val CardBg           = Color(0xFFFFFFFF)
val TopBarBg         = Color(0xFFFFFFFF)
val DividerColor     = Color(0xFFE4E7EC)
val SearchBg         = Color(0xFFEEF0F3)

val TextPrimary      = Color(0xFF111827)
val TextSecondary    = Color(0xFF6B7280)
val TextHint         = Color(0xFF9CA3AF)

val GreenPrimary     = Color(0xFF27AE7A)
val GreenLight       = Color(0xFFE6F7F0)
val GreenText        = Color(0xFF1A8C61)
val GreenBorder      = Color(0xFFB2DFD0)

val ChipActiveBg     = GreenLight
val ChipActiveBorder = GreenBorder
val ChipActiveText   = GreenText
val ChipIdleBg       = Color(0xFFEEF0F3)
val ChipIdleText     = TextSecondary

// ─────────────────────────────────────────────
//  Data
// ─────────────────────────────────────────────
enum class Status { UPCOMING, ONGOING, PAST, CANCELLED, ONLINE, OFFLINE }

data class Appointment(
    val id          : Int,
    val doctorName  : String,
    val specialty   : String,
    val date        : String,
    val time        : String,
    val mode        : String,
    val status      : Status,
    val unreadCount : Int = 0
)

val sampleAppointments = listOf(
    Appointment(1,  "Dr. Anjali Sharma",   "Cardiology · General Checkup",          "Today",       "3:00 PM",  "Online", Status.UPCOMING,  unreadCount = 1),
    Appointment(2,  "Dr. Rakesh Gupta",    "Dermatology · Online Consult",           "Today",       "12:47 PM", "Online", Status.ONGOING),
    Appointment(3,  "Dr. Arun Krishnan",   "ENT · Routine Hearing Test",             "Tomorrow",    "10:00 AM", "Clinic", Status.UPCOMING,  unreadCount = 2),
    Appointment(4,  "Dr. Vikram Nair",     "Ophthalmology · Eye Checkup",            "Tomorrow",    "8:00 AM",  "Online", Status.UPCOMING),
    Appointment(5,  "Dr. Sunita Rao",      "Endocrinology · Diabetes Review",        "14 Apr 2026", "11:30 AM", "Online", Status.UPCOMING),
    Appointment(6,  "Dr. Meera Iyer",      "Gynecology · Annual Screening",          "15 Apr 2026", "9:00 AM",  "Clinic", Status.UPCOMING),
    Appointment(7,  "Dr. Kavitha Reddy",   "Nutrition · Diet Planning",              "16 Apr 2026", "6:00 PM",  "Online", Status.UPCOMING),
    Appointment(8,  "Dr. Arjun Menon",     "Gastroenterology · IBS Review",          "17 Apr 2026", "1:00 PM",  "Online", Status.UPCOMING),
    Appointment(9,  "Dr. Pooja Nair",      "Physiotherapy · Knee Rehab",             "18 Apr 2026", "7:00 AM",  "Clinic", Status.UPCOMING),
    Appointment(10, "Dr. Priya Mehta",     "Neurology · Follow-up Visit",            "09 Apr 2026", "11:00 AM", "Clinic", Status.PAST),
    Appointment(11, "Dr. Naina Verma",     "Psychiatry · Mental Health Session",     "05 Apr 2026", "4:00 PM",  "Online", Status.PAST),
    Appointment(12, "Dr. Sanjay Kulkarni", "Cardiology · ECG Review",                "02 Apr 2026", "3:30 PM",  "Online", Status.PAST),
    Appointment(13, "Dr. Meera Iyer",      "Gynecology · Annual Screening",          "03 Apr 2026", "9:30 AM",  "Clinic", Status.PAST),
    Appointment(14, "Dr. Suresh Patel",    "Orthopedics · Bone Density Test",        "07 Apr 2026", "2:00 PM",  "Clinic", Status.CANCELLED),
    Appointment(15, "Dr. Ravi Shankar",    "Pulmonology · Breathing Assessment",     "01 Apr 2026", "10:00 AM", "Online", Status.CANCELLED),
)

val filterTabs = listOf("All", "Upcoming", "Ongoing", "Past", "Cancelled", "Online", "Offline")

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentContent(
    navController: NavController,
    selectedTab : Int,
    onTabSelected: (Int) -> Unit
) {
    var activeFilter    by remember { mutableStateOf("All") }

    val list = remember(activeFilter) {
        if (activeFilter == "All") sampleAppointments
        else sampleAppointments.filter {
            it.status.name.equals(activeFilter, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = ScreenBg,

        // ── Top Bar — title only, no icons ────────
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Appointments",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                },
                // No actions — search bar below handles search
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBg
                )
            )
        },

        bottomBar = {
            BottomNavBar(
                navItems = NavItems.patient,
                selectedIndex  = selectedTab,
                onItemSelected = onTabSelected
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick        = { },
                shape          = CircleShape,
                containerColor = GreenPrimary,
                contentColor   = Color.White
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.plus),
                    contentDescription = "Book Appointment",
                    modifier           = Modifier.size(24.dp)
                )
            }
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(innerPadding)
        ) {
            // Thin line under white top bar
            HorizontalDivider(thickness = 1.dp, color = DividerColor)
            Spacer(Modifier.height(12.dp))

            // Search bar — only search entry point
            SearchBar()
            Spacer(Modifier.height(10.dp))

            // Filter chips
            FilterRow(active = activeFilter, onSelect = { activeFilter = it })
            Spacer(Modifier.height(4.dp))

            // Card list — LazyColumn, no dividers between items
            AppointmentList(list)
        }
    }
}

// ─────────────────────────────────────────────
//  Search Bar
// ─────────────────────────────────────────────
@Composable
fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector        = Icons.Default.Search,
            contentDescription = null,
            tint               = TextHint,
            modifier           = Modifier.size(18.dp)
        )
        Text(
            text       = "Search appointments...",
            color      = TextHint,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────
//  Filter Chips
// ─────────────────────────────────────────────
@Composable
fun FilterRow(active: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filterTabs.forEach { tab ->
            val isActive = tab == active
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) ChipActiveBg else ChipIdleBg)
                    .then(
                        if (isActive) Modifier.border(
                            width = 1.dp,
                            color = ChipActiveBorder,
                            shape = RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = tab,
                    fontSize   = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isActive) ChipActiveText else ChipIdleText
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Appointment List — LazyColumn, no dividers
// ─────────────────────────────────────────────
@Composable
fun AppointmentList(list: List<Appointment>) {
    if (list.isEmpty()) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint               = TextHint,
                    modifier           = Modifier.size(40.dp)
                )
                Text(
                    text       = "No appointments found",
                    color      = TextSecondary,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text       = "Try a different filter",
                    color      = TextHint,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    } else {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(list, key = { it.id }) { appt ->
                AppointmentCard(appt)
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF6F7F9)
@Composable
fun AppointmentPreview() {
    AppointmentContent(navController = rememberNavController(),1,{})
}