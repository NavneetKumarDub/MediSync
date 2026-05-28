package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileRow(
    label: String,
    value: String,
    placeholder: String = "",
    editable: Boolean = true,
    onValueChange: ((String) -> Unit)? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var str by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 17.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )


            if (isEditing && editable) {
                    BasicTextField(
                        value = value,
                        onValueChange = { onValueChange?.invoke(it) },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            textAlign = TextAlign.End  
                        ),
                        modifier = Modifier.widthIn(
                            min = 80.dp,
                            max = 250.dp
                        )  
                    )

            }
                else {
                Text(
                    text = if (value.isEmpty()) placeholder else value,
                    color = if (value.isEmpty()) Color.LightGray else Color.Black,
                    fontSize = 15.sp,
                    modifier = if (editable) Modifier.clickable { isEditing = true } else Modifier
                )
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}