package com.example.medisync.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.AddSlotRequest
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.DurationSliderField
import com.example.medisync.ui.components.HorizontalScrollSelector
import com.example.medisync.ui.components.ModeSelector
import com.example.medisync.ui.components.SlotCard
import com.example.medisync.ui.components.TimePicker
import com.example.medisync.ui.theme.natureGreen
import kotlinx.coroutines.launch
import com.example.medisync.ui.navigation.safePopBackStack

private val PageBackground = Color(0xFFF9FAFB)
private val BottomBarBackground = Color(0xFFFFFFFF)
private val TopBarBackground = natureGreen
private val GreenPrimary2 = natureGreen
private val GrayText = Color(0xFF6B7280)
private val BlackText = Color(0xFF1A1A2E)
private val BorderColor = Color(0xFFE5E7EB)
private val ScrollChipUnselectedText = Color(0xFFCBEAF8)


data class Slot(
    val id: Int,
    val day: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val mode: String,
    val fee: String
)

@Composable
fun DoctorRegularSlotsManage(
    navController: NavController,
    userId: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var openSlotId by remember { mutableStateOf<Int?>(null) }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDay by remember { mutableStateOf("Mon") }
    var slots by remember { mutableStateOf(listOf<Slot>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var startTime by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(30) }
    var mode by remember { mutableStateOf("Online") }
    var fee by remember { mutableStateOf("") }

    val filteredSlots = slots.filter { it.day == selectedDay }
    val totalMinutes = filteredSlots.sumOf { it.duration }
    val totalHours = totalMinutes / 60

    LaunchedEffect(selectedDay) {
        isLoading = true
        errorMessage = ""
        try {
            val token = "Bearer ${TokenManager.getToken(context)}"
            val response = RetrofitInstance.api.getRegularSlots(
                token = token,
                day = selectedDay
            )
            if (response.success) {
                slots = response.slots.map { item ->
                    Slot(
                        id = item.id,
                        day = item.day_of_week,
                        startTime = convertTo12Hour(item.start_time),
                        endTime = convertTo12Hour(item.end_time),
                        duration = item.slot_duration_minutes,
                        mode = item.consultation_type,
                        fee = item.consultation_fee
                    )
                }
            }
        }  catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            errorMessage = try {
                org.json.JSONObject(errorBody ?: "").getString("message")
            } catch (ex: Exception) {
                "Something went wrong"
            }
        } catch (e: Exception) {
            errorMessage = e.message.toString()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TopBarBackground)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.safePopBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "Weekly Template",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${filteredSlots.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "slots", fontSize = 11.sp, color = ScrollChipUnselectedText)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalHours", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "hr", fontSize = 11.sp, color = ScrollChipUnselectedText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HorizontalScrollSelector(
                        items = days,
                        selectedItem = selectedDay,
                        onItemSelected = { selectedDay = it }
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))
            }
        },

        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .background(BottomBarBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeSelector(selectedMode = mode, onModeSelected = { mode = it })
                    TimePicker(label = "Start Time", selectedTime = startTime, onTimeSelected = { startTime = it })
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .width(90.dp)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (fee.isEmpty()) Text("Fee (₹)", color = GrayText, fontSize = 12.sp)
                        BasicTextField(
                            value = fee,
                            onValueChange = { fee = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 14.sp, color = BlackText)
                        )
                    }
                }

                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DurationSliderField(
                            label = "Duration",
                            value = duration,
                            min = 0,
                            max = 120,
                            step = 1,
                            unit = "min",
                            onValueChange = { duration = it }
                        )
                    }

                    
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(GreenPrimary2, RoundedCornerShape(8.dp))
                            .clickable {
                                if (startTime.isNotEmpty() && fee.isNotEmpty() && duration > 0) {
                                    scope.launch {
                                        try {
                                            val token = "Bearer ${TokenManager.getToken(context)}"
                                            val startTime24 = convertTo24Hour(startTime)
                                            val endTime24 = convertTo24Hour(calculateEndTime(startTime, duration))

                                            val response = RetrofitInstance.api.addRegularSlot(
                                                token = token,
                                                request = AddSlotRequest(
                                                    day_of_week = selectedDay,
                                                    start_time = startTime24,
                                                    end_time = endTime24,
                                                    slot_duration_minutes = duration,
                                                    consultation_fee = fee.toInt(),
                                                    consultation_type = mode
                                                )
                                            )
                                            if (response.success && response.slot != null) {
                                                val newSlot = Slot(
                                                    id = response.slot.id,
                                                    day = selectedDay,
                                                    startTime = startTime,
                                                    endTime = calculateEndTime(startTime, duration),
                                                    duration = duration,
                                                    mode = mode,
                                                    fee = fee
                                                )
                                                slots = slots + newSlot
                                                startTime = ""
                                                fee = ""
                                                errorMessage = ""
                                            }
                                        }  catch (e: retrofit2.HttpException) {
                                        val errorBody = e.response()?.errorBody()?.string()
                                        errorMessage = try {
                                            org.json.JSONObject(errorBody ?: "").getString("message")
                                        } catch (ex: Exception) {
                                            "Slot overlaps with an existing slot"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message.toString()
                                    }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures { openSlotId = null }
                }
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary2)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PageBackground)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    if (filteredSlots.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No slots for $selectedDay", color = GrayText, fontSize = 14.sp)
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = filteredSlots,
                            key = { _, slot -> slot.id }
                        ) { index, slot ->
                            SlotCard(
                                index = index + 1,
                                startTime = slot.startTime,
                                endTime = slot.endTime,
                                duration = slot.duration,
                                mode = slot.mode,
                                fee = slot.fee,
                                isOpen = openSlotId == slot.id,
                                onSwipeOpen = { openSlotId = slot.id },
                                onDelete = {
                                    scope.launch {
                                        try {
                                            val token = "Bearer ${TokenManager.getToken(context)}"
                                            val response = RetrofitInstance.api.deleteRegularSlot(
                                                token = token,
                                                slotId = slot.id
                                            )
                                            if (response.success) {
                                                slots = slots.filter { it.id != slot.id }
                                                openSlotId = null
                                                errorMessage = ""
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message.toString()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


fun calculateEndTime(startTime: String, durationMinutes: Int): String {
    try {
        val parts = startTime.split(":")
        val amPm = parts[1].split(" ")
        var hour = parts[0].toInt()
        var minute = amPm[0].toInt()
        val period = amPm[1]
        if (period == "PM" && hour != 12) hour += 12
        if (period == "AM" && hour == 12) hour = 0
        val totalMinutes = hour * 60 + minute + durationMinutes
        var endHour = (totalMinutes / 60) % 24
        val endMinute = totalMinutes % 60
        val endPeriod = if (endHour >= 12) "PM" else "AM"
        if (endHour > 12) endHour -= 12
        if (endHour == 0) endHour = 12
        return "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')} $endPeriod"
    } catch (e: Exception) { return "" }
}

fun convertTo24Hour(time12: String): String {
    return try {
        val parts = time12.trim().split(":")
        val amPm = parts[1].trim().split(" ")
        var hour = parts[0].trim().toInt()
        val minute = amPm[0].trim()
        val period = amPm[1].trim()
        if (period == "PM" && hour != 12) hour += 12
        if (period == "AM" && hour == 12) hour = 0
        "${hour.toString().padStart(2, '0')}:$minute"
    } catch (e: Exception) { time12 }
}

fun convertTo12Hour(time24: String): String {
    return try {
        val parts = time24.split(":")
        var hour = parts[0].toInt()
        val minute = parts[1].take(2)
        val period = if (hour >= 12) "PM" else "AM"
        if (hour > 12) hour -= 12
        if (hour == 0) hour = 12
        "${hour.toString().padStart(2, '0')}:$minute $period"
    } catch (e: Exception) { time24 }
}

@Preview(showBackground = true)
@Composable
fun DoctorSlotsPreview() {
    DoctorRegularSlotsManage(navController = rememberNavController(), userId = 1)
}
