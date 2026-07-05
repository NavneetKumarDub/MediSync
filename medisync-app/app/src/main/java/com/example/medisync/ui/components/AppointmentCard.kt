package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.ui.theme.natureGreen

@Composable
fun AppointmentCard(
    appt: AppointmentEntity,
    token: String,
    onAvatarClick: () -> Unit,
    onClick: () -> Unit
) {
    val isOnline = appt.type.equals(
        other = "online",
        ignoreCase = true
    )

    val statusText = if (isOnline) "Online" else "Offline"
    val statusColor =
        if (isOnline) natureGreen else Color(0xFF6B7280)

    val otherUserId = appt.doctorId ?: appt.patientId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePhoto(
                userId = otherUserId,
                photoKey = appt.profilePhotoKey,
                token = token,
                name = appt.displayName,
                size = 52.dp,
                modifier = Modifier.clickable(
                    onClick = onAvatarClick
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appt.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = appt.subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = buildString {
                        append(formatSmartDate(appt.date))
                        append(", ")
                        append(appt.time.take(5))
                    },
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