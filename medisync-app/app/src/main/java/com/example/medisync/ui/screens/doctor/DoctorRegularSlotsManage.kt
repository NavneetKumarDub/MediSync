package com.example.medisync.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.components.DurationSliderField
import com.example.medisync.ui.components.HorizontalScrollSelector
import com.example.medisync.ui.components.ModeSelector
import com.example.medisync.ui.components.SlotCard
import com.example.medisync.ui.components.TimePicker
import com.example.medisync.ui.theme.natureGreen

// ── COLORS ──
private val PageBackground = Color(0xFFF9FAFB)
private val SlotsBoxBackground = Color(0xFFE8EAED)
private val BottomBarBackground = Color(0xFFFFFFFF)
private val TopBarBackground = natureGreen
private val GreenPrimary2 = natureGreen
private val GrayText = Color(0xFF6B7280)
private val BlackText = Color(0xFF1A1A2E)
private val BorderColor = Color(0xFFE5E7EB)

// ── DATA CLASS ──
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
    // day selector
    var openSlotId by remember { mutableStateOf<Int?>(null) }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDay by remember { mutableStateOf("Mon") }

    // slots list
//    var slots by remember { mutableStateOf(listOf<Slot>()) }
    var slots by remember {
        mutableStateOf(
            listOf(
                Slot(id = 1, day = "Mon", startTime = "09:00 AM", endTime = "09:30 AM", duration = 30, mode = "Online", fee = "500"),
                Slot(id = 2, day = "Mon", startTime = "10:00 AM", endTime = "10:30 AM", duration = 30, mode = "Offline", fee = "700"),
                Slot(id = 3, day = "Tue", startTime = "11:00 AM", endTime = "11:45 AM", duration = 45, mode = "Online", fee = "500"),
                Slot(id = 4, day = "Wed", startTime = "02:00 PM", endTime = "02:30 PM", duration = 30, mode = "Offline", fee = "600"),
                Slot(id = 5, day = "Fri", startTime = "09:00 AM", endTime = "09:15 AM", duration = 15, mode = "Online", fee = "300"),
            )
        )
    }

    // input fields
    var startTime by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(30) }
    var mode by remember { mutableStateOf("Online") }
    var fee by remember { mutableStateOf("") }

    // filter slots by selected day
    val filteredSlots = slots.filter { it.day == selectedDay }

    // total hours for selected day
    val totalMinutes = filteredSlots.sumOf { it.duration }
    val totalHours = totalMinutes / 60

    Scaffold(
        // ── TOP BAR ──
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TopBarBackground)
                    .statusBarsPadding()
            ) {
                // back + title + stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "Regular Slots",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BlackText
                        )
                    }

                    // slots count + hours
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${filteredSlots.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                            Text(
                                text = "slots",
                                fontSize = 11.sp,
                                color = GrayText
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalHours",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlackText
                            )
                            Text(
                                text = "hr",
                                fontSize = 11.sp,
                                color = GrayText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

                // day selector
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

        // ── BOTTOM BAR ──
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .background(BottomBarBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // row 1 — start time + mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ModeSelector(
                        selectedMode = mode,
                        onModeSelected = { mode = it }
                    )
                    TimePicker(
                        label = "Start Time",
                        selectedTime = startTime,
                        onTimeSelected = { startTime = it }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .width(90.dp)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (fee.isEmpty()) {
                            Text("Fee (₹)", color = GrayText, fontSize = 12.sp)
                        }
                        BasicTextField(
                            value = fee,
                            onValueChange = { fee = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = BlackText
                            )
                        )
                    }
                }

                // row 2 — duration slider

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ){
                        DurationSliderField(
                            label = "Duration",
                            value = duration,
                            min = 5,
                            max = 120,
                            step = 5,
                            unit = "min",
                            onValueChange = { duration = it }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(GreenPrimary2, RoundedCornerShape(8.dp))
                            .clickable {
                                if (startTime.isNotEmpty()) {
                                    val newSlot = Slot(
                                        id = slots.size + 1,
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

        // ── SLOTS LIST ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures {
                        openSlotId = null  // close all on tap
                    }
                }
        ){
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
                            Text(
                                text = "No slots for $selectedDay",
                                color = GrayText,
                                fontSize = 14.sp
                            )
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
                            isOpen = openSlotId == slot.id,      // ← pass open state
                            onSwipeOpen = { openSlotId = slot.id },
                            onDelete = {
                                slots = slots.filter { it.id != slot.id }
                            }
                        )
                    }
                }
            }
        }
    }
}


// ── AUTO CALCULATE END TIME ──
fun calculateEndTime(startTime: String, durationMinutes: Int): String {
    try {
        val parts = startTime.split(":")
        val amPm = parts[1].split(" ")
        var hour = parts[0].toInt()
        var minute = amPm[0].toInt()
        val period = amPm[1]

        // convert to 24hr
        if (period == "PM" && hour != 12) hour += 12
        if (period == "AM" && hour == 12) hour = 0

        // add duration
        val totalMinutes = hour * 60 + minute + durationMinutes
        var endHour = (totalMinutes / 60) % 24
        val endMinute = totalMinutes % 60

        // convert back to 12hr
        val endPeriod = if (endHour >= 12) "PM" else "AM"
        if (endHour > 12) endHour -= 12
        if (endHour == 0) endHour = 12

        return "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')} $endPeriod"
    } catch (e: Exception) {
        return ""
    }
}


@Preview(showBackground = true)
@Composable
fun DoctorSlotsPreview() {
    DoctorRegularSlotsManage(
        navController = rememberNavController(),
        userId = 1
    )
}