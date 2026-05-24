package com.example.medisync.ui.screens.doctor

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.ClinicLocationRequest
import com.example.medisync.networks.RetrofitInstance
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices


private val ScreenBg = Color(0xFFE7F0F4)
private val Accent = Color(0xFF2A9DF4)
private val TextDark = Color(0xFF111B21)

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorClinicLocationScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()




    var token by remember { mutableStateOf<String?>(null) }
    var selectedLatLng by remember {
        mutableStateOf(LatLng(12.9716, 77.5946)) // Bangalore default
    }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    selectedLatLng = latLng
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)

                    scope.launch {
                        address = reverseGeocode(context, latLng)
                    }
                } else {
                    error = "Unable to get current location"
                }
            }
        } else {
            error = "Location permission denied"
        }
    }



    LaunchedEffect(Unit) {
        token = TokenManager.getToken(context)

        val currentToken = token
        if (currentToken != null) {
            try {
                val res = RetrofitInstance.api.getMyClinicLocation("Bearer $currentToken")
                if (res.isSuccessful && res.body() != null) {
                    val clinic = res.body()!!
                    if (clinic.latitude != null && clinic.longitude != null) {
                        selectedLatLng = LatLng(clinic.latitude, clinic.longitude)
                        address = clinic.address ?: ""
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
                    }
                }
            } catch (e: Exception) {
                error = null
            }
        }

        isLoading = false
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clinic Location",
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(ScreenBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                            scope.launch {
                                address = reverseGeocode(context, latLng)
                            }
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = selectedLatLng),
                            title = "Clinic Location",
                            snippet = address.ifBlank { "Selected location" }
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        val latLng = LatLng(location.latitude, location.longitude)
                                        selectedLatLng = latLng
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)

                                        scope.launch {
                                            address = reverseGeocode(context, latLng)
                                        }
                                    } else {
                                        error = "Unable to get current location"
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        containerColor = Color.White,
                        contentColor = Accent,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 10.dp, bottom = 110.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Use current location"
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp)
                    )
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Selected Address",
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            placeholder = { Text("Tap map or enter clinic address") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Lat: ${selectedLatLng.latitude}, Lng: ${selectedLatLng.longitude}",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (error != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val currentToken = token
                                if (currentToken == null) {
                                    error = "Please login again"
                                    return@Button
                                }

                                if (address.isBlank()) {
                                    error = "Address is required"
                                    return@Button
                                }

                                scope.launch {
                                    isSaving = true
                                    error = null

                                    try {
                                        val res = RetrofitInstance.api.updateClinicLocation(
                                            token = "Bearer $currentToken",
                                            request = ClinicLocationRequest(
                                                latitude = selectedLatLng.latitude,
                                                longitude = selectedLatLng.longitude,
                                                address = address
                                            )
                                        )

                                        if (res.isSuccessful) {
                                            navController.popBackStack()
                                        } else {
                                            error = "Failed to save clinic location"
                                        }
                                    } catch (e: Exception) {
                                        error = e.message ?: "Failed to save clinic location"
                                    }

                                    isSaving = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Clinic Location")
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun reverseGeocode(
    context: android.content.Context,
    latLng: LatLng
): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val result = geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )?.firstOrNull()

            result?.getAddressLine(0) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}