package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── COLORS ──
private val TimePickerGreen = Color(0xFF2E7D32)
private val TimePickerBorder = Color(0xFFE5E7EB)
private val TimePickerSelected = Color(0xFF2E7D32)
private val TimePickerUnselected = Color(0xFFE5E7EB)
private val TimePickerGray = Color(0xFF6B7280)
private val TimePickerBlack = Color(0xFF1A1A2E)

@Composable
fun TimePicker(
    label: String = "Start Time",
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    // ── TRIGGER BOX ──
    Box(
        modifier = Modifier
            .border(1.dp, TimePickerBorder, RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = if (selectedTime.isEmpty()) label else selectedTime,
            color = if (selectedTime.isEmpty()) TimePickerGray else TimePickerBlack,
            fontSize = 14.sp
        )
    }

    // ── DIALOG ──
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Select Time",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // ── HOUR PICKER ──
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                hour = if (hour >= 12) 1 else hour + 1
                            }) {
                                Text("▲", fontSize = 18.sp, color = TimePickerGreen)
                            }
                            Text(
                                text = hour.toString().padStart(2, '0'),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TimePickerBlack
                            )
                            IconButton(onClick = {
                                hour = if (hour <= 1) 12 else hour - 1
                            }) {
                                Text("▼", fontSize = 18.sp, color = TimePickerGreen)
                            }
                        }

                        Text(
                            text = ":",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TimePickerBlack
                        )

                        // ── MINUTE PICKER ──
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                minute = if (minute >= 55) 0 else minute + 5
                            }) {
                                Text("▲", fontSize = 18.sp, color = TimePickerGreen)
                            }
                            Text(
                                text = minute.toString().padStart(2, '0'),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TimePickerBlack
                            )
                            IconButton(onClick = {
                                minute = if (minute <= 0) 55 else minute - 5
                            }) {
                                Text("▼", fontSize = 18.sp, color = TimePickerGreen)
                            }
                        }

                        // ── AM/PM ──
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isAm) TimePickerSelected else TimePickerUnselected,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { isAm = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "AM",
                                    color = if (isAm) Color.White else TimePickerGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (!isAm) TimePickerSelected else TimePickerUnselected,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { isAm = false }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "PM",
                                    color = if (!isAm) Color.White else TimePickerGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} ${if (isAm) "AM" else "PM"}"
                    onTimeSelected(formattedTime)
                    showDialog = false
                }) {
                    Text("OK", color = TimePickerGreen, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = TimePickerGray)
                }
            }
        )
    }
}