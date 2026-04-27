package com.example.medisync.ui.screens.patient

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
import com.example.medisync.networks.LifestyleProfileRequest
import com.example.medisync.networks.MedicalProfileRequest
import com.example.medisync.networks.PersonalProfileRequest
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.DatePicker
import com.example.medisync.ui.components.DropdownField
import com.example.medisync.ui.components.ProfileRow
import com.example.medisync.ui.components.RadioButton
import com.example.medisync.ui.components.StepperField
import com.example.medisync.ui.theme.natureGreen
import kotlinx.coroutines.launch

private val ErrorRed    = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    navController: NavController,
    name         : String,
    phoneNumber  : String,
    userId       : Int
) {
    var selectedTab  by remember { mutableIntStateOf(0) }
    val tabs          = listOf("Personal", "Medical", "Lifestyle")
    val scope         = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }
    var isSaving     by remember { mutableStateOf(false) }

    // Personal fields
    var myName           by remember { mutableStateOf(name) }
    var email            by remember { mutableStateOf("") }
    var gender           by remember { mutableStateOf("") }
    var dob              by remember { mutableStateOf("") }
    var bloodGroup       by remember { mutableStateOf("") }
    var maritalStatus    by remember { mutableStateOf("") }
    var height           by remember { mutableIntStateOf(0) }
    var weight           by remember { mutableIntStateOf(0) }
    var emergencyContact by remember { mutableStateOf("") }

    // Medical fields
    var allergies          by remember { mutableStateOf("") }
    var currentMedications by remember { mutableStateOf("") }
    var pastMedications    by remember { mutableStateOf("") }
    var chronicDiseases    by remember { mutableStateOf("") }
    var injuries           by remember { mutableStateOf("") }
    var surgeries          by remember { mutableStateOf("") }

    // Lifestyle fields
    var smoking        by remember { mutableStateOf("") }
    var alcohol        by remember { mutableStateOf("") }
    var activityLevel  by remember { mutableStateOf("") }
    var foodPreference by remember { mutableStateOf("") }
    var occupation     by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            // ── Green rounded block (HomeTopBar style) ──
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

                // Tabs on green bg
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
                                    RetrofitInstance.api.updatePersonalProfile(
                                        userId  = userId,
                                        request = PersonalProfileRequest(
                                            email             = email,
                                            gender            = gender,
                                            blood_group       = bloodGroup,
                                            dob               = dob,
                                            name              = myName,
                                            marital_status    = maritalStatus,
                                            height            = height.toString(),
                                            weight            = weight.toString(),
                                            emergency_contact = emergencyContact
                                        )
                                    )
                                    RetrofitInstance.api.updateMedicalProfile(
                                        userId  = userId,
                                        request = MedicalProfileRequest(
                                            allergies           = allergies,
                                            current_medications = currentMedications,
                                            past_medications    = pastMedications,
                                            chronic_diseases    = chronicDiseases,
                                            injuries            = injuries,
                                            surgeries           = surgeries
                                        )
                                    )
                                    RetrofitInstance.api.updateLifestyleProfile(
                                        userId  = userId,
                                        request = LifestyleProfileRequest(
                                            smoking         = smoking,
                                            alcohol         = alcohol,
                                            activity_level  = activityLevel,
                                            food_preference = foodPreference,
                                            occupation      = occupation,
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
                    item { ProfileRow(label = "Name",              value = myName,          placeholder = "add name",               onValueChange = { myName = it }) }
                    item { ProfileRow(label = "Contact Number",    value = phoneNumber,     editable = false) }
                    item { ProfileRow(label = "Email Id",          value = email,           placeholder = "add email",              onValueChange = { email = it }) }
                    item { RadioButton(label = "Gender",           options = listOf("Male","Female","Other"), selectedOption = gender, onOptionSelected = { gender = it }) }
                    item { DatePicker(label = "Date of Birth",     value = dob,             placeholder = "yyyy mm dd",             onValueChange = { dob = it }) }
                    item { DropdownField(label = "Blood Group",    options = listOf("A+","A-","B+","B-","AB+","AB-","O+","O-"), selectedOption = bloodGroup, onOptionSelected = { bloodGroup = it }, paddingX = 300.dp) }
                    item { RadioButton(label = "Marital Status",   options = listOf("yes","no"), selectedOption = maritalStatus, onOptionSelected = { maritalStatus = it }) }
                    item { StepperField(label = "Height", min = 0, max = 500, unit = "cm", value = height, onValueChange = { height = it }) }
                    item { ProfileRow(label = "Emergency Contact", value = emergencyContact, placeholder = "add emergency details", onValueChange = { emergencyContact = it }) }
                }
                1 -> {
                    item { ProfileRow(label = "Allergies",            value = allergies,           placeholder = "add allergies",   onValueChange = { allergies = it }) }
                    item { ProfileRow(label = "Current Medications",  value = currentMedications,  placeholder = "add medications", onValueChange = { currentMedications = it }) }
                    item { ProfileRow(label = "Past Medications",     value = pastMedications,     placeholder = "add medications", onValueChange = { pastMedications = it }) }
                    item { ProfileRow(label = "Chronic Diseases",     value = chronicDiseases,     placeholder = "add diseases",    onValueChange = { chronicDiseases = it }) }
                    item { ProfileRow(label = "Injuries",             value = injuries,            placeholder = "add incident",    onValueChange = { injuries = it }) }
                    item { ProfileRow(label = "Surgeries",            value = surgeries,           placeholder = "add surgeries",   onValueChange = { surgeries = it }) }
                }
                2 -> {
                    item { DropdownField(label = "Alcohol consumption", options = listOf("Non-drinking", "Occasional", "Regular"), selectedOption = alcohol, onOptionSelected = { alcohol = it }, paddingX = 300.dp) }
                    item { DropdownField(label = "Smoking Habits",      options = listOf("Non-smoker", "Occasional", "Regular"),  selectedOption = smoking, onOptionSelected = { smoking = it }, paddingX = 300.dp) }
                    item { RadioButton(label = "Activity Level",        options = listOf("low", "Moderate", "High"),              selectedOption = activityLevel,  onOptionSelected = { activityLevel = it }) }
                    item { RadioButton(label = "Food Preference",       options = listOf("veg", "non-veg", "vegan"),              selectedOption = foodPreference, onOptionSelected = { foodPreference = it }) }
                    item { ProfileRow(label = "Occupation",             value = occupation, placeholder = "add occupation", onValueChange = { occupation = it }) }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    PatientProfileScreen(
        navController = rememberNavController(),
        name          = "Navneet",
        phoneNumber   = "9122349557",
        userId        = 5
    )
}