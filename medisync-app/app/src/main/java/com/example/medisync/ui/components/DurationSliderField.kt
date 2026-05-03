package com.example.medisync.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── COLORS ──
private val DurationGreen = Color(0xFF2E7D32)
private val DurationBorder = Color(0xFFE5E7EB)
private val DurationGray = Color(0xFF6B7280)
private val DurationBlack = Color(0xFF1A1A2E)

@Composable
fun DurationSliderField(
    label: String = "Duration",
    value: Int,
    min: Int = 5,
    max: Int = 120,
    step: Int = 5,
    unit: String = "min",
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ── LABEL ──
        Text(
            text = label,
            fontSize = 14.sp,
            color = DurationGray,
            modifier = Modifier.width(70.dp)
        )

        // ── SLIDER ──
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = ((max - min) / step) - 1,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = DurationGreen,
                activeTrackColor = DurationGreen,
                inactiveTrackColor = DurationBorder
            )
        )

        // ── STEPPER BOX ──
        Column(
            modifier = Modifier
                .border(1.dp, DurationBorder, RoundedCornerShape(8.dp))
                .width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // up arrow
            IconButton(
                onClick = { if (value + step <= max) onValueChange(value + step) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                Text(
                    text = "▲",
                    fontSize = 10.sp,
                    color = DurationGreen
                )
            }

            // value display
            Text(
                text = "$value",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DurationBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // down arrow
            IconButton(
                onClick = { if (value - step >= min) onValueChange(value - step) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                Text(
                    text = "▼",
                    fontSize = 10.sp,
                    color = DurationGreen
                )
            }
        }

        // ── UNIT ──
        Text(
            text = unit,
            fontSize = 13.sp,
            color = DurationGray
        )
    }
}