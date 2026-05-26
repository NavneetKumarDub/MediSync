package com.example.medisync.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.ProfileModels
import com.example.medisync.networks.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    phone: String,
    role: String
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val roleLabel = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    Scaffold(containerColor = AuthBg) { padding ->
        AuthScreenFrame(
            showBack = true,
            onBack = { navController.popBackStack() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(112.dp))

                AuthHeader(
                    title = "Create profile",
                    subtitle = "$roleLabel account for +91 $phone",
                    showLogo = true
                )

                Spacer(Modifier.height(42.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Full name",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = AuthTextSecondary
                            )
                        },
                        placeholder = {
                            Text("Enter your full name", color = Color(0xFF94A3B8), fontSize = 15.sp)
                        },
                        colors = authTextFieldColors()
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                AuthPrimaryButton(
                    text = if (isLoading) "Saving" else "Continue",
                    enabled = name.isNotBlank() && !isLoading,
                    loading = isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = ""
                            try {
                                val response = RetrofitInstance.api.registerUser(
                                    request = ProfileModels(
                                        phone = phone,
                                        name = name.trim(),
                                        role = role
                                    )
                                )
                                val userId = response.user?.id ?: 0
                                TokenManager.saveUserId(context, userId)
                                TokenManager.saveRole(context, role)
                                TokenManager.saveName(context, name.trim())
                                TokenManager.savePhone(context, phone)

                                isLoading = false
                                val destination = when (role) {
                                    "doctor" -> "doctorHome/${name.trim()}/$phone/$userId"
                                    "patient" -> "patientHome/${name.trim()}/$phone/$userId"
                                    else -> "selectRole/$phone"
                                }
                                navController.navigate(destination) {
                                    popUpTo("login") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.message ?: "Unable to create profile"
                            }
                        }
                    },
                    trailing = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        navController = rememberNavController(),
        phone = "9122349557",
        role = "patient"
    )
}
