package com.example.medisync

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.ChatNotificationManager
import com.example.medisync.networks.ChatWebSocketManager
import com.example.medisync.ui.screens.auth.LoginScreen
import com.example.medisync.ui.screens.auth.OtpScreen
import com.example.medisync.ui.screens.auth.RegisterScreen
import com.example.medisync.ui.screens.auth.SelectRoleScreen
import com.example.medisync.ui.screens.chat.ChatScreen
import com.example.medisync.ui.screens.doctor.DoctorCustomScheduleScreen
import com.example.medisync.ui.screens.doctor.DoctorNavigationScreen
import com.example.medisync.ui.screens.doctor.DoctorProfileScreen
import com.example.medisync.ui.screens.doctor.DoctorRegularSlotsManage
import com.example.medisync.ui.screens.doctor.DoctorScheduleScreen
import com.example.medisync.ui.screens.patient.PatientNavigationScreen
import com.example.medisync.ui.screens.patient.PatientProfileScreen
import com.example.medisync.ui.screens.patient.SearchScreen
import com.example.medisync.ui.screens.patient.SlotPickerScreen
import com.example.medisync.ui.screens.video.VideoRoomPermissionGate
import com.example.medisync.ui.theme.MediSyncTheme

class MainActivity : ComponentActivity() {

    private var pendingNotificationIntent by mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationIntent = intent
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediSyncTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()

