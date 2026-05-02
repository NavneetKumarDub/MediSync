package com.example.medisync.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medisync.networks.ProfileModels
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.theme.natureGreen
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    phone        : String,
    role         : String
) {
    var name         by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    val scope        = rememberCoroutineScope()

    Scaffold(containerColor = Color(0xFFFAFBFC)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color(0xFF0F172A)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(natureGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Person,
                        contentDescription = null,
                        tint               = natureGreen,
                        modifier           = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text       = "What's your name?",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF0F172A)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text     = "This is how others will see you on MediSync",
                    fontSize = 13.sp,
                    color    = Color(0xFF64748B)
                )

                Spacer(Modifier.height(40.dp))

                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text       = "Full name",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color(0xFF334155)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = name,
                        onValueChange = {
                            name         = it
                            errorMessage = ""
                        },
                        placeholder = {
                            Text(
                                text     = "Enter your full name",
                                color    = Color(0xFF94A3B8),
                                fontSize = 15.sp
                            )
                        },
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape      = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = natureGreen,
                            unfocusedBorderColor    = Color(0xFFE2E8F0),
                            cursorColor             = natureGreen,
                            focusedTextColor        = Color(0xFF0F172A),
                            unfocusedTextColor      = Color(0xFF0F172A),
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text     = errorMessage,
                        color    = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading    = true
                            errorMessage = ""
                            var userId   = 0
                            try {
                                val response = RetrofitInstance.api.registerUser(
                                    request = ProfileModels(
                                        phone = phone,
                                        name  = name,
                                        role  = role
                                    )
                                )
                                userId = response.user?.id ?: 0
                                isLoading = false
                                val destination = when (role) {
                                    "doctor"  -> "doctorHome/$name/$phone/$userId"
                                    "patient" -> "patientHome/$name/$phone/$userId"
                                    else      -> "selectRole/$phone"   // safety net if role is blank
                                }
                                navController.navigate(destination) {
                                    popUpTo("login") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                isLoading    = false
                                errorMessage = e.message.toString()
                            }
                        }
                    },
                    enabled = name.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = natureGreen,
                        disabledContainerColor = natureGreen.copy(alpha = 0.4f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color       = Color.White,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(22.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text       = "Continue",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White
                            )
                            Icon(
                                imageVector        = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint               = Color.White,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        navController = rememberNavController(),
        phone         = "9122349557",
        role          = "patient"
    )
}