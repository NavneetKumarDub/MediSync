package com.example.medisync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val ModeRadioGreen = Color(0xFF080908)
private val ModeRadioGray = Color(0xFF6B7280)

@Composable
fun ModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf("Online", "Offline").forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioButton(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ModeRadioGreen,
                            unselectedColor = ModeRadioGray
                        )
                    )
                    Text(
                        text = mode,
                        fontSize = 14.sp,
                        color = if (selectedMode == mode) ModeRadioGreen else ModeRadioGray
                    )
                }
            }
        }
    }
}