package com.example.medisync.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.theme.SlotBluePrimary
import com.example.medisync.ui.theme.SlotChipSelected
import com.example.medisync.ui.theme.SlotChipUnselected
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SlotDateChip(
    date      : String,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    val parsed = remember(date) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
        } catch (e: Exception) { null }
    }
    val dayName   = remember(parsed) {
        parsed?.let { SimpleDateFormat("EEE", Locale.getDefault()).format(it) } ?: ""
    }
    val dayNumber = remember(parsed) {
        parsed?.let { SimpleDateFormat("d", Locale.getDefault()).format(it) } ?: ""
    }
    val month     = remember(parsed) {
        parsed?.let { SimpleDateFormat("MMM", Locale.getDefault()).format(it) } ?: ""
    }

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) SlotChipSelected else SlotChipUnselected)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text       = dayName,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isSelected) SlotBluePrimary else SlotChipSelected
        )
        Text(
            text       = dayNumber,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = if (isSelected) SlotBluePrimary else SlotChipSelected
        )
        Text(
            text     = month,
            fontSize = 10.sp,
            color    = if (isSelected) SlotBluePrimary
            else SlotChipSelected.copy(alpha = 0.8f)
        )
    }
}