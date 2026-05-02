package com.example.medisync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.theme.SlotTextHint
import com.example.medisync.ui.theme.SlotTextPrimary

@Composable
fun SlotGroupHeader(
    icon : String,
    title: String,
    time : String
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Column {
            Text(
                text       = title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = SlotTextPrimary
            )
            Text(
                text     = time,
                fontSize = 11.sp,
                color    = SlotTextHint
            )
        }
    }
}