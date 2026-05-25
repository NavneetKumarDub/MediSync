package com.example.medisync.ui.screens.appointment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.networks.DoctorDetail
import com.example.medisync.networks.DoctorRatingDto
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.viewmodels.AppointmentDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ScreenBg = Color(0xFFE7F0F4)
private val Accent = natureGreen
private val AccentDeep = natureGreen
private val TextDark = Color(0xFF111B21)
private val TextMuted = Color(0xFF6B7280)
private val StrokeSoft = Color(0xFFD7E2E8)
private val WarningGold = Color(0xFFFFB300)

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
    val uiState by viewModel.uiState.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("patient") }
    val isPatientView = userRole == "patient"

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val oldStatusBarColor = window?.statusBarColor
        window?.statusBarColor = natureGreen.toArgb()
        onDispose {
            if (oldStatusBarColor != null) {
                window.statusBarColor = oldStatusBarColor
            }
        }
    }

    LaunchedEffect(appointment?.id) {
        userRole = TokenManager.getRole(context) ?: "patient"
        if (appointment != null) {
            viewModel.loadRatingData(context)
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(natureGreen)
                )

                TopAppBar(
                    title = {
                        Text(
                            text = "Appointment Details",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = natureGreen)
                )
            }
        },
        bottomBar = {
            appointment?.let { appt ->
                ChatBottomBar(
                    label = if (isPatientView) "Chat with Doctor" else "Chat with Patient",
                    onClick = {
                        val roomId = appt.roomId ?: return@ChatBottomBar
                        val encodedName = Uri.encode(appt.displayName)
                        val encodedPhoto = Uri.encode(appt.photoUrl ?: "")
                        navController.navigate("chat/$roomId?name=$encodedName&photoUrl=$encodedPhoto")
                    }
                )
            }
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(ScreenBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    AccentDeep,
                                    Accent.copy(alpha = 0.45f),
                                    ScreenBg
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 92.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(28.dp))

                    DoctorHeader(appt)

                    Spacer(Modifier.height(28.dp))

                    AppointmentSection(appt)

                    if (isPatientView && isOffline(appt.type)) {
                        Spacer(Modifier.height(18.dp))
                        ClinicLocationSection(
                            doctor = uiState.doctor,
                            onDirectionsClick = { lat, lng, label ->
                                openGoogleMapsDirections(context, lat, lng, label)
                            }
                        )
                    }

                    if (isPatientView) {
                        Spacer(Modifier.height(18.dp))

                        RatingSection(
                            appt = appt,
                            rating = uiState.rating,
                            average = uiState.ratingAverage,
                            count = uiState.ratingCount,
                            onRateClick = { showRatingDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            isSubmitting = uiState.isSubmittingRating,
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitRating(context, rating, comment)
                showRatingDialog = false
            }
        )
    }
}

