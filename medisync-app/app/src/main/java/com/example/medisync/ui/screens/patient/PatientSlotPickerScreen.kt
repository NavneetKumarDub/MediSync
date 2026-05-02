package com.example.medisync.ui.screens.patient

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.BookAppointmentRequest
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.SlotItem
import com.example.medisync.ui.components.SlotDateChip
import com.example.medisync.ui.components.SlotGrid
import com.example.medisync.ui.theme.*
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

// ── Formatters ─────────────────────────────────
fun formatTime(time: String): String = try {
    val parsed = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(time)
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed!!)
} catch (_: Exception) { time }

private fun formatDateDisplay(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(parsed!!)
    } catch (_: Exception) { dateStr }
}

// ── Preview data ───────────────────────────────
private val previewSlots = listOf(
    SlotItem(1,  "09:00:00", "09:15:00", "500.00", "available"),
    SlotItem(2,  "09:15:00", "09:30:00", "500.00", "booked"),
    SlotItem(3,  "09:30:00", "09:45:00", "500.00", "available"),
    SlotItem(4,  "09:45:00", "10:00:00", "500.00", "available"),
    SlotItem(5,  "10:00:00", "10:15:00", "500.00", "available"),
    SlotItem(6,  "10:15:00", "10:30:00", "500.00", "booked"),
    SlotItem(7,  "14:00:00", "14:30:00", "700.00", "available"),
    SlotItem(8,  "14:30:00", "15:00:00", "700.00", "available"),
    SlotItem(9,  "15:00:00", "15:30:00", "700.00", "booked"),
    SlotItem(10, "16:00:00", "16:15:00", "800.00", "available"),
    SlotItem(11, "16:15:00", "16:30:00", "800.00", "available"),
    SlotItem(12, "16:30:00", "16:45:00", "800.00", "available"),
)
private val previewDates = listOf(
    "2026-04-21", "2026-04-22", "2026-04-23", "2026-04-24", "2026-04-25"
)

// ── Stateful screen ────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotPickerScreen(
    doctorId     : Int,
    doctorName   : String = "Doctor",
    navController: NavController
) {
    val context           = LocalContext.current
    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var availableDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedDate   by remember { mutableStateOf<String?>(null) }
    var slots          by remember { mutableStateOf<List<SlotItem>>(emptyList()) }
    var selectedSlot   by remember { mutableStateOf<SlotItem?>(null) }
    var isLoadingDates by remember { mutableStateOf(true) }
    var isLoadingSlots by remember { mutableStateOf(false) }
    var isBooking      by remember { mutableStateOf(false) }

    LaunchedEffect(doctorId) {
        try {
            val response   = RetrofitInstance.api.getDoctorAvailableDates(doctorId)
            availableDates = response.dates
            if (response.dates.isNotEmpty()) selectedDate = response.dates.first()
        } catch (_: Exception) {
            snackbarHostState.showSnackbar("Could not load dates")
        }
        isLoadingDates = false
    }

    LaunchedEffect(selectedDate) {
        val date = selectedDate ?: return@LaunchedEffect
        isLoadingSlots = true
        selectedSlot   = null
        try {
            slots = RetrofitInstance.api.getDoctorSlots(doctorId, date).slots
        } catch (_: Exception) {
            snackbarHostState.showSnackbar("Could not load slots")
        }
        isLoadingSlots = false
    }

    suspend fun refreshSlots() {
        val date = selectedDate ?: return
        runCatching { RetrofitInstance.api.getDoctorSlots(doctorId, date) }
            .onSuccess { slots = it.slots }
    }

    SlotPickerContent(
        availableDates    = availableDates,
        selectedDate      = selectedDate,
        slots             = slots,
        selectedSlot      = selectedSlot,
        isLoadingDates    = isLoadingDates,
        isLoadingSlots    = isLoadingSlots,
        isBooking         = isBooking,
        doctorName        = doctorName,
        doctorId          = doctorId,
        snackbarHostState = snackbarHostState,
        onDateSelected    = { selectedDate = it },
        onSlotSelected    = { selectedSlot = it },
        onBackClick       = { navController.popBackStack() },
        onConfirmClick    = { _, slotId ->
            if (isBooking) return@SlotPickerContent
            scope.launch {
                isBooking = true
                try {
                    val token = TokenManager.getToken(context)
                    if (token == null) {
                        snackbarHostState.showSnackbar("Please login again")
                        return@launch
                    }
                    val res = RetrofitInstance.api.bookAppointment(
                        token = "Bearer $token",
                        body  = BookAppointmentRequest(slotId)
                    )
                    when {
                        res.isSuccessful -> {
                            navController.popBackStack()
                        }
                        res.code() == 409 -> {
                            snackbarHostState.showSnackbar(
                                "This slot was just taken. Please pick another."
                            )
                            selectedSlot = null
                            refreshSlots()
                        }
                        else -> snackbarHostState.showSnackbar("Booking failed (${res.code()})")
                    }
                } catch (e: Exception) {
                    // We don't want to show an error for that!
                    if (e is CancellationException) throw e

                    // 2. Log the real error for YOU (the developer) to see in Logcat
                    Log.e("BookingError", "Failed to book appointment", e)

                    // 3. Show a friendly message to the USER based on the specific crash
                    val errorMessage = when (e) {
                        is SocketTimeoutException ->
                            "The server is taking too long to respond. Please try again."

                        is IOException ->
                            "No internet connection. Please check your Wi-Fi or data."

                        is JsonSyntaxException ->
                            "App update required. (Data mismatch)"

                        is IllegalArgumentException ->
                            "Navigation error. Please restart the app."

                        else ->
                            "Something went wrong. Please try again." // Generic fallback
                    }

                    // Show the friendly message
                    snackbarHostState.showSnackbar(errorMessage)
                } finally {
                    isBooking = false
                }
            }
        }
    )
}

