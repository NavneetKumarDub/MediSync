package com.example.medisync.ui.screens.appointment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.AppointmentCard
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.components.SearchBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.viewmodels.AppointmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val MediTextMuted        = Color(0xFF6B7280)
private val MediChipActiveBg     = natureGreen.copy(alpha = 0.1f)
private val MediChipActiveBorder = natureGreen.copy(alpha = 0.3f)
private val MediChipActiveText   = natureGreen
private val MediChipIdleBg       = Color(0xFFF3F4F6)
private val MediChipIdleText     = MediTextMuted
private val filterTabs           = listOf("All", "Upcoming", "Past", "Online", "Offline")

@RequiresApi(Build.VERSION_CODES.O)
fun getSmartDateLabel(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "No Date"
    return try {
        val date  = LocalDate.parse(rawDate)
        val today = LocalDate.now()
        val diff  = ChronoUnit.DAYS.between(today, date)
        when (diff) {
            0L   -> "Today"
            1L   -> "Tomorrow"
            -1L  -> "Yesterday"
            else -> {
                val pattern = if (date.year == today.year) "d MMM" else "d MMM yyyy"
                date.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
            }
        }
    } catch (e: Exception) {
        rawDate
    }
}

fun formatSmartDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "No Date"
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getSmartDateLabel(rawDate)
    } else {
        rawDate
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(
    navController: NavController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: AppointmentViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as MediSyncApplication)
            .let { AppointmentViewModel.Factory(it.appointmentRepository) }
    )
) {
    val context = LocalContext.current
    val today   = LocalDate.now()

    var userRole           by remember { mutableStateOf("patient") }
    var activeFilter       by remember { mutableStateOf("All") }
    var showAvatarDialog   by remember { mutableStateOf(false) }
    var selectedAvatarUrl  by remember { mutableStateOf<String?>(null) }
    var selectedAvatarName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userRole = TokenManager.getRole(context) ?: "patient"
        if (viewModel.appointments.isEmpty()) {
            viewModel.isLoading = true  // only show spinner if no cached data
        }
        if (userRole == "doctor") viewModel.fetchDoctorAppointments(context)
        else viewModel.fetchPatientAppointments(context)
    }

    val appointments = viewModel.appointments
    val navItems     = if (userRole == "doctor") NavItems.doctor else NavItems.patient

    val filteredList = remember(activeFilter, appointments) {
        val upcoming = appointments.filter {
            val d = try { LocalDate.parse(it.date) } catch (e: Exception) { today }
            !d.isBefore(today)
        }.sortedBy { it.date }

        val past = appointments.filter {
            val d = try { LocalDate.parse(it.date) } catch (e: Exception) { today }
            d.isBefore(today)
        }.sortedByDescending { it.date }

        when (activeFilter) {
            "Upcoming" -> upcoming
            "Past"     -> past
            "Online"   -> (upcoming + past).filter { it.type.equals("online", ignoreCase = true) }
            "Offline"  -> (upcoming + past).filter { it.type.contains("offline", ignoreCase = true) }
            else       -> upcoming + past
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text       = "Appointments",
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color      = natureGreen
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomNavBar(
                    navItems      = navItems,
                    selectedIndex = selectedTab,
                    onItemSelected = onTabSelected
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Spacer(Modifier.height(8.dp))
                SearchBar()
                Spacer(Modifier.height(12.dp))
                FilterRow(active = activeFilter, onSelect = { activeFilter = it })
                Spacer(Modifier.height(8.dp))

                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = natureGreen)
                    }
                } else {
                    AppointmentLazyList(
                        list         = filteredList,
                        onAvatarClick = { name, url ->
                            selectedAvatarName = name
                            selectedAvatarUrl  = url
                            showAvatarDialog   = true
                        },
                        onCardClick  = { }
                    )
                }
            }
        }

        if (showAvatarDialog) {
            AvatarPopup(
                name      = selectedAvatarName,
                url       = selectedAvatarUrl,
                onDismiss = { showAvatarDialog = false }
            )
        }
    }
}

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
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isActive) MediChipActiveBg else MediChipIdleBg)
                    .then(
                        if (isActive) Modifier.border(
                            width = 1.dp,
                            color = MediChipActiveBorder,
                            shape = RoundedCornerShape(24.dp)
                        ) else Modifier
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = tab,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (isActive) MediChipActiveText else MediChipIdleText
                )
            }
        }
    }
}

@Composable
fun AppointmentLazyList(
    list          : List<AppointmentEntity>,
    onAvatarClick : (String, String?) -> Unit,
    onCardClick   : (AppointmentEntity) -> Unit
) {
    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No appointments found", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(list, key = { it.id }) { appt ->
                AppointmentCard(
                    appt         = appt,
                    onAvatarClick = onAvatarClick,
                    onClick       = { onCardClick(appt) }
                )
            }
        }
    }
}

@Composable
fun AvatarPopup(name: String, url: String?, onDismiss: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .wrapContentHeight()
                .offset(y = (-80).dp)
                .clickable(enabled = false) { },
            shape  = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    if (url != null) {
                        AsyncImage(
                            model = "${RetrofitInstance.MINIO_BASE_URL}${url}",
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(Color(0xFFE1F5FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                name.take(1).uppercase(),
                                fontSize   = 100.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF0288D1)
                            )
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.25f))
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Row(
                    modifier              = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Visibility, null, tint = natureGreen, modifier = Modifier.size(24.dp).clickable { onDismiss() })
                    Icon(Icons.Default.Info, null, tint = natureGreen, modifier = Modifier.size(24.dp).clickable { onDismiss() })
                }
            }
        }
    }
}