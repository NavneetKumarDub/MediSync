package com.example.medisync.ui.components

import android.R
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StepperField(
    label: String,
    min: Int = 0,
    max: Int = 500,
    unit: String,
    value: Int = 0,
    onValueChange: (Int) -> Unit
) {
    Column() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp)
        ) {

            Text(
                text = label,
                color = Color.Gray,
                fontSize = 13.sp
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (value > min) onValueChange((value - 1)) }
                ) {
                    Text(
                        text = "-",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 25.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(55.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = value.toString(),
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (parsed != null && parsed in min..max) onValueChange(parsed)
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 15.sp, textAlign = TextAlign.Center),

                    )
                }


                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = unit,
                    fontSize = 14.sp,
                )
                IconButton(
                    onClick = { if (value < max) onValueChange((value + 1)) }
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp
                    )
                }
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}



















