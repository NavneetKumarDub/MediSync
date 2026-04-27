package com.example.medisync.ui.screens.doctor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.screens.HomeTopBar
import com.example.medisync.ui.theme.natureGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DoctorHomeContent(
    navController: NavController,
    name         : String,
    phone        : String,
    userId       : Int,
    selectedTab  : Int,
    onTabSelected: (Int) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF6F7F9),
        topBar = {
            HomeTopBar(
                location            = "Bangalore",
                onProfileClick      = {
                    val encodedName = Uri.encode(name)
                    navController.navigate("doctorProfile/$encodedName/$phone/$userId")
                },
                onLocationClick     = { },
                onNotificationClick = { },
                onSearchClick       = { }
            )
        },
        bottomBar = {
            BottomNavBar(
                navItems       = NavItems.doctor,
                selectedIndex  = selectedTab,
                onItemSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF6F7F9)),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { DoctorGreeting(name = name) }
            item { TodayStatsRow() }
            item { NextPatientCard() }
            item { QuickActionsGrid() }
            item { RecentAppointmentsCard() }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

// ── Greeting ───────────────────────────────────
@Composable
private fun DoctorGreeting(name: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11  -> "Good Morning"
        hour in 12..16 -> "Good Afternoon"
        hour in 17..20 -> "Good Evening"
        else           -> "Good Night"
    }
    val today = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text     = today,
            fontSize = 12.sp,
            color    = Color(0xFF9CA3AF)
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text       = "$greeting, Dr. $name",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF111827)
        )
    }
}

// ── Today's Stats ──────────────────────────────
@Composable
private fun TodayStatsRow() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label    = "Today",
            value    = "8",
            subtitle = "appointments",
            icon     = Icons.Default.CalendarToday,
            tint     = natureGreen,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label    = "Pending",
            value    = "3",
            subtitle = "to confirm",
            icon     = Icons.Default.Schedule,
            tint     = Color(0xFFD97706),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label    = "Completed",
            value    = "42",
            subtitle = "this week",
            icon     = Icons.Default.CheckCircle,
            tint     = Color(0xFF2563EB),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label    : String,
    value    : String,
    subtitle : String,
    icon     : ImageVector,
    tint     : Color,
    modifier : Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(16.dp)
            )
        }
        Text(
            text       = value,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF111827)
        )
        Text(
            text     = label,
            fontSize = 11.sp,
            color    = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
        Text(
            text     = subtitle,
            fontSize = 10.sp,
            color    = Color(0xFF94A3B8)
        )
    }
}

// ── Next Patient ───────────────────────────────
@Composable
private fun NextPatientCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text       = "Next Patient",
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF111827),
            modifier   = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(natureGreen)
                .padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "R",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text       = "Rohan Verma",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
                Text(
                    text     = "Fever, body ache",
                    fontSize = 12.sp,
                    color    = Color.White.copy(alpha = 0.8f)
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint               = Color.White.copy(alpha = 0.85f),
                        modifier           = Modifier.size(12.dp)
                    )
                    Text(
                        text     = "in 25 minutes",
                        fontSize = 11.sp,
                        color    = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text       = "Start",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = natureGreen
                )
            }
        }
    }
}

// ── Quick Actions ──────────────────────────────
@Composable
private fun QuickActionsGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text       = "Quick Actions",
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF111827),
            modifier   = Modifier.padding(bottom = 10.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                icon     = Icons.Default.EditCalendar,
                title    = "Manage\nSlots",
                bg       = Color(0xFFEEF2FF),
                tint     = Color(0xFF4F46E5),
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                icon     = Icons.Default.People,
                title    = "My\nPatients",
                bg       = Color(0xFFECFDF5),
                tint     = Color(0xFF059669),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                icon     = Icons.Default.ChatBubbleOutline,
                title    = "Messages",
                bg       = Color(0xFFFFF7ED),
                tint     = Color(0xFFEA580C),
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                icon     = Icons.Default.Analytics,
                title    = "Earnings",
                bg       = Color(0xFFFCE7F3),
                tint     = Color(0xFFDB2777),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionTile(
    icon    : ImageVector,
    title   : String,
    bg      : Color,
    tint    : Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(20.dp)
            )
        }
        Text(
            text       = title,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF111827),
            lineHeight = 16.sp
        )
    }
}

// ── Recent Appointments ────────────────────────
@Composable
private fun RecentAppointmentsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = "Upcoming Today",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF111827)
            )
            Text(
                text       = "View all",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = natureGreen,
                modifier   = Modifier.clickable { }
            )
        }
        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            UpcomingRow("Priya Shah",   "General consult",  "3:30 PM", "online")
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            UpcomingRow("Arjun Mehta",  "Follow-up",        "4:00 PM", "offline")
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            UpcomingRow("Neha Kapoor",  "Skin consult",     "4:30 PM", "online")
        }
    }
}

@Composable
private fun UpcomingRow(
    patient: String,
    reason : String,
    time   : String,
    mode   : String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(natureGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = patient.first().toString(),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = natureGreen
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = patient,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF111827)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text     = reason,
                    fontSize = 12.sp,
                    color    = Color(0xFF64748B)
                )
                Text(text = "·", color = Color(0xFF94A3B8))
                Text(
                    text     = mode,
                    fontSize = 11.sp,
                    color    = if (mode == "online") Color(0xFF059669) else Color(0xFF334155),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text       = time,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF111827)
        )
    }
}