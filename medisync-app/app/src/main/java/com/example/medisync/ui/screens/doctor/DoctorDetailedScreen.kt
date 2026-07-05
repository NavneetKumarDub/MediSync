package com.example.medisync.ui.screens.doctor

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.DoctorDetail
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.*
import com.example.medisync.ui.theme.natureGreen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.medisync.ui.navigation.safePopBackStack


private val ScreenBg    = Color(0xFFE7F0F4)
private  val TextHint = Color(0xFF6B7280)
private val TextPrimary = Color(0xFF1C3A34)

private val CardBg = Color.White

private val TextSecondary = Color(0xFF475569)

private val BorderSoft    = Color(0xFFD7E2E8)
private val StarGold = Color(0xFFFFB300)

private val ChipOnlineBg   = Color(0xFFECFDF5); private val ChipOnlineText  = Color(0xFF059669)
private val ChipOfflineBg  = Color(0xFFF1F5F9); private val ChipOfflineText = Color(0xFF334155)
private val ChipBothBg     = natureGreen.copy(alpha = 0.12f)


private val previewDoctor = DoctorDetail(
    doctorId         = 1,
    doctorName       = "Dr. Ravi Sharma",
    speciality       = "General Physician",
    subSpeciality    = "Diabetology",
    qualification    = "MBBS, MD - Internal Medicine",
    experienceYears  = 8,
    consultationFee  = 500.0,
    consultationType = "both",
    languages        = "English, Hindi, Kannada",
    about            = "Dr. Ravi Sharma is an experienced general physician with over 8 years of practice. He specializes in preventive care and chronic disease management.",
    gender           = "Male",
    profilePhoto     = null,
    clinicName       = "Ravi Medical Center",
    address          = "123 MG Road",
    city             = "Bangalore",
    pincode          = "560001"
)




