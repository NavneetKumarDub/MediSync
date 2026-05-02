package com.example.medisync.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.networks.DoctorClinicRequest
import com.example.medisync.networks.DoctorPersonalRequest
import com.example.medisync.networks.DoctorProfessionalRequest
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.DatePicker
import com.example.medisync.ui.components.DropdownField
import com.example.medisync.ui.components.ProfileRow
import com.example.medisync.ui.components.RadioButton
import com.example.medisync.ui.theme.natureGreen
import kotlinx.coroutines.launch


private val ErrorRed    = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    navController: NavController,
    name         : String,
    phoneNumber  : String,
    userId       : Int
) {
    var selectedTab  by remember { mutableIntStateOf(0) }
    val tabs          = listOf("Personal", "Professional", "Clinic")
    val scope         = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }
    var isSaving     by remember { mutableStateOf(false) }

    // ── Personal ──────────────────────────────
    var email         by remember { mutableStateOf("") }
    var gender        by remember { mutableStateOf("") }
    var dob           by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("") }
    var about         by remember { mutableStateOf("") }

    // ── Professional ──────────────────────────
    var licenseNumber    by remember { mutableStateOf("") }
    var speciality       by remember { mutableStateOf("") }
    var subSpeciality    by remember { mutableStateOf("") }
    var qualification    by remember { mutableStateOf("") }
    var experienceYears  by remember { mutableStateOf("") }
    var languages        by remember { mutableStateOf("") }
    var consultationFee  by remember { mutableStateOf("") }
    var consultationType by remember { mutableStateOf("") }

    // ── Clinic ────────────────────────────────
    var clinicName by remember { mutableStateOf("") }
    var address    by remember { mutableStateOf("") }
    var city       by remember { mutableStateOf("") }
    var pincode    by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            // ── Green rounded block ────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        clip  = true
                    }
                    .background(natureGreen)
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint               = Color.White
                        )
                    }
                    Text(
                        text       = "My Profile",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }

                Spacer(Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = natureGreen,
                    contentColor     = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .padding(horizontal = 24.dp),
                            height = 3.dp,
                            color  = Color.White
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            selectedContentColor   = Color.White,
                            unselectedContentColor = Color.White.copy(alpha = 0.65f),
                            text = {
                                Text(
                                    text       = title,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize   = 14.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = CardBg, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text     = errorMessage,
                            color    = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            scope.launch {
                                isSaving     = true
                                errorMessage = ""
                                try {
                                    RetrofitInstance.api.updateDoctorPersonal(
                                        userId  = userId,
                                        request = DoctorPersonalRequest(
                                            email          = email,
                                            gender         = gender,
                                            dob            = dob,
                                            marital_status = maritalStatus,
                                            about          = about
                                        )
                                    )
                                    RetrofitInstance.api.updateDoctorProfessional(
                                        userId  = userId,
                                        request = DoctorProfessionalRequest(
                                            license_number    = licenseNumber,
                                            speciality        = speciality,
                                            sub_speciality    = subSpeciality,
                                            qualification     = qualification,
                                            experience_years  = experienceYears.toIntOrNull() ?: 0,
                                            languages         = languages,
                                            consultation_fee  = consultationFee,
                                            consultation_type = consultationType
                                        )
                                    )
                                    RetrofitInstance.api.updateDoctorClinic(
                                        userId  = userId,
                                        request = DoctorClinicRequest(
                                            clinic_name = clinicName,
                                            address     = address,
                                            city        = city,
                                            pincode     = pincode
                                        )
                                    )
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to save"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = natureGreen,
                            disabledContainerColor = natureGreen.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                strokeWidth = 2.dp,
                                modifier    = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text       = "Save",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    item { ProfileRow(label = "Name",           value = name,        editable = false) }
                    item { ProfileRow(label = "Contact Number", value = phoneNumber, editable = false) }
                    item { ProfileRow(label = "Email Id",       value = email,       placeholder = "add email", onValueChange = { email = it }) }
                    item { RadioButton(label = "Gender",        options = listOf("Male","Female","Other"), selectedOption = gender, onOptionSelected = { gender = it }) }
                    item { DatePicker(label = "Date of Birth",  value = dob,         placeholder = "yyyy mm dd", onValueChange = { dob = it }) }
                    item { RadioButton(label = "Marital Status",options = listOf("yes","no"), selectedOption = maritalStatus, onOptionSelected = { maritalStatus = it }) }
                    item { ProfileRow(label = "About",          value = about,       placeholder = "Tell patients about yourself", onValueChange = { about = it }) }
                }
                1 -> {
                    item { ProfileRow(label = "License Number",  value = licenseNumber,   placeholder = "add license no.",     onValueChange = { licenseNumber = it }) }
                    item { ProfileRow(label = "Speciality",      value = speciality,      placeholder = "e.g. Cardiology",     onValueChange = { speciality = it }) }
                    item { ProfileRow(label = "Sub Speciality",  value = subSpeciality,   placeholder = "e.g. Interventional", onValueChange = { subSpeciality = it }) }
                    item { ProfileRow(label = "Qualification",   value = qualification,   placeholder = "e.g. MBBS, MD",       onValueChange = { qualification = it }) }
                    item { ProfileRow(label = "Experience (yrs)",value = experienceYears, placeholder = "e.g. 8",              onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) experienceYears = it }) }
                    item { ProfileRow(label = "Languages",       value = languages,       placeholder = "English, Hindi",      onValueChange = { languages = it }) }
                    item { ProfileRow(label = "Consultation Fee",value = consultationFee, placeholder = "e.g. 500",            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) consultationFee = it }) }
                    item {
                        DropdownField(
                            label            = "Consultation Type",
                            options          = listOf("online", "offline", "both"),
                            selectedOption   = consultationType,
                            onOptionSelected = { consultationType = it },
                            paddingX         = 300.dp
                        )
                    }
                }
                2 -> {
                    item { ProfileRow(label = "Clinic Name", value = clinicName, placeholder = "add clinic name",    onValueChange = { clinicName = it }) }
                    item { ProfileRow(label = "Address",     value = address,    placeholder = "add street address", onValueChange = { address = it }) }
                    item { ProfileRow(label = "City",        value = city,       placeholder = "e.g. Bangalore",     onValueChange = { city = it }) }
                    item { ProfileRow(label = "Pincode",     value = pincode,    placeholder = "6-digit pincode",    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 6) pincode = it }) }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorProfileEditPreview() {
    DoctorProfileScreen(
        navController = rememberNavController(),
        name          = "Dr. Ravi Sharma",
        phoneNumber   = "9122349557",
        userId        = 5
    )
}