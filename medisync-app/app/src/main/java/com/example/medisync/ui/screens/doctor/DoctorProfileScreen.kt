package com.example.medisync.ui.screens.doctor

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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.ProfileCacheEntity
import com.example.medisync.networks.ConfirmUploadRequest
import com.example.medisync.networks.DoctorClinicRequest
import com.example.medisync.networks.DoctorPersonalRequest
import com.example.medisync.networks.DoctorProfessionalRequest
import com.example.medisync.networks.PresignedUrlRequest
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.DatePicker
import com.example.medisync.ui.components.DropdownField
import com.example.medisync.ui.components.ProfileRow
import com.example.medisync.ui.components.RadioButton
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.utils.DoctorProfileCacheData
import com.example.medisync.utils.FileCacheManager
import com.example.medisync.viewmodels.UserViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.medisync.ui.navigation.safePopBackStack

private val ErrorRed = Color(0xFFDC2626)
private val AvtarColor = Color(0xFF3E505D)
private val ScreenBg1 = Color(0xFFF6F7F9)
private val CardBg1 = Color(0xFFFFFFFF)

private suspend fun uploadImageToMinIO(
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
            val request = Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .addHeader("Content-Type", mimeType)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    navController: NavController,
    name         : String,
    phoneNumber  : String,
    userId       : Int,
    userViewModel: UserViewModel = viewModel()
) {

    val context = LocalContext.current
    val app = context.applicationContext as MediSyncApplication
    val gson = remember { Gson() }
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab  by remember { mutableIntStateOf(0) }
    val tabs          = listOf("Personal", "Professional", "Clinic")
    val scope         = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }
    var isSaving     by remember { mutableStateOf(false) }

    var shouldDeletePhoto by remember { mutableStateOf(false) }
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

    var email         by remember { mutableStateOf("") }
    var gender        by remember { mutableStateOf("") }
    var dob           by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("") }
    var about         by remember { mutableStateOf("") }

    var licenseNumber    by remember { mutableStateOf("") }
    var speciality       by remember { mutableStateOf("") }
    var subSpeciality    by remember { mutableStateOf("") }
    var qualification    by remember { mutableStateOf("") }
    var experienceYears  by remember { mutableStateOf("") }
    var languages        by remember { mutableStateOf("") }
    var consultationFee  by remember { mutableStateOf("") }
    var consultationType by remember { mutableStateOf("") }

    var clinicName by remember { mutableStateOf("") }

    fun applyCachedProfile(cache: DoctorProfileCacheData) {
        email = cache.email
        gender = cache.gender
        dob = cache.dob
        maritalStatus = cache.maritalStatus
        about = cache.about
        licenseNumber = cache.licenseNumber
        speciality = cache.speciality
        subSpeciality = cache.subSpeciality
        qualification = cache.qualification
        experienceYears = cache.experienceYears
        languages = cache.languages
        consultationFee = cache.consultationFee
        consultationType = cache.consultationType
        clinicName = cache.clinicName
    }

    fun currentCacheData() = DoctorProfileCacheData(
        email = email,
        gender = gender,
        dob = dob,
        maritalStatus = maritalStatus,
        about = about,
        licenseNumber = licenseNumber,
        speciality = speciality,
        subSpeciality = subSpeciality,
        qualification = qualification,
        experienceYears = experienceYears,
        languages = languages,
        consultationFee = consultationFee,
        consultationType = consultationType,
        clinicName = clinicName
    )

    LaunchedEffect(key1 = userId) {
        val cachedProfile = app.database.profileCacheDao().getProfile(userId, "doctor")
        cachedProfile?.let { cache ->
            runCatching {
                gson.fromJson(cache.dataJson, DoctorProfileCacheData::class.java)
            }.getOrNull()?.let(::applyCachedProfile)
            profilePhotoUrl = cache.photoUri
        }

        try {
            val token = "Bearer ${TokenManager.getToken(context) ?: ""}"

            val response = RetrofitInstance.api.getDoctorProfile(token,userId)

            if (response.isSuccessful) {
                response.body()?.doctor?.let { profile ->
                    email         = profile.email ?: ""
                    gender        = profile.gender ?: ""
                    dob           = profile.dob ?: ""
                    maritalStatus = profile.maritalStatus ?: ""
                    about         = profile.about ?: ""

                    licenseNumber    = profile.licenseNumber ?: ""
                    speciality       = profile.speciality ?: ""
                    subSpeciality    = profile.subSpeciality ?: ""
                    qualification    = profile.qualification ?: ""
                    experienceYears  = profile.experienceYears?.toString() ?: ""
                    languages        = profile.languages ?: ""
                    consultationFee  = profile.consultationFee?.toString() ?: ""
                    consultationType = profile.consultationType ?: ""

                    clinicName = profile.clinicName ?: ""

                }
            } else {
                errorMessage = "Failed to load profile data."
            }

            try {
                val photoResponse = RetrofitInstance.api.getProfilePhotoUrl(token,userId)
                val photoFile = FileCacheManager.getOrDownloadFile(
                    context = context.applicationContext,
                    fileKey = "doctor_profile_photo_$userId",
                    fileName = "doctor_profile_$userId.jpg",
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
                    role = "doctor",
                    dataJson = gson.toJson(currentCacheData()),
                    photoUri = profilePhotoUrl,
                    updatedAt = System.currentTimeMillis()
                )
            )

        } catch (e: Exception) {
            if (cachedProfile == null) {
                errorMessage = "Network error: ${e.message}"
            }
        }
    }

    if (showPhotoDialog) {
        Dialog(
            onDismissRequest = { showPhotoDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
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
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Placeholder",
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.Center),
                                tint = Color.Gray
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E))
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    profilePhotoUri = null
                                    profilePhotoUrl = null
                                    shouldDeletePhoto = true
                                    showPhotoDialog = false
                                }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove",
                                tint = natureGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = natureGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }

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
                    IconButton(onClick = { navController.safePopBackStack() }) {
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
                                val token = "Bearer ${TokenManager.getToken(context) ?: ""}"

                                try {
                                    if (shouldDeletePhoto && profilePhotoUri == null) {
                                        RetrofitInstance.api.deleteProfilePhoto(token, userId)
                                        userViewModel.updateProfilePhotoUrl(null)
                                        profilePhotoUrl = null
                                        app.database.profileCacheDao().clearPhoto(userId, "doctor", System.currentTimeMillis())
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
                                                fileKey = "doctor_profile_photo_$userId",
                                                fileName = "doctor_profile_$userId.jpg",
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

                                    RetrofitInstance.api.updateDoctorPersonal(
                                        token,
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
                                        token,
                                        userId  = userId,
                                        request = DoctorProfessionalRequest(
                                            license_number    = licenseNumber,
                                            speciality        = speciality,
                                            sub_speciality    = subSpeciality,
                                            qualification     = qualification,
                                            experience_years  = experienceYears.toIntOrNull() ?: 0,
                                            languages         = languages,
                                            consultation_fee  = consultationFee.toDoubleOrNull()?.toString() ?: "0.0",
                                            consultation_type = consultationType
                                        )
                                    )
                                    RetrofitInstance.api.updateDoctorClinic(
                                        token,
                                        userId  = userId,
                                        request = DoctorClinicRequest(
                                            clinic_name = clinicName,

                                        )
                                    )
                                    app.database.profileCacheDao().upsertProfile(
                                        ProfileCacheEntity(
                                            userId = userId,
                                            role = "doctor",
                                            dataJson = gson.toJson(currentCacheData()),
                                            photoUri = profilePhotoUrl,
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                    navController.safePopBackStack()
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
                                        model = imageModel,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Placeholder",
                                        modifier = Modifier.size(50.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
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