@Composable
fun DoctorProfileScreen(
    doctorId     : Int,
    navController: NavController
) {
    val context = LocalContext.current
    var doctor       by remember { mutableStateOf<DoctorDetail?>(null) }
    var ratingAverage by remember { mutableStateOf(0.0) }
    var ratingCount by remember { mutableIntStateOf(0) }
    var token by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        try {
            val rawToken = TokenManager.getToken(context) ?: ""
            token = rawToken
            val bearerToken = "Bearer $rawToken"
            val profileRes = RetrofitInstance.api.getDoctorProfile(bearerToken,doctorId)
            if (profileRes.isSuccessful) {
                doctor = profileRes.body()?.doctor
            } else {
                error = "Failed to load doctor details"
            }

            val ratingRes = RetrofitInstance.api.getDoctorRatingSummary(token, doctorId)
            if (ratingRes.isSuccessful) {
                ratingAverage = ratingRes.body()?.average ?: 0.0
                ratingCount = ratingRes.body()?.count ?: 0
            }

        } catch (e: Exception) {
            error = "Could not connect to server"
            Log.e("DoctorProfile", "Error: ${e.message}")
        } finally {

            isLoading = false
        }
    }
    when {
        isLoading -> Box(
            modifier         = Modifier.fillMaxSize().background(ScreenBg),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = natureGreen) }

        error != null -> Box(
            modifier         = Modifier.fillMaxSize().background(ScreenBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint               = Color(0xFFDC2626),
                    modifier           = Modifier.size(48.dp)
                )
                Text(text = error ?: "Something went wrong", color = TextSecondary, fontSize = 14.sp)
                TextButton(onClick = { navController.safePopBackStack() }) {
                    Text(text = "Go Back", color = natureGreen)
                }
            }
        }

        doctor != null -> DoctorProfileContent(
            doctor       = doctor!!,
            ratingAverage = ratingAverage,
            ratingCount = ratingCount,
            token = token,
            onBackClick  = { navController.safePopBackStack() },
            onBookClick  = { id ->
                val encodedName = Uri.encode(doctor!!.doctorName)
                navController.navigate("slotPicker/$id/$encodedName")
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileContent(
    doctor      : DoctorDetail,
    ratingAverage: Double,
    ratingCount: Int,
    token: String,
    onBackClick : () -> Unit,
    onBookClick : (Int) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        clip  = true
                    }
                    .background(natureGreen)
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 18.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text       = "Doctor Profile",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    DoctorHeroAvatar(
                        userId   = doctor.doctorId,
                        name     = formattedDoctorName(doctor.doctorName),
                        photoUrl = doctor.profilePhoto,
                        token    = token,
                        size     = 68.dp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text       = formattedDoctorName(doctor.doctorName),
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        doctor.speciality.validTextOrNull()?.let { speciality ->
                            Text(
                                text = speciality,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        RatingLine(
                            average = ratingAverage,
                            count = ratingCount,
                            light = true
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = CardBg, shadowElevation = 8.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(natureGreen)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null
                            ) { onBookClick(doctor.doctorId) }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint               = Color.White,
                                modifier           = Modifier.size(18.dp)
                            )
                            Text(
                                text       = "Book Appointment",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            doctor.about.validTextOrNull()?.let { about ->
                Section(title = "About") {
                    Text(
                        text       = about,
                        fontSize   = 14.sp,
                        color      = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            Section(title = "Details") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    doctor.subSpeciality.validTextOrNull()?.let { subSpeciality ->
                        DoctorDetailRow(icon = Icons.Default.WorkspacePremium, label = "Sub-speciality", value = subSpeciality)
                    }
                    doctor.qualification.validTextOrNull()?.let { qualification ->
                        DoctorDetailRow(icon = Icons.Default.School, label = "Qualification", value = qualification)
                    }
                    doctor.clinicName.validTextOrNull()?.let { clinicName ->
                        DoctorDetailRow(
                            icon  = Icons.Default.LocalHospital,
                            label = "Clinic",
                            value = buildString {
                                append(clinicName)
                                doctor.city.validTextOrNull()?.let { city -> append(", $city") }
                            }
                        )
                    }
                    doctor.languages.validTextOrNull()?.let { languages ->
                        DoctorDetailRow(icon = Icons.Default.Language, label = "Languages", value = languages)
                    }
                    doctor.consultationType.validTextOrNull()?.let { consultationType ->
                        DoctorDetailRow(
                            icon  = Icons.Default.Devices,
                            label = "Consultation Mode",
                            value = consultationType.replaceFirstChar { c -> c.uppercase() }
                        )
                    }
                }
            }

            if (doctor.latitude != null && doctor.longitude != null) {
                Section(title = "Clinic Location") {
                    ClinicMapPreview(
                        latitude = doctor.latitude,
                        longitude = doctor.longitude,
                        label = doctor.clinicName.validTextOrNull() ?: doctor.doctorName
                    )

                    Spacer(Modifier.height(12.dp))

                    if (doctor.address.hasText() || doctor.clinicName.hasText()) {
                        DoctorDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = doctor.clinicName.validTextOrNull() ?: "Clinic",
                            value = listOfNotNull(
                                doctor.address,
                                doctor.city,
                                doctor.pincode
                            ).filter { it.hasText() }.joinToString(", ")
                        )
                    }

                    Button(
                        onClick = {
                            openGoogleMapsDirections(
                                context = context,
                                latitude = doctor.latitude,
                                longitude = doctor.longitude,
                                label = doctor.clinicName.validTextOrNull() ?: doctor.doctorName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = natureGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Get Directions", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DoctorHeroAvatar(
    userId: Int,
    name: String,
    photoUrl: String?,
    token: String,
    size: androidx.compose.ui.unit.Dp
) {
    ProfilePhoto(
        userId = userId,
        photoKey = photoUrl,
        token = token,
        name = name,
        size = size
    )
}

@Composable
private fun RatingLine(
    average: Double,
    count: Int,
    light: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < average.toInt()) StarGold else if (light) Color.White.copy(alpha = 0.35f) else BorderSoft,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Text(
            text = if (count > 0) {
                String.format(java.util.Locale.ENGLISH, "%.1f", average) + " ($count reviews)"
            } else {
                "No ratings yet"
            },
            color = if (light) Color.White.copy(alpha = 0.9f) else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@SuppressLint("UnrememberedMutableState")
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
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
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

private fun openGoogleMapsDirections(
    context: Context,
    latitude: Double,
    longitude: Double,
    label: String
) {
    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude")).apply {
        setPackage("com.google.android.apps.maps")
    }
    val fallbackIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
    )

    try {
        context.startActivity(mapsIntent)
    } catch (e: Exception) {
        context.startActivity(fallbackIntent)
    }
}

private fun profilePhotoModel(photoUrl: String?): String? {
    if (photoUrl.isNullOrBlank()) return null
    return if (photoUrl.startsWith("http")) {
        photoUrl
    } else {
        null
    }
}

private fun String?.hasText(): Boolean {
    return !this.isNullOrBlank() && this.lowercase() != "null"
}

private fun String?.validTextOrNull(): String? {
    return if (hasText()) this else null
}

private fun formattedDoctorName(name: String): String {
    val formattedName = name
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.lowercase().replaceFirstChar { char -> char.titlecase() }
        }

    return if (
        formattedName.startsWith("Dr", ignoreCase = true) ||
        formattedName.startsWith("Doctor", ignoreCase = true)
    ) {
        formattedName
    } else {
        "Dr. $formattedName"
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text       = title,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = BorderSoft, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun HeroMeta(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = Color.White.copy(alpha = 0.85f),
            modifier           = Modifier.size(14.dp)
        )
        Text(text = text, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
    }
}


@Composable
private fun ConsultChip(type: String) {
    val (bg, fg) = when (type) {
        "online"  -> ChipOnlineBg to ChipOnlineText
        "offline" -> ChipOfflineBg to ChipOfflineText
        else      -> ChipBothBg to Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text       = type.replaceFirstChar { it.uppercase() },
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = fg
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorProfilePreview() {
    DoctorProfileContent(
        doctor       = previewDoctor,
        ratingAverage = 4.8,
        ratingCount = 23,
        token = "fake-token",
        onBackClick  = { },
        onBookClick  = { }
    )
}
