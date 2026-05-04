package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

private val Black      = Color(0xFF1A1A1A)
private val LightGray  = Color(0xFFF3F4F6)
private val MediumGray = Color(0xFF6B7280)

@Composable
fun TimePicker(
    label: String,
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(110.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Black)
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (selectedTime.isEmpty()) label else selectedTime,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }

    if (showDialog) {
        TimePickerDialog(
            onDismiss = { showDialog = false },
            onConfirm = { time ->
                onTimeSelected(time)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedHour   by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm           by remember { mutableStateOf(true) }

    val hours   = (1..12).toList()
    val minutes = (0..59).toList()

    val hourListState   = rememberLazyListState(initialFirstVisibleItemIndex = (selectedHour - 1).coerceAtLeast(0))
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedMinute.coerceAtLeast(0))

    val scope = rememberCoroutineScope()

    LaunchedEffect(hourListState.isScrollInProgress) {
        if (!hourListState.isScrollInProgress) {
            val index = hourListState.firstVisibleItemIndex.coerceIn(0, hours.size - 1)
            selectedHour = hours[index]
            scope.launch { hourListState.animateScrollToItem(index) }
        }
    }

    LaunchedEffect(minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            val index = minuteListState.firstVisibleItemIndex.coerceIn(0, minutes.size - 1)
            selectedMinute = minutes[index]
            scope.launch { minuteListState.animateScrollToItem(index) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Time",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrumRoll(
                        items = hours,
                        selectedIndex = hours.indexOf(selectedHour),
                        listState = hourListState,
                        label = { it.toString().padStart(2, '0') },
                        onItemSelected = { index ->
                            selectedHour = hours[index]
                            scope.launch { hourListState.animateScrollToItem(index) }
                        }
                    )

                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Black)

                    DrumRoll(
                        items = minutes,
                        selectedIndex = selectedMinute,
                        listState = minuteListState,
                        label = { it.toString().padStart(2, '0') },
                        onItemSelected = { index ->
                            selectedMinute = minutes[index]
                            scope.launch { minuteListState.animateScrollToItem(index) }
                        }
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AmPmButton(label = "AM", selected = isAm,  onClick = { isAm = true  })
                        AmPmButton(label = "PM", selected = !isAm, onClick = { isAm = false })
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MediumGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val period = if (isAm) "AM" else "PM"
                        val time = "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')} $period"
                        onConfirm(time)
                    }) {
                        Text("OK", color = Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> DrumRoll(
    items: List<T>,
    selectedIndex: Int,
    listState: LazyListState,
    label: (T) -> String,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(150.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LightGray)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White)
                    )
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 51.dp)
        ) {
            items(items.size) { index ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(items[index]),
                        fontSize = if (isSelected) 22.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Black else MediumGray.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AmPmButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Black else LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MediumGray
        )
    }
}