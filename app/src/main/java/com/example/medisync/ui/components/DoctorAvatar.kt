package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.medisync.ui.theme.DocProfileAvatarBg
import com.example.medisync.ui.theme.DocProfileAvatarText

@Composable
fun DoctorAvatar(
    name    : String,
    photoUrl: String?,
    size    : Dp = 52.dp
) {
    Box(
        modifier         = Modifier
            .size(size)
            .clip(CircleShape)
            .background(DocProfileAvatarBg),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model              = photoUrl,
                contentDescription = name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text       = name
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifEmpty { "?" },
                fontSize   = (size.value * 0.3f).sp,
                fontWeight = FontWeight.Bold,
                color      = DocProfileAvatarText
            )
        }
    }
}