package com.example.medisync.ui.screens.patient

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.components.ConsultationCard
import com.example.medisync.ui.components.FindDoctorSection
//import com.example.medisync.ui.components.NextAppointmentCard
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.screens.HomeTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.AppDrawerItem
import com.example.medisync.ui.components.AppSideDrawer

@Composable
fun HomeContent(
    navController: NavController,
    name: String = "",
    phone: String = "",
    userId: Int = 0,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        val token = TokenManager.getToken(context)
        if (token != null && userId != 0) {
            try {
                profilePhotoUrl = RetrofitInstance.api
                    .getProfilePhotoUrl("Bearer $token", userId)
                    .viewUrl
            } catch (e: Exception) {
                profilePhotoUrl = null
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppSideDrawer(
                name = name,
                phone = phone,
                photoUrl = profilePhotoUrl,
                items = listOf(

                    AppDrawerItem(
                        label = "AI Health Chat",
                        icon = Icons.Default.HealthAndSafety,
                        onClick = {
                            scope.launch { drawerState.close() }
                            // navController.navigate("patientAiChat")
                        }
                    ),
                    AppDrawerItem(
                        label = "Medical Records",
                        icon = Icons.Default.Article,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onTabSelected(3)
                        }
                    )
                ),
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    val encodedName = Uri.encode(name)
                    navController.navigate("patientProfile/${encodedName}/$phone/$userId")
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    scope.launch {
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
            containerColor = Color(0xFFF6F7F9),
            topBar = {
                HomeTopBar(
                    location = "Bangalore",
                    onProfileClick = {
                        scope.launch { drawerState.open() }
                    },
                    onLocationClick = { },
                    onNotificationClick = { },
                    onSearchClick = { navController.navigate("search") }
                )
            },
            bottomBar = {
                BottomNavBar(
                    navItems = NavItems.patient,
                    selectedIndex = selectedTab,
                    onItemSelected = onTabSelected
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF6F7F9))
            ) {
                item {
                    GreetingHeader(name)
                }

                item {
                    ConsultationCard(
                        onPhysicalClick = {},
                        onVideoClick = {}
                    )
                }

                item {
                    FindDoctorSection(
                        onSpecialityClick = {},
                        onMoreClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingHeader(
    userName: String,
    modifier: Modifier = Modifier
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val greeting = when {
        hour in 5..11  -> "Good Morning, $userName"
        hour in 12..16 -> "Good Afternoon, $userName"
        hour in 17..20 -> "Good Evening, $userName"
        else           -> "Good Night, $userName"
    }

    val today = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text       = today,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Normal,
            color      = Color(0xFF9CA3AF)
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text       = greeting,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF111827)
        )
    }
}