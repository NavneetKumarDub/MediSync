package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.theme.DocProfileBluePrimary
import com.example.medisync.ui.theme.DocProfileIconBg
import com.example.medisync.ui.theme.DocProfileTextHint
import com.example.medisync.ui.theme.DocProfileTextPrimary

@Composable
fun DoctorDetailRow(
    icon : ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DocProfileIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = DocProfileBluePrimary,
                modifier           = Modifier.size(18.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text     = label,
                fontSize = 11.sp,
                color    = DocProfileTextHint
            )
            Text(
                text       = value,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = DocProfileTextPrimary
            )
        }
    }
}