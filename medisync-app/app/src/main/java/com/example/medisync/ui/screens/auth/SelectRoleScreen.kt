package com.example.medisync.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.theme.natureGreen

@Composable
fun SelectRoleScreen(
    navController: NavController,
    phone: String
) {
    var selectedRole by remember { mutableStateOf("") }

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
                Spacer(Modifier.height(106.dp))

                AuthHeader(
                    title = "Choose your role",
                    subtitle = "MediSync will tailor your workspace",
                    showLogo = true
                )

                Spacer(Modifier.height(38.dp))

                AuthRoleCard(
                    title = "Doctor",
                    subtitle = "Manage appointments, slots, reports, and patient chats",
                    icon = Icons.Outlined.MedicalServices,
                    isSelected = selectedRole == "doctor",
                    onClick = { selectedRole = "doctor" }
                )

                Spacer(Modifier.height(14.dp))

                AuthRoleCard(
                    title = "Patient",
                    subtitle = "Book consultations, store records, and chat with doctors",
                    icon = Icons.Outlined.Person,
                    isSelected = selectedRole == "patient",
                    onClick = { selectedRole = "patient" }
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "Choose carefully. This role shapes your app experience.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                AuthPrimaryButton(
                    text = "Continue",
                    enabled = selectedRole.isNotEmpty(),
                    onClick = { navController.navigate("register/$phone/$selectedRole") }
                )

                Spacer(Modifier.height(26.dp))
            }
        }
    }
}

@Composable
private fun AuthRoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val border = if (isSelected) natureGreen else AuthBorder
    val background = if (isSelected) Color(0xFFE0F2FE) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFFE0F2FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = natureGreen,
                modifier = Modifier.size(25.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AuthTextPrimary
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = AuthTextSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) natureGreen else Color.Transparent)
                .border(1.dp, if (isSelected) natureGreen else AuthBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RolePreview() {
    SelectRoleScreen(
        navController = rememberNavController(),
        phone = "9122349557"
    )
}
