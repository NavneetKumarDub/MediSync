package com.example.medisync.ui.screens.patient

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.local.ProfileCacheEntity
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.ConfirmUploadRequest
import com.example.medisync.networks.LifestyleProfileRequest
import com.example.medisync.networks.MedicalProfileRequest
import com.example.medisync.networks.PersonalProfileRequest
import com.example.medisync.networks.PresignedUrlRequest
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.DatePicker
import com.example.medisync.ui.components.DropdownField
import com.example.medisync.ui.components.ProfileRow
import com.example.medisync.ui.components.RadioButton
import com.example.medisync.ui.components.StepperField
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.utils.FileCacheManager
import com.example.medisync.utils.PatientProfileCacheData
import com.example.medisync.viewmodels.UserViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private val ErrorRed = Color(0xFFDC2626)
private val AvtarColor = Color(0xFF3E505D)
private val ScreenBg1 = Color(0xFFF6F7F9)
private val CardBg1 = Color(0xFFFFFFFF)
private val MediSkyBlueSoftBg = Color(0xFFE1F5FE)
private val MediSkyBlueText = Color(0xFF0288D1)

suspend fun uploadImageToMinIO(
    context: Context,
    uri: Uri,
    uploadUrl: String,
    mimeType: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@withContext false
            inputStream.close()

            val client = OkHttpClient()
            val requestBody = bytes.toRequestBody(mimeType.toMediaType())
            android.util.Log.d("MinIOUpload", "Uploading to URL: $uploadUrl")
            android.util.Log.d("MinIOUpload", "Bytes size: ${bytes.size}")
            android.util.Log.d("MinIOUpload", "MimeType: $mimeType")
            val request = Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .addHeader("Content-Type", mimeType)
                .build()

            val response = client.newCall(request).execute()
            android.util.Log.d("MinIOUpload", "Response code: ${response.code}")
            android.util.Log.d("MinIOUpload", "Response body: ${response.body?.string()}")
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    navController: NavController,
    name         : String,
    phoneNumber  : String,
    userId       : Int,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val app = context.applicationContext as MediSyncApplication
    val gson = remember { Gson() }
    var shouldDeletePhoto by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab  by remember { mutableIntStateOf(0) }
    val tabs          = listOf("Personal", "Medical", "Lifestyle")
    val scope         = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }
    var isSaving     by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(true) }

    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profilePhotoUri = uri
            shouldDeletePhoto = false
        }
    }

    var myName           by remember { mutableStateOf(name) }
    var email            by remember { mutableStateOf("") }
    var gender           by remember { mutableStateOf("") }
    var dob              by remember { mutableStateOf("") }
    var bloodGroup       by remember { mutableStateOf("") }
    var maritalStatus    by remember { mutableStateOf("") }
    var height           by remember { mutableIntStateOf(0) }
    var weight           by remember { mutableIntStateOf(0) }
    var emergencyContact by remember { mutableStateOf("") }

    var allergies          by remember { mutableStateOf("") }
    var currentMedications by remember { mutableStateOf("") }
    var pastMedications    by remember { mutableStateOf("") }
    var chronicDiseases    by remember { mutableStateOf("") }
    var injuries           by remember { mutableStateOf("") }
    var surgeries          by remember { mutableStateOf("") }

    var smoking        by remember { mutableStateOf("") }
    var alcohol        by remember { mutableStateOf("") }
    var activityLevel  by remember { mutableStateOf("") }
    var foodPreference by remember { mutableStateOf("") }
    var occupation     by remember { mutableStateOf("") }

    fun applyCachedProfile(cache: PatientProfileCacheData) {
        myName = cache.name.ifBlank { name }
        email = cache.email
        gender = cache.gender
        dob = cache.dob
        bloodGroup = cache.bloodGroup
        maritalStatus = cache.maritalStatus
        height = cache.height
        weight = cache.weight
        emergencyContact = cache.emergencyContact
        allergies = cache.allergies
        currentMedications = cache.currentMedications
        pastMedications = cache.pastMedications
        chronicDiseases = cache.chronicDiseases
        injuries = cache.injuries
        surgeries = cache.surgeries
        smoking = cache.smoking
        alcohol = cache.alcohol
        activityLevel = cache.activityLevel
        foodPreference = cache.foodPreference
        occupation = cache.occupation
    }

    fun currentCacheData() = PatientProfileCacheData(
        name = myName,
        email = email,
        gender = gender,
        dob = dob,
        bloodGroup = bloodGroup,
        maritalStatus = maritalStatus,
        height = height,
        weight = weight,
        emergencyContact = emergencyContact,
        allergies = allergies,
        currentMedications = currentMedications,
        pastMedications = pastMedications,
        chronicDiseases = chronicDiseases,
        injuries = injuries,
        surgeries = surgeries,
        smoking = smoking,
        alcohol = alcohol,
        activityLevel = activityLevel,
        foodPreference = foodPreference,
        occupation = occupation
    )

    LaunchedEffect(userId) {
        val cachedProfile = app.database.profileCacheDao().getProfile(userId, "patient")
        cachedProfile?.let { cache ->
            runCatching {
                gson.fromJson(cache.dataJson, PatientProfileCacheData::class.java)
            }.getOrNull()?.let(::applyCachedProfile)
            profilePhotoUrl = cache.photoUri
            isLoading = false
        }

        try {
            val token = "Bearer ${TokenManager.getToken(context) ?: ""}"
            val personalResponse  = RetrofitInstance.api.getPersonalProfile(token,userId)
            val medicalResponse   = RetrofitInstance.api.getMedicalProfile(token,userId)
            val lifestyleResponse = RetrofitInstance.api.getLifestyleProfile(token,userId)

            personalResponse.data?.let { p ->
                myName           = p.name             ?: name
                email            = p.email            ?: ""
                gender           = p.gender           ?: ""
                dob              = p.dob              ?: ""
                bloodGroup       = p.blood_group      ?: ""
                maritalStatus    = p.marital_status   ?: ""
                height           = p.height?.toIntOrNull() ?: 0
                weight           = p.weight?.toIntOrNull() ?: 0
                emergencyContact = p.emergency_contact ?: ""
            }

            medicalResponse.data?.let { m ->
                allergies          = m.allergies           ?: ""
                currentMedications = m.current_medications ?: ""
                pastMedications    = m.past_medications    ?: ""
                chronicDiseases    = m.chronic_diseases    ?: ""
                injuries           = m.injuries            ?: ""
                surgeries          = m.surgeries           ?: ""
            }

            lifestyleResponse.data?.let { l ->
                smoking        = l.smoking        ?: ""
                alcohol        = l.alcohol        ?: ""
                activityLevel  = l.activity_level ?: ""
                foodPreference = l.food_preference ?: ""
                occupation     = l.occupation     ?: ""
            }

            try {
                val photoResponse = RetrofitInstance.api.getProfilePhotoUrl(token,userId)
                val photoFile = FileCacheManager.getOrDownloadFile(
                    context = context.applicationContext,
                    fileKey = "patient_profile_photo_$userId",
                    fileName = "patient_profile_$userId.jpg",
                    fileType = "image/jpeg",
                    forceRefresh = profilePhotoUrl == null
                ) {
                    photoResponse.viewUrl
                }
                profilePhotoUrl = FileCacheManager.contentUri(context.applicationContext, photoFile).toString()
            } catch (e: Exception) {
                if (profilePhotoUrl == null) profilePhotoUrl = cachedProfile?.photoUri
            }

            app.database.profileCacheDao().upsertProfile(
                ProfileCacheEntity(
                    userId = userId,
                    role = "patient",
                    dataJson = gson.toJson(currentCacheData()),
                    photoUri = profilePhotoUrl,
                    updatedAt = System.currentTimeMillis()
                )
            )

        } catch (e: Exception) {
            if (cachedProfile == null) {
                errorMessage = "Failed to load profile"
            }
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ScreenBg1,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                Surface(color = CardBg1, shadowElevation = 8.dp) {
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
                                        val token = "Bearer ${TokenManager.getToken(context) ?: ""}"

                                        if (shouldDeletePhoto && profilePhotoUri == null) {
                                            RetrofitInstance.api.deleteProfilePhoto(token, userId)
                                            userViewModel.updateProfilePhotoUrl(null)
                                            profilePhotoUrl = null
                                            app.database.profileCacheDao().clearPhoto(userId, "patient", System.currentTimeMillis())
                                            shouldDeletePhoto = false
                                        }

                                        if (profilePhotoUri != null) {
                                            val mimeType = context.contentResolver
                                                .getType(profilePhotoUri!!) ?: "image/jpeg"
                                            val extension = mimeType.split("/").lastOrNull() ?: "jpg"
                                            val fileName = "avatar_${System.currentTimeMillis()}.$extension"

                                            val presignedResponse = RetrofitInstance.api.getPresignedUploadUrl(
                                                token,
                                                PresignedUrlRequest(
                                                    userId   = userId,
                                                    fileName = fileName,
                                                    fileType = mimeType
                                                )
                                            )

                                            val uploaded = uploadImageToMinIO(
                                                context   = context,
                                                uri       = profilePhotoUri!!,
                                                uploadUrl = presignedResponse.uploadUrl,
                                                mimeType  = mimeType
                                            )

                                            if (uploaded) {
                                                RetrofitInstance.api.confirmProfilePhotoUpload(
                                                    token,
                                                    ConfirmUploadRequest(
                                                        userId = userId,
                                                        key    = presignedResponse.key
                                                    )
                                                )
                                                userViewModel.refreshProfilePhoto()
                                                val photoResponse = RetrofitInstance.api.getProfilePhotoUrl(token, userId)
                                                val photoFile = FileCacheManager.getOrDownloadFile(
                                                    context = context.applicationContext,
                                                    fileKey = "patient_profile_photo_$userId",
                                                    fileName = "patient_profile_$userId.jpg",
                                                    fileType = mimeType,
                                                    forceRefresh = true
                                                ) {
                                                    photoResponse.viewUrl
                                                }
                                                profilePhotoUrl = FileCacheManager.contentUri(context.applicationContext, photoFile).toString()
                                                profilePhotoUri = null
                                            } else {
                                                errorMessage = "Photo upload failed"
                                                isSaving = false
                                                return@launch
                                            }
                                        }

                                        RetrofitInstance.api.updatePersonalProfile(
                                            token,
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
                                            token,
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
                                            token,
                                            userId  = userId,
                                            request = LifestyleProfileRequest(
                                                smoking         = smoking,
                                                alcohol         = alcohol,
                                                activity_level  = activityLevel,
                                                food_preference = foodPreference,
                                                occupation      = occupation,
                                            )
                                        )
                                        app.database.profileCacheDao().upsertProfile(
                                            ProfileCacheEntity(
                                                userId = userId,
                                                role = "patient",
                                                dataJson = gson.toJson(currentCacheData()),
                                                photoUri = profilePhotoUrl,
                                                updatedAt = System.currentTimeMillis()
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
                            enabled = !isSaving && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor         = natureGreen,
                                disabledContainerColor = natureGreen.copy(alpha = 0.4f)
                            )
                        ) {
                            if (isSaving || isLoading) {
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

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = natureGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE5E7EB))
                                            .clickable { showPhotoDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val imageModel = profilePhotoUri ?: profilePhotoUrl
                                        if (imageModel != null) {
                                            AsyncImage(
                                                model              = imageModel,
                                                contentDescription = "Profile Photo",
                                                contentScale       = ContentScale.Crop,
                                                modifier           = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector        = Icons.Default.Person,
                                                contentDescription = "Placeholder",
                                                modifier           = Modifier.size(50.dp),
                                                tint               = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                            item { ProfileRow(label = "Name",              value = myName,           placeholder = "add name",               onValueChange = { myName = it }) }
                            item { ProfileRow(label = "Contact Number",    value = phoneNumber,      editable = false) }
                            item { ProfileRow(label = "Email Id",          value = email,            placeholder = "add email",              onValueChange = { email = it }) }
                            item { RadioButton(label = "Gender",           options = listOf("Male","Female","Other"), selectedOption = gender, onOptionSelected = { gender = it }) }
                            item { DatePicker(label = "Date of Birth",     value = dob,              placeholder = "yyyy mm dd",             onValueChange = { dob = it }) }
                            item { DropdownField(label = "Blood Group",    options = listOf("A+","A-","B+","B-","AB+","AB-","O+","O-"), selectedOption = bloodGroup, onOptionSelected = { bloodGroup = it }, paddingX = 300.dp) }
                            item { RadioButton(label = "Marital Status",   options = listOf("yes","no"), selectedOption = maritalStatus, onOptionSelected = { maritalStatus = it }) }
                            item { StepperField(label = "Height", min = 0, max = 500, unit = "cm", value = height, onValueChange = { height = it }) }
                            item { ProfileRow(label = "Emergency Contact", value = emergencyContact,  placeholder = "add emergency details", onValueChange = { emergencyContact = it }) }
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
                            item { DropdownField(label = "Alcohol consumption", options = listOf("Non-drinking", "Occasional", "Regular"), selectedOption = alcohol,        onOptionSelected = { alcohol = it },        paddingX = 300.dp) }
                            item { DropdownField(label = "Smoking Habits",      options = listOf("Non-smoker", "Occasional", "Regular"),   selectedOption = smoking,        onOptionSelected = { smoking = it },        paddingX = 300.dp) }
                            item { RadioButton(label = "Activity Level",        options = listOf("low", "Moderate", "High"),               selectedOption = activityLevel,  onOptionSelected = { activityLevel = it }) }
                            item { RadioButton(label = "Food Preference",       options = listOf("veg", "non-veg", "vegan"),               selectedOption = foodPreference, onOptionSelected = { foodPreference = it }) }
                            item { ProfileRow(label = "Occupation",             value = occupation,          placeholder = "add occupation", onValueChange = { occupation = it }) }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }

        if (showPhotoDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showPhotoDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .wrapContentHeight()
                        .offset(y = (0).dp)
                        .clickable(enabled = false) {}, // Prevent clicks on card from closing
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(AvtarColor)
                        ) {
                            val dialogImageModel = profilePhotoUri ?: profilePhotoUrl
                            if (dialogImageModel != null) {
                                AsyncImage(
                                    model = dialogImageModel,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFE5E7EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Placeholder",
                                        modifier = Modifier.size(120.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove",
                                tint = natureGreen,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        profilePhotoUri = null
                                        profilePhotoUrl = null
                                        shouldDeletePhoto = true
                                        showPhotoDialog = false
                                    }
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = natureGreen,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        photoPickerLauncher.launch("image/*")
                                    }
                            )
                        }
                    }
                }
            }
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
