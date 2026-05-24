package com.example.medisync.ui.screens.appointment

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.viewmodels.AppointmentDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ScreenBg = Color(0xFFE7F0F4)
private val Accent = Color(0xFF2A9DF4)
private val TextDark = Color(0xFF111B21)
private val TextMuted = Color(0xFF6B7280)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentDetailScreen(
    navController: NavController,
    appointmentId: Int
) {
    val context = LocalContext.current
    val app = context.applicationContext as MediSyncApplication

    val viewModel: AppointmentDetailViewModel = viewModel(
        factory = AppointmentDetailViewModel.Factory(
            repository = app.appointmentRepository,
            appointmentId = appointmentId
        )
    )

    val appointment by viewModel.appointment.collectAsState()

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Appointment Details",
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (appointment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            val appt = appointment!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(ScreenBg)
                    .padding(16.dp)
            ) {
                DoctorHeroCard(appt = appt)

                Spacer(Modifier.height(12.dp))

                AppointmentInfoCard(appt = appt)

                Spacer(Modifier.height(12.dp))

                ClinicLocationPlaceholder(appt = appt)

                Spacer(Modifier.height(12.dp))

                RatingPlaceholder()

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            appt.roomId?.let { roomId ->
                                navController.navigate(
                                    "chat/$roomId?name=${appt.displayName}&photoUrl=${appt.photoUrl ?: ""}"
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(14.dp),
                        enabled = appt.roomId != null
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Chat")
                    }

                    Button(
                        onClick = {
                            appt.roomId?.let { roomId ->
                                navController.navigate("video_room/$roomId")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(14.dp),
                        enabled = appt.type.equals("online", ignoreCase = true) && appt.roomId != null
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Join")
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorHeroCard(appt: AppointmentEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (!appt.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = "${RetrofitInstance.MINIO_BASE_URL}${appt.photoUrl}",
                        contentDescription = appt.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = appt.displayName,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = appt.subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            StatusChip(status = appt.status)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AppointmentInfoCard(appt: AppointmentEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Appointment Time",
                fontWeight = FontWeight.Bold,
                color = TextDark,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.10f))
                        .padding(12.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Accent
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Date", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = formatDate(appt.date),
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.10f))
                        .padding(12.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Accent
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Time", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = appt.time,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            DetailRow(Icons.Default.Videocam, "Consultation Type", appt.type)
            DetailRow(
                Icons.Default.Payments,
                "Consultation Fee",
                appt.consultationFee?.let { "₹${it.toInt()}" } ?: "Not available"
            )
            DetailRow(Icons.Default.Info, "Status", appt.status)
        }
    }
}

@Composable
private fun ClinicLocationPlaceholder(appt: AppointmentEntity) {
    DetailCard(title = "Clinic Location") {
        if (appt.type.contains("offline", ignoreCase = true)) {
            DetailRow(Icons.Default.LocationOn, "Location", "Clinic location will appear here")
        } else {
            DetailRow(Icons.Default.LocationOn, "Location", "Online consultation")
        }
    }
}

@Composable
private fun RatingPlaceholder() {
    DetailCard(title = "Doctor Rating") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Rating will be added after appointment",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(10.dp))

            content()
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(21.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = TextDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "confirmed" -> Color(0xFF16A34A)
        "pending" -> Color(0xFFEAB308)
        "cancelled" -> Color(0xFFDC2626)
        "completed" -> Accent
        else -> TextMuted
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = color,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(value: String): String {
    return try {
        LocalDate.parse(value)
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
    } catch (e: Exception) {
        value
    }
}