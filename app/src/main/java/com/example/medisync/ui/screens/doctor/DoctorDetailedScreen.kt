package com.example.medisync.ui.screens.doctor

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medisync.networks.AvailabilitySlot
import com.example.medisync.networks.DoctorDetail
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.*
import com.example.medisync.ui.theme.natureGreen

// ── Palette ────────────────────────────────────

private val BorderSoft    = Color(0xFFE2E8F0)

private val ChipOnlineBg   = Color(0xFFECFDF5); private val ChipOnlineText  = Color(0xFF059669)
private val ChipOfflineBg  = Color(0xFFF1F5F9); private val ChipOfflineText = Color(0xFF334155)
private val ChipBothBg     = natureGreen.copy(alpha = 0.12f)

// ── Preview data ──────────────────────────────
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

private val previewAvailability = listOf(
    AvailabilitySlot("Monday",    "09:00", "13:00", 15),
    AvailabilitySlot("Wednesday", "14:00", "18:00", 15),
    AvailabilitySlot("Friday",    "09:00", "12:00", 30),
)

// ── Main Screen ───────────────────────────────
@Composable
fun DoctorProfileScreen(
    doctorId     : Int,
    navController: NavController
) {
    var doctor       by remember { mutableStateOf<DoctorDetail?>(null) }
    var availability by remember { mutableStateOf<List<AvailabilitySlot>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var error        by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(doctorId) {
        try {
            val profileRes = RetrofitInstance.api.getDoctorProfile(doctorId)
            doctor         = profileRes.doctor
            val availRes   = RetrofitInstance.api.getDoctorAvailability(doctorId)
            availability   = availRes.availability
        } catch (e: Exception) {
            error = "Could not load doctor profile"
            Log.e("DoctorProfile", "Error: ${e.message}")
        }
        isLoading = false
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
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(text = "Go Back", color = natureGreen)
                }
            }
        }

        doctor != null -> DoctorProfileContent(
            doctor       = doctor!!,
            availability = availability,
            onBackClick  = { navController.popBackStack() },
            onBookClick  = { id ->
                val encodedName = Uri.encode(doctor!!.doctorName)
                navController.navigate("slotPicker/$id/$encodedName")
            }
        )
    }
}

// ── Stateless Content ─────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileContent(
    doctor      : DoctorDetail,
    availability: List<AvailabilitySlot>,
    onBackClick : () -> Unit,
    onBookClick : (Int) -> Unit
) {
    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            // ── Green hero block (HomeTopBar style) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        clip  = true
                    }
                    .background(natureGreen)
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 28.dp)
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

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    DoctorAvatar(
                        name     = doctor.doctorName,
                        photoUrl = doctor.profilePhoto,
                        size     = 80.dp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text       = doctor.doctorName,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        if (!doctor.speciality.isNullOrEmpty()) {
                            Text(
                                text     = doctor.speciality,
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        if (!doctor.qualification.isNullOrEmpty()) {
                            Text(
                                text     = doctor.qualification,
                                fontSize = 11.sp,
                                color    = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            doctor.experienceYears?.let {
                                HeroMeta(icon = Icons.Default.WorkHistory, text = "$it yrs exp")
                            }
                            if (!doctor.city.isNullOrEmpty()) {
                                HeroMeta(icon = Icons.Default.LocationOn, text = doctor.city)
                            }
                        }

                        if (!doctor.consultationType.isNullOrEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            ConsultChip(type = doctor.consultationType)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = CardBg, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Consultation Fee", fontSize = 11.sp, color = TextHint)
                        Text(
                            text       = doctor.consultationFee?.let { "₹${it.toInt()}" } ?: "Free",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
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
            // About
            if (!doctor.about.isNullOrEmpty()) {
                Section(title = "About") {
                    Text(
                        text       = doctor.about,
                        fontSize   = 14.sp,
                        color      = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Details
            Section(title = "Details") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    doctor.qualification?.let {
                        DoctorDetailRow(icon = Icons.Default.School, label = "Qualification", value = it)
                    }
                    if (!doctor.clinicName.isNullOrEmpty()) {
                        DoctorDetailRow(
                            icon  = Icons.Default.LocalHospital,
                            label = "Clinic",
                            value = buildString {
                                append(doctor.clinicName)
                                if (!doctor.city.isNullOrEmpty()) append(", ${doctor.city}")
                            }
                        )
                    }
                    doctor.languages?.let {
                        DoctorDetailRow(icon = Icons.Default.Language, label = "Languages", value = it)
                    }
                    doctor.consultationFee?.let {
                        DoctorDetailRow(
                            icon  = Icons.Default.CurrencyRupee,
                            label = "Consultation Fee",
                            value = "₹${it.toInt()} per visit"
                        )
                    }
                    doctor.consultationType?.let {
                        DoctorDetailRow(
                            icon  = Icons.Default.Devices,
                            label = "Consultation Mode",
                            value = it.replaceFirstChar { c -> c.uppercase() }
                        )
                    }
                }
            }

            // Availability
            Section(title = "Availability") {
                if (availability.isEmpty()) {
                    Text(text = "No availability set yet", fontSize = 14.sp, color = TextHint)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        availability.forEach { slot -> DoctorAvailabilityRow(slot = slot) }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Flat section (no card) ─────────────────────
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

// ── Hero meta row ──────────────────────────────
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

// ── Consult chip ───────────────────────────────
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
        availability = previewAvailability,
        onBackClick  = { },
        onBookClick  = { }
    )
}