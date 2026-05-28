package com.example.medisync.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.ui.theme.natureGreen


private val DurationGreen = Color(0xFFE5E7EB)
private val StepperColor = Color.Black
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
    var textValue by remember { mutableStateOf(value.toString()) }


    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.width(60.dp)
        )

        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = ((max - min) / step) - 1,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.DarkGray,
                activeTrackColor = Color.DarkGray,
                inactiveTrackColor = DurationBorder
            )
        )

        
        Column(
            modifier = Modifier
                .width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { if (value + step <= max) onValueChange(value + step) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrowup),
                    contentDescription = "up",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
            Box(
                modifier = Modifier
                    .border(1.dp, DurationBorder, RoundedCornerShape(8.dp))
                    .width(50.dp).height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                

                
                BasicTextField(
                    value = value.toString(),
                    onValueChange = { input ->
                        textValue = input
                        val parsed = input.toIntOrNull()
                        if (parsed != null && parsed in min..max) {
                            onValueChange(parsed)
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(48.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DurationBlack,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

            }
            IconButton(
                onClick = { if (value - step >= min) onValueChange(value - step) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrowdown),
                    contentDescription = "down",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }

        }

        
        Text(
            text = unit,
            fontSize = 13.sp,
            color = DurationGray
        )
    }
}