// ── Section header (no emoji, neutral grey pill) ──
@Composable
private fun SlotSectionHeader(
    title         : String,
    subtitle      : String,
    availableCount: Int
) {
    Column {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text       = title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = SlotTextPrimary
                )
                Text(
                    text     = subtitle,
                    fontSize = 11.sp,
                    color    = SlotTextHint
                )
            }
            // Neutral grey pill — no longer theme blue
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = "$availableCount available",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFF334155)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        Spacer(Modifier.height(10.dp))
    }
}

// ── Info pill for bottom bar ───────────────────
@Composable
private fun InfoPill(icon: ImageVector, text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = SlotTextHint,
            modifier           = Modifier.size(14.dp)
        )
        Text(
            text       = text,
            fontSize   = 13.sp,
            color      = SlotTextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Stateless content ──────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotPickerContent(
    availableDates    : List<String>,
    selectedDate      : String?,
    slots             : List<SlotItem>,
    selectedSlot      : SlotItem?,
    isLoadingDates    : Boolean,
    isLoadingSlots    : Boolean,
    isBooking         : Boolean = false,
    doctorName        : String,
    doctorId          : Int,
    snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
    onDateSelected    : (String) -> Unit,
    onSlotSelected    : (SlotItem) -> Unit,
    onBackClick       : () -> Unit,
    onConfirmClick    : (String, Int) -> Unit
) {
    val morningSlots   = slots.filter { it.startTime < "12:00" }
    val afternoonSlots = slots.filter { it.startTime >= "12:00" && it.startTime < "16:00" }
    val eveningSlots   = slots.filter { it.startTime >= "16:00" }

    Scaffold(
        containerColor = SlotScreenBg,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        clip  = true
                    }
                    .background(SlotBluePrimary)
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Column {
                        Text("Book Appointment", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(doctorName, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                Spacer(Modifier.height(16.dp))
                when {
                    isLoadingDates -> CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                    availableDates.isEmpty() -> Text(
                        "No available dates", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f)
                    )
                    else -> Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableDates.forEach { date ->
                            SlotDateChip(
                                date       = date,
                                isSelected = date == selectedDate,
                                onClick    = { onDateSelected(date) }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedSlot != null,
                enter   = slideInVertically { it } + fadeIn(),
                exit    = slideOutVertically { it } + fadeOut()
            ) {
                if (selectedSlot != null) {
                    Surface(
                        color           = SlotCardBg,
                        shadowElevation = 16.dp,
                        shape           = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.Gray)
                                    .align(Alignment.CenterHorizontally)
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    InfoPill(Icons.Default.CalendarToday, formatDateDisplay(selectedDate))
                                    InfoPill(Icons.Default.Schedule, formatTime(selectedSlot.startTime))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Fee", fontSize = 10.sp, color = SlotTextHint)
                                    Text(
                                        "₹${selectedSlot.consultationFee.toDouble().toInt()}",
                                        fontSize   = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = SlotTextPrimary
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isBooking) natureGreen.copy(alpha = 0.7f)
                                        else natureGreen
                                    )
                                    .clickable(
                                        enabled           = !isBooking,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null
                                    ) {
                                        onConfirmClick(selectedDate ?: "", selectedSlot.id)
                                    }
                                    .padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isBooking) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(22.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Confirm Booking",
                                            fontSize   = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = Color.White
                                        )
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint               = Color.White,
                                            modifier           = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoadingSlots -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = SlotBluePrimary) }

            slots.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SlotBlueLighter),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            tint               = SlotBluePrimary,
                            modifier           = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("No slots available", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = SlotTextPrimary)
                    Text("Try a different date", fontSize = 13.sp, color = SlotTextHint)
                }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                if (morningSlots.isNotEmpty()) {
                    SlotSectionHeader(
                        title          = "Morning",
                        subtitle       = "Before 12:00 PM",
                        availableCount = morningSlots.count { it.status == "available" }
                    )
                    SlotGrid(slots = morningSlots, selectedSlot = selectedSlot, onSelect = onSlotSelected)
                    Spacer(Modifier.height(20.dp))
                }
                if (afternoonSlots.isNotEmpty()) {
                    SlotSectionHeader(
                        title          = "Afternoon",
                        subtitle       = "12:00 PM – 4:00 PM",
                        availableCount = afternoonSlots.count { it.status == "available" }
                    )
                    SlotGrid(slots = afternoonSlots, selectedSlot = selectedSlot, onSelect = onSlotSelected)
                    Spacer(Modifier.height(20.dp))
                }
                if (eveningSlots.isNotEmpty()) {
                    SlotSectionHeader(
                        title          = "Evening",
                        subtitle       = "After 4:00 PM",
                        availableCount = eveningSlots.count { it.status == "available" }
                    )
                    SlotGrid(slots = eveningSlots, selectedSlot = selectedSlot, onSelect = onSlotSelected)
                }

                Spacer(Modifier.height(200.dp)) // clearance for bottom sheet
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SlotPickerPreview() {
    SlotPickerContent(
        availableDates = previewDates,
        selectedDate   = previewDates.first(),
        slots          = previewSlots,
        selectedSlot   = previewSlots.first(),
        isLoadingDates = false,
        isLoadingSlots = false,
        doctorName     = "Dr. Ravi Sharma",
        doctorId       = 1,
        onDateSelected = { },
        onSlotSelected = { },
        onBackClick    = { },
        onConfirmClick = { _, _ -> }
    )
}