@Composable
private fun DoctorHeader(appt: AppointmentEntity) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(124.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            if (!appt.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profilePhotoModel(appt.photoUrl),
                    contentDescription = appt.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = appt.displayName,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        if (appt.subtitle.isNotBlank() && appt.subtitle != "Patient") {
            Spacer(Modifier.height(3.dp))
            Text(
                text = appt.subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AppointmentSection(appt: AppointmentEntity) {
    PlainSection(title = "Appointment") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoTile(
                icon = Icons.Default.Event,
                label = "Date",
                value = formatDate(appt.date),
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = Icons.Default.Schedule,
                label = "Time",
                value = formatTime(appt.time),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        DetailRow(Icons.Default.Videocam, "Consultation Type", appt.type.displayValue())
        DetailRow(
            Icons.Default.Payments,
            "Consultation Fee",
            appt.consultationFee?.let { "₹${it.toInt()}" } ?: "Not available"
        )
        DetailRow(Icons.Default.Info, "Status", appt.status.displayValue())
    }
}

@Composable
private fun ClinicLocationSection(
    doctor: DoctorDetail?,
    onDirectionsClick: (Double, Double, String) -> Unit
) {
    PlainSection(title = "Clinic Location") {
        val lat = doctor?.latitude
        val lng = doctor?.longitude
        val label = doctor?.clinicName?.takeIf { it.isNotBlank() }
            ?: doctor?.address?.takeIf { it.isNotBlank() }
            ?: "Clinic"

        if (lat != null && lng != null) {
            ClinicMapPreview(
                latitude = lat,
                longitude = lng,
                label = label
            )

            Spacer(Modifier.height(10.dp))

            DetailRow(
                Icons.Default.LocationOn,
                "Location",
                doctor.address?.takeIf { it.isNotBlank() } ?: label
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { onDirectionsClick(lat, lng, label) },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Get Directions", fontWeight = FontWeight.Bold)
            }
        } else {
            DetailRow(Icons.Default.LocationOn, "Location", "Clinic location not added yet")
        }
    }
}

@Composable
private fun ClinicMapPreview(
    latitude: Double,
    longitude: Double,
    label: String
) {
    val position = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    LaunchedEffect(latitude, longitude) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.72f))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = false,
                tiltGesturesEnabled = false,
                rotationGesturesEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = position),
                title = label
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RatingSection(
    appt: AppointmentEntity,
    rating: DoctorRatingDto?,
    average: Double,
    count: Int,
    onRateClick: () -> Unit
) {
    PlainSection(title = "Doctor Rating") {
        val canRate = appt.status.equals("completed", ignoreCase = true) || isAppointmentPast(appt)

        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingStars(
                rating = average.toInt(),
                starSize = 24
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = if (count > 0) {
                    String.format(Locale.ENGLISH, "%.1f", average) + " ($count reviews)"
                } else {
                    "No ratings yet"
                },
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(14.dp))

        if (rating != null) {
            Text(
                text = "Your rating",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(5.dp))
            RatingStars(rating = rating.rating, starSize = 26)

            if (!rating.comment.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = rating.comment,
                    color = TextDark,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Button(
                onClick = onRateClick,
                enabled = canRate,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (canRate) {
                        "Rate Doctor"
                    } else {
                        "Rating opens after appointment"
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatBottomBar(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = ScreenBg,
        shadowElevation = 0.dp
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .height(52.dp)
        ) {
            Icon(Icons.Default.Chat, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(label, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlainSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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

@Composable
private fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .padding(14.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(10.dp))
            Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = TextDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(label, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = TextDark, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RatingStars(
    rating: Int,
    starSize: Int,
    onSelect: ((Int) -> Unit)? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        (1..5).forEach { star ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (star <= rating) WarningGold else StrokeSoft,
                modifier = Modifier
                    .size(starSize.dp)
                    .then(
                        if (onSelect != null) {
                            Modifier.clickable { onSelect(star) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
private fun RatingDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate Doctor") },
        text = {
            Column {
                RatingStars(
                    rating = selectedRating,
                    starSize = 34,
                    onSelect = { selectedRating = it }
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment optional") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(selectedRating, comment.ifBlank { null }) },
                enabled = !isSubmitting
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun isOffline(type: String): Boolean {
    val normalized = type.lowercase(Locale.ENGLISH)
    return normalized.contains("offline") || normalized.contains("in_person") || normalized.contains("in person")
}

private fun profilePhotoModel(photoUrl: String?): String? {
    if (photoUrl.isNullOrBlank()) return null
    return if (photoUrl.startsWith("http")) {
        photoUrl
    } else {
        "${RetrofitInstance.MINIO_BASE_URL}$photoUrl"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun isAppointmentPast(appt: AppointmentEntity): Boolean {
    return try {
        val date = LocalDate.parse(appt.date)
        val today = LocalDate.now()

        if (date.isBefore(today)) return true
        if (date.isAfter(today)) return false

        val time = LocalTime.parse(appt.time.substringBefore("+").substringBefore("Z"))
        time.isBefore(LocalTime.now())
    } catch (e: Exception) {
        false
    }
}

private fun openGoogleMapsDirections(
    context: Context,
    latitude: Double,
    longitude: Double,
    label: String
) {
    val googleMapsUri = Uri.parse("google.navigation:q=$latitude,$longitude")
    val mapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
    val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)

    try {
        context.startActivity(mapsIntent)
    } catch (e: Exception) {
        context.startActivity(fallbackIntent)
    }
}

private fun String.displayValue(): String {
    return replace("_", " ")
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
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

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(value: String): String {
    return try {
        LocalTime.parse(value.substringBefore("+").substringBefore("Z"))
            .format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
    } catch (e: Exception) {
        value.substringBefore(".")
    }
}
