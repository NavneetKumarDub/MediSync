package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.networks.AvailabilitySlot
import com.example.medisync.ui.theme.DocProfileBluePrimary
import com.example.medisync.ui.theme.DocProfileDayChipBg
import com.example.medisync.ui.theme.DocProfileDayChipText
import com.example.medisync.ui.theme.DocProfileTextHint
import com.example.medisync.ui.theme.DocProfileTextSecondary

@Composable
fun DoctorAvailabilityRow(slot: AvailabilitySlot) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DocProfileDayChipBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text       = slot.dayOfWeek,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = DocProfileDayChipText
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.Schedule,
                contentDescription = null,
                tint               = DocProfileTextHint,
                modifier           = Modifier.size(14.dp)
            )
            Text(
                text     = "${slot.startTime} → ${slot.endTime}",
                fontSize = 13.sp,
                color    = DocProfileTextSecondary
            )
        }
    }
}