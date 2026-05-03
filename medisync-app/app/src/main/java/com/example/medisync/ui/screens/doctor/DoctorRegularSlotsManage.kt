package com.example.medisync.ui.screens.doctor

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview


// --- 1. DATA MODELS & LOGIC ---

data class ShiftTemplate(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTime: String, // format: "HH:mm" (24hr)
    val durationMinutes: Long
) {
    val endTime: String
        @SuppressLint("NewApi")
        get() {
            val parsed = LocalTime.parse(startTime)
            return parsed.plusMinutes(durationMinutes).format(DateTimeFormatter.ofPattern("HH:mm"))
        }

    // Helper to display friendly 12-hour time in UI
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedTimeRange(): String {
        val start12 = LocalTime.parse(startTime).format(DateTimeFormatter.ofPattern("hh:mm a"))
        val end12 = LocalTime.parse(endTime).format(DateTimeFormatter.ofPattern("hh:mm a"))
        return "$start12 — $end12"
    }
}

// The Overlap checking formula
@RequiresApi(Build.VERSION_CODES.O)
fun isOverlap(newShift: ShiftTemplate, existingShifts: List<ShiftTemplate>): Boolean {
    val newStart = LocalTime.parse(newShift.startTime)
    val newEnd = LocalTime.parse(newShift.endTime)

    return existingShifts.any { existing ->
        val existingStart = LocalTime.parse(existing.startTime)
        val existingEnd = LocalTime.parse(existing.endTime)
        newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)
    }
}

val DaysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

// --- 2. MAIN SCREEN COMPOSABLE ---

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterScheduleScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // State (In production, this moves to your ViewModel)
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var weeklySchedule by remember { mutableStateOf(mapOf<String, List<ShiftTemplate>>()) }

    // Bottom Control State
    var inputHour by remember { mutableIntStateOf(9) }
    var inputMinute by remember { mutableIntStateOf(0) }
    var inputDuration by remember { mutableLongStateOf(30L) }

    val selectedDay = DaysOfWeek[selectedDayIndex]
    val todayShifts = weeklySchedule[selectedDay]?.sortedBy { LocalTime.parse(it.startTime) } ?: emptyList()

    Scaffold(
        containerColor = Color(0xFFF5F6F6),
        topBar = {
            TopAppBar(
                title = { Text("Weekly Schedule", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Layer 1: Dashboard
            DailyDashboard(selectedDay, todayShifts)

            // Layer 2: Timeline Canvas
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (todayShifts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No shifts scheduled for $selectedDay.\nAdd your working hours below.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                } else {
                    items(todayShifts, key = { it.id }) { shift ->
                        ShiftCard(
                            shift = shift,
                            onDelete = {
                                val currentList = weeklySchedule[selectedDay]?.toMutableList() ?: mutableListOf()
                                currentList.remove(shift)
                                weeklySchedule = weeklySchedule.toMutableMap().apply { put(selectedDay, currentList) }
                            }
                        )
                    }
                }
            }

            // Layer 3: Control Center
            ControlCenter(
                selectedDay = selectedDay,
                inputHour = inputHour,
                inputMinute = inputMinute,
                inputDuration = inputDuration,
                onNextDay = {
                    selectedDayIndex = (selectedDayIndex + 1) % DaysOfWeek.size
                },
                onTimeChange = { h, m -> inputHour = h; inputMinute = m },
                onDurationChange = { newDuration -> inputDuration = newDuration.coerceAtLeast(15L) },
                onAddShift = {
                    val formattedStart = String.format("%02d:%02d", inputHour, inputMinute)
                    val newShift = ShiftTemplate(startTime = formattedStart, durationMinutes = inputDuration)

                    if (isOverlap(newShift, todayShifts)) {
                        Toast.makeText(context, "Overlap detected! Please choose a different time.", Toast.LENGTH_SHORT).show()
                    } else {
                        val currentList = weeklySchedule[selectedDay]?.toMutableList() ?: mutableListOf()
                        currentList.add(newShift)
                        weeklySchedule = weeklySchedule.toMutableMap().apply { put(selectedDay, currentList) }
                    }
                }
            )
        }
    }
}

// --- 3. UI COMPONENTS ---

@Composable
fun DailyDashboard(day: String, shifts: List<ShiftTemplate>) {
    val totalMinutes = shifts.sumOf { it.durationMinutes }
    val totalHours = totalMinutes / 60.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(day, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111B21))
            Column(horizontalAlignment = Alignment.End) {
                Text("${shifts.size} Shifts", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (totalHours > 0) String.format("Total Load: %.1f hrs", totalHours) else "Total Load: 0 hrs",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShiftCard(shift: ShiftTemplate, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue indicator stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF2A9DF4))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shift.getFormattedTimeRange(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111B21)
                )
                Text(
                    text = "${shift.durationMinutes}-min slots",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFE53935))
            }
        }
    }
}

@Composable
fun ControlCenter(
    selectedDay: String,
    inputHour: Int,
    inputMinute: Int,
    inputDuration: Long,
    onNextDay: () -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onDurationChange: (Long) -> Unit,
    onAddShift: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Row 1: Day Navigator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedDay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A9DF4))
                TextButton(onClick = onNextDay) {
                    Text("Next Day", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Input Pods
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Time Pod (Mocked interaction for now - would open TimePickerDialog)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            // TODO: Launch Material 3 TimePicker here
                            // For quick demo, we just advance hour by 1
                            onTimeChange((inputHour + 1) % 24, inputMinute)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F0F4))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Start Time", fontSize = 12.sp, color = Color.Gray)
                        val amPm = if (inputHour >= 12) "PM" else "AM"
                        val displayHour = if (inputHour % 12 == 0) 12 else inputHour % 12
                        Text(
                            text = String.format("%02d:%02d %s", displayHour, inputMinute, amPm),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111B21)
                        )
                    }
                }

                // Duration Pod with Stepper
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F0F4))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Slot Duration", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "${inputDuration}m",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111B21)
                            )
                        }
                        Column {
                            IconButton(onClick = { onDurationChange(inputDuration + 15) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                            }
                            IconButton(onClick = { onDurationChange(inputDuration - 15) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Row 3: Action Button
            Button(
                onClick = onAddShift,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A9DF4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Shift", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    showBackground = true,
    name = "Master Schedule - Light Mode",
    device = "id:pixel_7_pro" // Renders it on a modern phone screen size
)
@Composable
fun MasterScheduleScreenPreview() {
    // If you have a custom theme like MediSyncTheme, wrap it here!
    // MediSyncTheme {
    MasterScheduleScreen(
        onBack = { /* Preview doesn't need to navigate */ }
    )
    // }
}