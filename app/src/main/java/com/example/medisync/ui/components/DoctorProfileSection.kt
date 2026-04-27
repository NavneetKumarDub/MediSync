package com.example.medisync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.theme.DocProfileCardBg
import com.example.medisync.ui.theme.DocProfileDivider
import com.example.medisync.ui.theme.DocProfileTextPrimary

@Composable
fun DoctorProfileSection(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = DocProfileCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = title,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = DocProfileTextPrimary
            )
            HorizontalDivider(color = DocProfileDivider)
            content()
        }
    }
}