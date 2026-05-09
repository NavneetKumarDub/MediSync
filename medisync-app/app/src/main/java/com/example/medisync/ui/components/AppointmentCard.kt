package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.medisync.networks.AppointmentItem
import com.example.medisync.ui.theme.natureGreen


@Composable
fun AppointmentCard(
    appt: AppointmentItem,
    onAvatarClick: (name: String, photoUrl: String?) -> Unit,
    onClick: () -> Unit
) {
    val isOnline = appt.type?.contains("video", ignoreCase = true) == true
    val statusText = if (isOnline) "Online" else "Offline"
    val statusColor = if (isOnline) natureGreen else Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1F5FE))
                    .clickable { onAvatarClick(appt.displayName ?: "User", appt.profilePhoto) },
                contentAlignment = Alignment.Center
            ) {
                if (appt.profilePhoto != null) {
                    AsyncImage(
                        model = appt.profilePhoto,
                        contentDescription = "Doctor Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = appt.displayName.getInitials(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appt.displayName ?: "Unknown Doctor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = appt.speciality ?: "General Physician",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatSmartDate(appt.date)}, ${appt.startTime?.take(5) ?: ""}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 84.dp)
                .height(0.5.dp)
                .background(Color(0xFFF0F2F5))
        )
    }
}



private fun String?.getInitials(): String {
    if (this.isNullOrBlank()) return "??"
    return this.removePrefix("Dr. ")
        .removePrefix("Dr ")
        .trim()
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
}

val demoAppt = AppointmentItem(
    appointmentId = 1,
    displayName = "Dr. Sourav",
    speciality = "Cardiologist",
    date = "2026-05-09", // Current date for testing
    startTime = "09:00:00",
    type = "video", // This will trigger the "Online" status
    profilePhoto = null,
    doctorId = 1,
    patientId = 2,
    endTime = "",
    status = "upcoming",
    roomId = 3 // This will trigger the initials placeholder
)
@Preview(showBackground = true)
@Composable
fun TestCardPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        AppointmentCard(
            appt = demoAppt,
            onAvatarClick = { _, _ -> },
            onClick = { }
        )
    }
}