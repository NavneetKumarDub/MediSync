package com.example.medisync.ui.screens.doctor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.AppDrawerItem
import com.example.medisync.ui.components.AppSideDrawer
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.screens.HomeTopBar
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.viewmodels.AppointmentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.medisync.utils.FileCacheManager

private val DoctorHomeBg = Color(0xFFF6F8FA)
private val CardBg = Color.White
private val CardBorder = Color(0xFFE8EEF3)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF64748B)
private val TextMuted = Color(0xFF94A3B8)

@Composable
fun DoctorHomeContent(
    navController: NavController,
    name: String,
    phone: String,
    userId: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val app = context.applicationContext as MediSyncApplication

    val appointmentViewModel: AppointmentViewModel = viewModel(
        factory = AppointmentViewModel.Factory(app.appointmentRepository)
    )
    val appointments = appointmentViewModel.appointments
    val doctorAppointments = remember(appointments, userId) {
        appointments.filter { appointment ->
            appointment.doctorId == userId ||
                (appointment.doctorId == null && appointment.patientId != null)
        }
    }

    var token by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        appointmentViewModel.fetchDoctorAppointments(context)
        token = TokenManager.getToken(context) ?: ""
    }


    val homeData = remember(doctorAppointments) { DoctorHomeData.from(doctorAppointments) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppSideDrawer(
                name = "Dr. ${name.toDisplayName()}",
                phone = phone,
                userId = userId,
                photoKey = null,
                token = token,
                items = listOf(
                    AppDrawerItem(
                        label = "Clinic Location",
                        icon = Icons.Default.LocationOn,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("doctorClinicLocation")
                        }
                    ),
                    AppDrawerItem(
                        label = "Schedule / Slots",
                        icon = Icons.Default.CalendarToday,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onTabSelected(3)
                        }
                    ),
                    AppDrawerItem(
                        label = "Appointments",
                        icon = Icons.Default.EventNote,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onTabSelected(1)
                        }
                    ),
                    AppDrawerItem(
                        label = "Chat",
                        icon = Icons.Default.Chat,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onTabSelected(2)
                        }
                    )
                ),
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    val encodedName = Uri.encode(name)
                    navController.navigate("doctorProfile/$encodedName/$phone/$userId")
                },
                onLogoutClick = {
                    scope.launch {
                        drawerState.close()
                        TokenManager.clear(context)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = DoctorHomeBg,
            topBar = {
                HomeTopBar(
                    location = "Bangalore",
                    onProfileClick = { scope.launch { drawerState.open() } },
                    onLocationClick = { },
                    onNotificationClick = { },
                    onSearchClick = { }
                )
            },
            bottomBar = {
                BottomNavBar(
                    navItems = NavItems.doctor,
                    selectedIndex = selectedTab,
                    onItemSelected = onTabSelected
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DoctorHomeBg)
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { DoctorGreeting(name = name) }
                item { TodayStatsRow(homeData) }
                item {
                    NextPatientCard(
                        appointment = homeData.nextAppointment,
                        onOpenAppointments = { onTabSelected(1) }
                    )
                }
                item {
                    QuickActionsGrid(
                        onSlotsClick = { onTabSelected(3) },
                        onAppointmentsClick = { onTabSelected(1) },
                        onMessagesClick = { onTabSelected(2) },
                        onClinicClick = { navController.navigate("doctorClinicLocation") }
                    )
                }
                item {
                    UpcomingAppointmentsCard(
                        appointments = homeData.upcomingToday,
                        onViewAll = { onTabSelected(1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DoctorGreeting(name: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11 -> "Good Morning"
        hour in 12..16 -> "Good Afternoon"
        hour in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
    val today = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = today,
            fontSize = 12.sp,
            color = TextMuted
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = "$greeting, Dr. ${name.toDisplayName()}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun TodayStatsRow(data: DoctorHomeData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label = "Today",
            value = data.todayCount.toString(),
            subtitle = "appointments",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Upcoming",
            value = data.upcomingCount.toString(),
            subtitle = "scheduled",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Completed",
            value = data.completedCount.toString(),
            subtitle = "all time",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun NextPatientCard(
    appointment: AppointmentEntity?,
    onOpenAppointments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(title = "Next Patient", action = "View all", onActionClick = onOpenAppointments)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .clickable { onOpenAppointments() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val patientName = appointment?.displayName ?: "No upcoming patient"
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(natureGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = patientName.firstOrNull()?.uppercase() ?: "-",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureGreen
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = patientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = appointment?.type?.toDisplayType() ?: "No appointment scheduled",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                appointment?.let {
                    Text(
                        text = "${it.formatHomeTime()} • ${it.status.toStatusText()}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onSlotsClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onClinicClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(title = "Quick Actions")

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                icon = Icons.Default.EditCalendar,
                title = "Manage Slots",
                tint = Color(0xFF4F46E5),
                onClick = onSlotsClick,
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                icon = Icons.Default.EventNote,
                title = "Appointments",
                tint = Color(0xFF0284C7),
                onClick = onAppointmentsClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                icon = Icons.Default.Chat,
                title = "Messages",
                tint = Color(0xFFEA580C),
                onClick = onMessagesClick,
                modifier = Modifier.weight(1f)
            )
            ActionTile(
                icon = Icons.Default.LocationOn,
                title = "Clinic Location",
                tint = Color(0xFF059669),
                onClick = onClinicClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(11.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun UpcomingAppointmentsCard(
    appointments: List<AppointmentEntity>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(title = "Upcoming Today", action = "View all", onActionClick = onViewAll)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
        ) {
            if (appointments.isEmpty()) {
                EmptyUpcomingRow()
            } else {
                appointments.take(3).forEachIndexed { index, appointment ->
                    UpcomingRow(appointment = appointment)
                    if (index != appointments.take(3).lastIndex) {
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        if (action != null && onActionClick != null) {
            Text(
                text = action,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = natureGreen,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
private fun EmptyUpcomingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "No appointments today",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Your upcoming visits will appear here",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun UpcomingRow(appointment: AppointmentEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(natureGreen.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appointment.displayName.firstOrNull()?.uppercase() ?: "-",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = natureGreen
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appointment.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = appointment.type.toDisplayType(),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(text = "·", color = TextMuted)
                Text(
                    text = appointment.status.toStatusText(),
                    fontSize = 11.sp,
                    color = appointment.statusColor(),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = appointment.formatHomeTime(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

private data class DoctorHomeData(
    val todayCount: Int,
    val upcomingCount: Int,
    val completedCount: Int,
    val nextAppointment: AppointmentEntity?,
    val upcomingToday: List<AppointmentEntity>
) {
    companion object {
        fun from(appointments: List<AppointmentEntity>): DoctorHomeData {
            val now = System.currentTimeMillis()
            val today = appointments.filter { it.isToday() }
            val upcoming = appointments
                .filter { appointment ->
                    !appointment.status.equals("completed", ignoreCase = true) &&
                        (appointment.startMillis() ?: Long.MAX_VALUE) >= now
                }
                .sortedBy { it.startMillis() ?: Long.MAX_VALUE }
            val upcomingToday = today
                .filter { !it.status.equals("completed", ignoreCase = true) }
                .sortedBy { it.startMillis() ?: Long.MAX_VALUE }

            return DoctorHomeData(
                todayCount = today.size,
                upcomingCount = upcoming.size,
                completedCount = appointments.count { it.status.equals("completed", ignoreCase = true) },
                nextAppointment = upcoming.firstOrNull(),
                upcomingToday = upcomingToday
            )
        }
    }
}

private fun AppointmentEntity.isToday(): Boolean {
    val datePart = date.substringBefore("T")
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return datePart == today
}

private fun AppointmentEntity.startMillis(): Long? {
    val datePart = date.substringBefore("T")
    val cleanTime = time.substringBefore(".").substringBefore("Z")
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd h:mm a"
    )

    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.getDefault())
                .parse("$datePart $cleanTime")
                ?.time
        }.getOrNull()
    }
}

private fun AppointmentEntity.formatHomeTime(): String {
    val millis = startMillis()
    return if (millis != null) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    } else {
        time.substringBefore(":").padStart(2, '0') + ":" + time.substringAfter(":", "00").take(2)
    }
}

private fun String.toDisplayType(): String {
    return when (lowercase(Locale.getDefault())) {
        "online" -> "Online"
        "offline" -> "Offline"
        "in_person" -> "Offline"
        else -> replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
}

private fun String.toStatusText(): String {
    return replace("_", " ").replaceFirstChar { it.titlecase(Locale.getDefault()) }
}

private fun AppointmentEntity.statusColor(): Color {
    return when (status.lowercase(Locale.getDefault())) {
        "accepted" -> Color(0xFF0284C7)
        "completed" -> Color(0xFF059669)
        "cancelled" -> Color(0xFFDC2626)
        else -> Color(0xFF7C3AED)
    }
}

private fun String.toDisplayName(): String {
    return trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase(Locale.getDefault())
                .replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
}
