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
import androidx.compose.ui.text.style.TextOverflow
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
import java.text.SimpleDateFormat
import java.util.Locale

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
        val contentColor = when {
            isSelected -> SlotSelectedText
            isBooked   -> SlotBookedText
            else       -> SlotAvailableText
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = formatTime(slot.startTime),
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = contentColor,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            val meta = slotMetaText(slot)
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text       = meta,
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color      = contentColor.copy(alpha = if (isSelected) 0.9f else 0.72f),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun slotMetaText(slot: SlotItem): String {
    val type = slot.consultationType
        ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        ?.let { if (it.lowercase().contains("online")) "Online" else "Offline" }

    val duration = (slot.slotDurationMinutes ?: calculateDurationMinutes(slot.startTime, slot.endTime))
        .takeIf { it > 0 }
        .let { "${it}m" }

    return listOfNotNull(type, duration).joinToString(" • ")
}

private fun calculateDurationMinutes(startTime: String, endTime: String): Int {
    return try {
        val parser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val start = parser.parse(startTime)?.time ?: return 0
        val end = parser.parse(endTime)?.time ?: return 0
        ((end - start) / 60000L).toInt().coerceAtLeast(0)
    } catch (_: Exception) {
        0
    }
}
