package com.example.medisync.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.medisync.networks.SlotItem
import com.example.medisync.ui.theme.SlotAvailableBg
import com.example.medisync.ui.theme.SlotAvailableBorder
import com.example.medisync.ui.theme.SlotAvailableText
import com.example.medisync.ui.theme.SlotBookedBg
import com.example.medisync.ui.theme.SlotBookedText
import com.example.medisync.ui.theme.SlotSelectedBg
import com.example.medisync.ui.theme.SlotSelectedText
import com.example.medisync.ui.screens.patient.formatTime

@Composable
fun SlotChip(
    slot      : SlotItem,
    isSelected: Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    val isBooked      = slot.status == "booked"
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> SlotSelectedBg
                    isBooked   -> SlotBookedBg
                    else       -> SlotAvailableBg
                }
            )
            .then(
                if (!isSelected && !isBooked)
                    Modifier.border(
                        1.dp,
                        SlotAvailableBorder,
                        RoundedCornerShape(10.dp)
                    )
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = !isBooked,
                onClick           = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = formatTime(slot.startTime),
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            color      = when {
                isSelected -> SlotSelectedText
                isBooked   -> SlotBookedText
                else       -> SlotAvailableText
            }
        )
    }
}