package com.example.medisync.ui.screens.doctor

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.CreateCustomSlotRequest
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.components.DurationSliderField
import com.example.medisync.ui.components.ModeSelector
import com.example.medisync.ui.components.SlotCard
import com.example.medisync.ui.components.TimePicker
import com.example.medisync.ui.theme.natureGreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val CalendarBg      = natureGreen
private val BottomBarBg     = Color(0xFFFFFFFF)
private val PageBg          = Color(0xFFF9FAFB)
private val AddButtonColor  = natureGreen
private val GrayText        = Color(0xFF6B7280)
private val BlackText       = Color(0xFF1A1A2E)
private val BorderColor     = Color(0xFFE5E7EB)
private val ChipUnselected  = Color(0xFFCBEAF8)
private val CalWhite        = Color(0xFFFFFFFF)
private val CalSelectedBg   = Color(0xFFFFFFFF)
private val CalSelectedText = natureGreen
private val CalDimText      = Color(0xFFE0F2FE)
private val SlotDotColor    = Color(0xFF4ADE80)

data class CustomSlot(
    val id: Int,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val mode: String,
    val fee: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DoctorCustomScheduleScreen(
    navController: NavController,
    userId: Int
) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()

    var openSlotId       by remember { mutableStateOf<Int?>(null) }
    var selectedDate     by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth     by remember { mutableStateOf(YearMonth.now()) }
    var calendarExpanded by remember { mutableStateOf(false) }
    var slots            by remember { mutableStateOf(listOf<CustomSlot>()) }
    var datesWithSlots   by remember { mutableStateOf(setOf<String>()) }
    var isLoading        by remember { mutableStateOf(false) }
    var errorMessage     by remember { mutableStateOf("") }

    var startTime by remember { mutableStateOf("") }
    var duration  by remember { mutableStateOf(30) }
    var mode      by remember { mutableStateOf("Online") }
    var fee       by remember { mutableStateOf("") }

    val filteredSlots = slots.filter { it.date == selectedDate.toString() }
    val totalMinutes  = filteredSlots.sumOf { it.duration }
    val totalHours    = totalMinutes / 60
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")

    fun fetchDatesWithSlots(month: YearMonth) {
        scope.launch {
            try {
                val token    = "Bearer ${TokenManager.getToken(context)}"
                val response = RetrofitInstance.api.getDatesWithSlots(
                    token = token,
                    month = month.monthValue,
                    year  = month.year
                )
                if (response.success) {
                    datesWithSlots = response.dates.toSet()
                }
            } catch (e: Exception) {
                errorMessage = e.message.toString()
            }
        }
    }

    fun fetchSlotsByDate(date: LocalDate) {
        scope.launch {
            isLoading    = true
            errorMessage = ""
            try {
                val token    = "Bearer ${TokenManager.getToken(context)}"
                val response = RetrofitInstance.api.getSlotsByDate(
                    token = token,
                    date  = date.toString()
                )
                Log.d("SlotsDebug", "success: ${response.success}")
                Log.d("SlotsDebug", "slots count: ${response.slots.size}")
                Log.d("SlotsDebug", "raw slots: ${response.slots}")
                if (response.success) {

                    slots = response.slots.map { item ->
                        CustomSlot(
                            id        = item.id,
                            date      = item.date,
                            startTime = convertTo12Hour(item.start_time),
                            endTime   = convertTo12Hour(item.end_time),
                            duration  = item.slot_duration_minutes,
                            mode      = item.consultation_type,
                            fee       = item.consultation_fee
                        )
                    }
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                errorMessage  = try {
                    org.json.JSONObject(errorBody ?: "").getString("message")
                } catch (ex: Exception) {
                    "Something went wrong"
                }
            } catch (e: Exception) {
                errorMessage = e.message.toString()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchDatesWithSlots(currentMonth)
        fetchSlotsByDate(selectedDate)
    }

    LaunchedEffect(selectedDate) {
        fetchSlotsByDate(selectedDate)
    }

    LaunchedEffect(currentMonth) {
        fetchDatesWithSlots(currentMonth)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(natureGreen)
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
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text       = "Custom Schedule",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${filteredSlots.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "slots", fontSize = 11.sp, color = ChipUnselected)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalHours", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "hr", fontSize = 11.sp, color = ChipUnselected)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { calendarExpanded = !calendarExpanded }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(
                            text       = selectedDate.format(dateFormatter),
                            color      = Color.White,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector    = if (calendarExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint           = Color.White,
                        modifier       = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = calendarExpanded,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CalendarBg)
                            .padding(bottom = 8.dp)
                    ) {
                        EmbeddedCalendar(
                            currentMonth   = currentMonth,
                            selectedDate   = selectedDate,
                            datesWithSlots = datesWithSlots,
                            onDateSelected = {
                                selectedDate     = it
                                calendarExpanded = false
                            },
                            onMonthChange  = { currentMonth = it }
                        )
                    }
                }
            }
        },

        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .background(BottomBarBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, fontSize = 11.sp)
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    ModeSelector(selectedMode = mode, onModeSelected = { mode = it })
                    TimePicker(label = "Start Time", selectedTime = startTime, onTimeSelected = { startTime = it })
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (fee.isEmpty()) Text("Fee (₹)", color = GrayText, fontSize = 12.sp)
                        BasicTextField(
                            value          = fee,
                            onValueChange  = { fee = it },
                            singleLine     = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle      = TextStyle(fontSize = 14.sp, color = BlackText)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DurationSliderField(
                            label         = "Duration",
                            value         = duration,
                            min           = 0,
                            max           = 120,
                            step          = 1,
                            unit          = "min",
                            onValueChange = { duration = it }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(AddButtonColor, RoundedCornerShape(8.dp))
                            .clickable {
                                if (startTime.isNotEmpty() && fee.isNotEmpty() && duration > 0) {
                                    scope.launch {
                                        try {
                                            val token      = "Bearer ${TokenManager.getToken(context)}"
                                            val startTime24 = convertTo24Hour(startTime)
                                            val endTime24   = convertTo24Hour(calculateEndTime(startTime, duration))

                                            val response = RetrofitInstance.api.createCustomSlot(
                                                token   = token,
                                                request = CreateCustomSlotRequest(
                                                    date                  = selectedDate.toString(),
                                                    start_time            = startTime24,
                                                    end_time              = endTime24,
                                                    consultation_fee      = fee.toInt(),
                                                    consultation_type     = mode,
                                                    slot_duration_minutes = duration
                                                )
                                            )

                                            if (response.success && response.slot != null) {
                                                val newSlot = CustomSlot(
                                                    id        = response.slot.id,
                                                    date      = selectedDate.toString(),
                                                    startTime = startTime,
                                                    endTime   = calculateEndTime(startTime, duration),
                                                    duration  = duration,
                                                    mode      = mode,
                                                    fee       = fee
                                                )
                                                slots        = slots + newSlot
                                                datesWithSlots = datesWithSlots + selectedDate.toString()
                                                startTime    = ""
                                                fee          = ""
                                                errorMessage = ""
                                            }
                                        } catch (e: retrofit2.HttpException) {
                                            val errorBody = e.response()?.errorBody()?.string()
                                            errorMessage  = try {
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
                            imageVector    = Icons.Default.Add,
                            contentDescription = "Add",
                            tint           = Color.White,
                            modifier       = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBg)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures {
                        openSlotId       = null
                        calendarExpanded = false
                    }
                }
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AddButtonColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PageBg)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    if (filteredSlots.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text     = "No slots for ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                                    color    = GrayText,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = filteredSlots,
                            key   = { _, slot -> slot.id }
                        ) { index, slot ->
                            SlotCard(
                                index       = index + 1,
                                startTime   = slot.startTime,
                                endTime     = slot.endTime,
                                duration    = slot.duration,
                                mode        = slot.mode,
                                fee         = slot.fee,
                                isOpen      = openSlotId == slot.id,
                                onSwipeOpen = { openSlotId = slot.id },
                                onDelete    = {
                                    scope.launch {
                                        try {
                                            val token    = "Bearer ${TokenManager.getToken(context)}"
                                            val response = RetrofitInstance.api.deleteCustomSlot(
                                                token  = token,
                                                slotId = slot.id
                                            )
                                            if (response.success) {
                                                slots      = slots.filter { it.id != slot.id }
                                                openSlotId = null
                                                errorMessage = ""
                                                if (slots.none { it.date == selectedDate.toString() }) {
                                                    datesWithSlots = datesWithSlots - selectedDate.toString()
                                                }
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmbeddedCalendar(
    currentMonth   : YearMonth,
    selectedDate   : LocalDate,
    datesWithSlots : Set<String>,
    onDateSelected : (LocalDate) -> Unit,
    onMonthChange  : (YearMonth) -> Unit
) {
    val dayHeaders  = listOf("M", "T", "W", "T", "F", "S", "S")
    val firstDay    = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value - 1
    val totalCells  = startOffset + daysInMonth
    val rows        = (totalCells + 6) / 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
            }
            Text(
                text       = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = day, color = CalDimText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex  = row * 7 + col
                    val dayNumber  = cellIndex - startOffset + 1
                    val isValid    = dayNumber in 1..daysInMonth
                    val date       = if (isValid) currentMonth.atDay(dayNumber) else null
                    val isSelected = date == selectedDate
                    val hasSlots   = date != null && datesWithSlots.contains(date.toString())

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .then(
                                if (isSelected) Modifier.clip(CircleShape).background(CalSelectedBg)
                                else Modifier
                            )
                            .then(
                                if (date != null)
                                    Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        onDateSelected(date)
                                    }                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isValid) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text       = dayNumber.toString(),
                                    color      = if (isSelected) CalSelectedText else CalWhite,
                                    fontSize   = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign  = TextAlign.Center
                                )
                                if (hasSlots) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(SlotDotColor, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DoctorCustomSchedulePreview() {
    DoctorCustomScheduleScreen(navController = rememberNavController(), userId = 1)
}