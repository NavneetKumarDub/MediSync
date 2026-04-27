package com.example.medisync.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.components.RoleCard
import com.example.medisync.ui.theme.natureGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectRoleScreen(
    navController: NavController,
    phone        : String
) {
    var selectedRole by remember { mutableStateOf("") }

    Scaffold(containerColor = Color(0xFFFAFBFC)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Back ─────────────────────────────
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
                    .padding(horizontal = 28.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // ── Icon ─────────────────────────
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(natureGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Person,
                        contentDescription = null,
                        tint               = natureGreen,
                        modifier           = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text       = "Who are you?",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF0F172A)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text     = "Choose your role to get started",
                    fontSize = 13.sp,
                    color    = Color(0xFF64748B)
                )

                Spacer(Modifier.height(40.dp))

                // ── Role cards ───────────────────
                RoleCard(
                    title      = "Doctor",
                    subtitle   = "Healthcare provider",
                    icon       = Icons.Outlined.MedicalServices,
                    iconBg     = natureGreen.copy(alpha = 0.12f),
                    iconTint   = natureGreen,
                    isSelected = selectedRole == "doctor",
                    onClick    = { selectedRole = "doctor" }
                )

                Spacer(Modifier.height(12.dp))

                RoleCard(
                    title      = "Patient",
                    subtitle   = "Seeking care",
                    icon       = Icons.Outlined.Person,
                    iconBg     = Color(0xFFF1F5F9),
                    iconTint   = Color(0xFF475569),
                    isSelected = selectedRole == "patient",
                    onClick    = { selectedRole = "patient" }
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text       = "This cannot be changed later",
                    fontSize   = 11.sp,
                    color      = Color(0xFF94A3B8),
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Button(
                    onClick = { navController.navigate("register/$phone/$selectedRole") },
                    enabled = selectedRole.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = natureGreen,
                        disabledContainerColor = natureGreen.copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation  = 0.dp,
                        pressedElevation  = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Text(
                        text       = "Continue",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RolePreview() {
    SelectRoleScreen(
        navController = rememberNavController(),
        phone         = "9122349557"
    )
}