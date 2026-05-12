//package com.example.medisync.ui.screens.patient
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.outlined.CalendarMonth
//import androidx.compose.material.icons.outlined.Visibility
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.example.medisync.R
//import com.example.medisync.networks.AppointmentItem
//import com.example.medisync.ui.components.AppointmentCard
//import com.example.medisync.ui.components.BottomNavBar
//import com.example.medisync.ui.components.SearchBar
//import com.example.medisync.ui.navigation.NavItems
//import com.example.medisync.viewmodels.AppointmentViewModel
//import com.example.medisync.ui.theme.natureGreen // Assuming this is your primary green
//import java.time.LocalDate
//
//// ─────────────────────────────────────────────
////  WhatsApp-Style Filter Tabs
//// ─────────────────────────────────────────────
//val filterTabs = listOf("All", "Upcoming", "Past", "Online", "Offline")
//
//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AppointmentContent(
//    navController: NavController,
//    selectedTab: Int,
//    onTabSelected: (Int) -> Unit,
//    viewModel: AppointmentViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val today = LocalDate.now()
//
//    // 1. Popup State (Just like ChatListScreen)
//    var showAvatarDialog by remember { mutableStateOf(false) }
//    var selectedAvatarUrl by remember { mutableStateOf<String?>(null) }
//    var selectedAvatarName by remember { mutableStateOf("") }
//
//    var activeFilter by remember { mutableStateOf("All") }
//
//    LaunchedEffect(Unit) {
//        viewModel.fetchPatientAppointments(context)
//    }
//
//    val appointments = viewModel.appointments
//    val isLoading = viewModel.isLoading
//
//    // 2. Logic: Split and Sort Appointments
//    val list = remember(activeFilter, appointments) {
//        val upcoming = appointments.filter {
//            val date = try { LocalDate.parse(it.date) } catch(e: Exception) { today }
//            !date.isBefore(today)
//        }.sortedBy { it.date } // Soonest first
//
//        val past = appointments.filter {
//            val date = try { LocalDate.parse(it.date) } catch(e: Exception) { today }
//            date.isBefore(today)
//        }.sortedByDescending { it.date } // Most recent first
//
//        when (activeFilter) {
//            "Upcoming" -> upcoming
//            "Past" -> past
//            "Online" -> (upcoming + past).filter { it.type?.contains("video", true) == true }
//            "Offline" -> (upcoming + past).filter { it.type?.contains("video", true) == false }
//            else -> upcoming + past // All
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Scaffold(
//            containerColor = Color.White,
//            topBar = {
//                TopAppBar(
//                    title = {
//                        Text(
//                            text = "Appointments",
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF111827)
//                        )
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
//                    // Optional: You could add a Profile icon here later
//                )
//            },
//            bottomBar = {
//                BottomNavBar(
//                    navItems = NavItems.patient,
//                    selectedIndex = selectedTab,
//                    onItemSelected = onTabSelected
//                )
//            },
//            floatingActionButton = {
//                FloatingActionButton(
//                    onClick = { /* Book Logic */ },
//                    shape = CircleShape,
//                    containerColor = natureGreen,
//                    contentColor = Color.White
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.plus),
//                        contentDescription = "Book Appointment",
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//        ) { innerPadding ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//            ) {
//                SearchBar()
//                Spacer(Modifier.height(8.dp))
//                // WhatsApp Style Filter Row
//                FilterRow(active = activeFilter, onSelect = { activeFilter = it })
//
//                Spacer(Modifier.height(8.dp))
//
//                if (isLoading) {
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator(color = natureGreen)
//                    }
//                } else {
//                    AppointmentList(
//                        list = list,
//                        onAvatarClick = { name, url ->
//                            selectedAvatarName = name
//                            selectedAvatarUrl = url
//                            showAvatarDialog = true
//                        },
//                        onCardClick = { }
//                    )
//                }
//            }
//        }
//
//        // 3. Avatar Popup Overlay
//        if (showAvatarDialog) {
//            // Re-using the same Popup Component we discussed for ChatList
//            AvatarPopup(
//                name = selectedAvatarName,
//                url = selectedAvatarUrl,
//                onDismiss = { showAvatarDialog = false },
//                onViewDetails = {}
//            )
//        }
//    }
//}
//
//
//
//@Composable
//fun FilterRow(active: String, onSelect: (String) -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .horizontalScroll(rememberScrollState())
//            .padding(horizontal = 16.dp, vertical = 4.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        filterTabs.forEach { tab ->
//            val isActive = tab == active
//            Box(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(if (isActive) Color(0xFFE7F5EE) else Color(0xFFF0F2F5))
//                    .clickable { onSelect(tab) }
//                    .padding(horizontal = 16.dp, vertical = 6.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = tab,
//                    fontSize = 14.sp,
//                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
//                    color = if (isActive) natureGreen else Color(0xFF6B7280)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun AppointmentList(
//    list: List<AppointmentItem>,
//    onAvatarClick: (String, String?) -> Unit,
//    onCardClick: (AppointmentItem) -> Unit
//) {
//    if (list.isEmpty()) {
//        EmptyState()
//    } else {
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(bottom = 100.dp)
//        ) {
//            items(list, key = { it.appointmentId }) { appt ->
//                AppointmentCard(
//                    appt = appt,
//                    onAvatarClick = { name, url -> onAvatarClick(name, url) },
//                    onClick = { onCardClick(appt) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun EmptyState() {
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Icon(
//                imageVector = Icons.Outlined.CalendarMonth,
//                contentDescription = null,
//                tint = Color(0xFFD1D5DB),
//                modifier = Modifier.size(60.dp)
//            )
//            Spacer(Modifier.height(12.dp))
//            Text("No appointments found", color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
//        }
//    }
//}
//
//@Composable
//fun AvatarPopup(
//    name: String,
//    url: String?,
//    onDismiss: () -> Unit,
//    onViewDetails: () -> Unit
//) {
//    // Dimmed background overlay
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = 0.6f))
//            .clickable { onDismiss() },
//        contentAlignment = Alignment.Center
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth(0.65f) // Your exact width
//                .wrapContentHeight()
//                .offset(y = (-80).dp) // Your exact offset
//                .clickable(enabled = false) { },
//            shape = RoundedCornerShape(4.dp),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
//            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//        ) {
//            Column {
//                // 1. Image / Fallback Section
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .aspectRatio(1f)
//                ) {
//                    if (url != null) {
//                        AsyncImage(
//                            model = url,
//                            contentDescription = "Doctor Photo",
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier.fillMaxSize()
//                        )
//                    } else {
//                        // Fallback using your MediSkyBlue colors
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(Color(0xFFE1F5FE)), // MediSkyBlueSoftBg
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = name.take(1).uppercase(),
//                                fontSize = 100.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF0288D1) // MediSkyBlueText
//                            )
//                        }
//                    }
//
//                    // Top Name Bar (Semi-transparent)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.Black.copy(alpha = 0.25f))
//                            .align(Alignment.TopCenter)
//                            .padding(horizontal = 12.dp, vertical = 6.dp)
//                    ) {
//                        Text(
//                            text = name,
//                            color = Color.White,
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                }
//
//                // 2. Action Bar (White bottom strip)
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color.White)
//                        .padding(vertical = 12.dp),
//                    horizontalArrangement = Arrangement.SpaceEvenly,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // View Details Action
//                    Icon(
//                        imageVector = Icons.Outlined.Visibility,
//                        contentDescription = "View",
//                        tint = natureGreen,
//                        modifier = Modifier
//                            .size(24.dp)
//                            .clickable {
//                                onDismiss()
//                                onViewDetails()
//                            }
//                    )
//
//
//
//                    // Info/Profile Action
//                    Icon(
//                        imageVector = Icons.Default.Info,
//                        contentDescription = "Info",
//                        tint = natureGreen,
//                        modifier = Modifier
//                            .size(24.dp)
//                            .clickable { onDismiss() }
//                    )
//                }
//            }
//        }
//    }
//}