                val app = context.applicationContext as MediSyncApplication
                val notificationManager = remember {
                    ChatNotificationManager(app.chatInboxRepository, context)
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    val token = TokenManager.getToken(context)
                    if (token != null) {
                        ChatWebSocketManager.connect(context)
                        notificationManager.startListening(scope)
                    } else {
                        notificationManager.stopListening()
                        ChatWebSocketManager.disconnect()
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "auth_check"
                ) {
                    composable("auth_check") {
                        LaunchedEffect(Unit) {
                            val token = TokenManager.getToken(context)
                            val role = TokenManager.getRole(context)
                            val userId = TokenManager.getUserId(context)
                            val name = TokenManager.getName(context)
                            val phone = TokenManager.getPhone(context)

                            val notificationType = intent.getStringExtra("notification_type")
                            val notificationRoomId = intent.getStringExtra("roomId")?.toIntOrNull()

                            if (token != null && role != null && userId != null) {
                                ChatWebSocketManager.connect(context)

                                if (notificationType == "chat_message" && notificationRoomId != null) {
                                    navController.navigate(
                                        "chat/$notificationRoomId?name=${Uri.encode("Unknown")}&photoUrl="
                                    ) {
                                        popUpTo("auth_check") { inclusive = true }
                                    }
                                } else {
                                    when (role) {
                                        "patient" -> {
                                            navController.navigate("patientHome/$name/$phone/$userId") {
                                                popUpTo("auth_check") { inclusive = true }
                                            }
                                        }

                                        "doctor" -> {
                                            navController.navigate("doctorHome/$name/$phone/$userId") {
                                                popUpTo("auth_check") { inclusive = true }
                                            }
                                        }

                                        else -> {
                                            navController.navigate("login") {
                                                popUpTo("auth_check") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            } else {
                                navController.navigate("login") {
                                    popUpTo("auth_check") { inclusive = true }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    composable("login") {
                        LoginScreen(navController = navController)
                    }

                    composable("otp/{verificationId}/{phone}") { backStackEntry ->
                        OtpScreen(
                            navController = navController,
                            verificationId = backStackEntry.arguments?.getString("verificationId") ?: "",
                            phoneNumber = backStackEntry.arguments?.getString("phone") ?: ""
                        )
                    }

                    composable("selectRole/{phone}") { backStackEntry ->
                        SelectRoleScreen(
                            navController = navController,
                            phone = backStackEntry.arguments?.getString("phone") ?: ""
                        )
                    }

                    composable("register/{phone}/{role}") { backStackEntry ->
                        RegisterScreen(
                            navController = navController,
                            phone = backStackEntry.arguments?.getString("phone") ?: "",
                            role = backStackEntry.arguments?.getString("role") ?: ""
                        )
                    }

                    composable("patientHome/{name}/{phone}/{userId}") { backStackEntry ->
                        PatientNavigationScreen(
                            navController = navController,
                            name = backStackEntry.arguments?.getString("name") ?: "",
                            phone = backStackEntry.arguments?.getString("phone") ?: "",
                            userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }

                    composable("patientProfile/{name}/{phone}/{userId}") { backStackEntry ->
                        PatientProfileScreen(
                            navController = navController,
                            name = backStackEntry.arguments?.getString("name") ?: "",
                            phoneNumber = backStackEntry.arguments?.getString("phone") ?: "",
                            userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }

                    composable("search") {
                        SearchScreen(navController = navController)
                    }

                    composable("doctorProfile/{doctorId}") { backStackEntry ->
                        DoctorProfileScreen(
                            doctorId = backStackEntry.arguments?.getString("doctorId")?.toIntOrNull() ?: 0,
                            navController = navController
                        )
                    }

                    composable("slotPicker/{doctorId}/{doctorName}") { backStackEntry ->
                        SlotPickerScreen(
                            doctorId = backStackEntry.arguments?.getString("doctorId")?.toIntOrNull() ?: 0,
                            doctorName = backStackEntry.arguments?.getString("doctorName") ?: "",
                            navController = navController
                        )
                    }

                    composable("doctorHome/{name}/{phone}/{userId}") { backStackEntry ->
                        DoctorNavigationScreen(
                            navController = navController,
                            name = backStackEntry.arguments?.getString("name") ?: "",
                            phone = backStackEntry.arguments?.getString("phone") ?: "",
                            userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }

                    composable("doctorProfile/{name}/{phone}/{userId}") { backStackEntry ->
                        DoctorProfileScreen(
                            navController = navController,
                            name = backStackEntry.arguments?.getString("name") ?: "",
                            phoneNumber = backStackEntry.arguments?.getString("phone") ?: "",
                            userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }

                    composable(
                        route = "chat/{roomId}?name={name}&photoUrl={photoUrl}",
                        arguments = listOf(
                            navArgument("roomId") { type = NavType.IntType },
                            navArgument("name") {
                                type = NavType.StringType
                                defaultValue = "Unknown User"
                            },
                            navArgument("photoUrl") {
                                type = NavType.StringType
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getInt("roomId") ?: 0
                        val otherUserName = backStackEntry.arguments?.getString("name") ?: "Unknown User"
                        val photoUrl = backStackEntry.arguments?.getString("photoUrl")

                        ChatScreen(
                            navController = navController,
                            roomId = roomId,
                            otherUserName = otherUserName,
                            photoUrl = photoUrl
                        )
                    }

                    composable("video_room/{roomId}") { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getString("roomId")?.toIntOrNull() ?: 0

                        VideoRoomPermissionGate(
                            navController = navController,
                            roomId = roomId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("doctorSchedule/{selectedTab}/{userId}") { backStackEntry ->
                        DoctorScheduleScreen(
                            navController = navController,
                            selectedTab = backStackEntry.arguments?.getString("selectedTab")?.toIntOrNull() ?: 0,
                            onTabSelected = { },
                            userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }

                    composable("doctorWeeklyTemplate/{userId}") { backStackEntry ->
                        DoctorRegularSlotsManage(
                            navController = navController,
                            userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }

                    composable("CustomEditSlot/{userId}") { backStackEntry ->
                        DoctorCustomScheduleScreen(
                            navController = navController,
                            userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }
                }

                LaunchedEffect(pendingNotificationIntent) {
                    val clickIntent = pendingNotificationIntent ?: return@LaunchedEffect
                    val notificationType = clickIntent.getStringExtra("notification_type")
                    val roomId = clickIntent.getStringExtra("roomId")?.toIntOrNull()

                    if (notificationType == "chat_message" && roomId != null) {
                        val token = TokenManager.getToken(context)
                        if (token != null) {
                            navController.navigate(
                                "chat/$roomId?name=${Uri.encode("Unknown")}&photoUrl="
                            )
                            pendingNotificationIntent = null
                        }
                    }
                }
            }
        }
    }
}