package com.example.medisync

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medisync.ui.screens.doctor.DoctorProfileScreen
import com.example.medisync.ui.screens.auth.LoginScreen
import com.example.medisync.ui.screens.auth.OtpScreen
import com.example.medisync.ui.screens.auth.RegisterScreen
import com.example.medisync.ui.screens.patient.SearchScreen
import com.example.medisync.ui.screens.auth.SelectRoleScreen
import com.example.medisync.ui.screens.chat.ChatScreen
import com.example.medisync.ui.screens.doctor.DoctorNavigationScreen
import com.example.medisync.ui.screens.patient.PatientNavigationScreen
import com.example.medisync.ui.screens.patient.SlotPickerScreen
import com.example.medisync.ui.screens.patient.PatientProfileScreen
import com.example.medisync.ui.screens.video.VideoRoomPermissionGate
import com.example.medisync.ui.theme.MediSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediSyncTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ){
                    composable("login"){
                        LoginScreen(navController = navController)
                    }
                    composable("otp/{verificationId}/{phone}"){ backStackEntry ->
                        OtpScreen(
                            navController = navController,
                            verificationId = backStackEntry.arguments?.getString("verificationId")?:"",
                            phoneNumber = backStackEntry.arguments?.getString("phone") ?:""
                        )
                    }
                    composable("selectRole/{phone}"){backStackEntry ->
                        SelectRoleScreen(
                            navController = navController,
                            phone = backStackEntry.arguments?.getString("phone") ?: ""
                        )
                    }
                    composable("register/{phone}/{role}"){backStackEntry ->
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
                        SearchScreen(
                            navController = navController,
                        )
                    }
                    composable("doctorProfile/{doctorId}") { backStackEntry ->
                        DoctorProfileScreen(
                            doctorId      = backStackEntry.arguments?.getString("doctorId")?.toIntOrNull() ?: 0,
                            navController = navController
                        )
                    }
                    composable("slotPicker/{doctorId}/{doctorName}"){backStackEntry ->
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
                            name          = backStackEntry.arguments?.getString("name") ?: "",
                            phoneNumber   = backStackEntry.arguments?.getString("phone") ?: "",
                            userId        = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                        )
                    }

                    composable("chat/{roomId}") { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getString("roomId")?.toIntOrNull() ?: 0

                        ChatScreen(
                            navController = navController,
                            roomId = roomId
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


                }

            }
        }
    }
}

