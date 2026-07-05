package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.medisync.MediSyncApplication

@Composable
fun ProfilePhoto(
    userId: Int?,
    photoKey: String?,
    token: String,
    name: String,
    size: Dp = 52.dp,
    shape: Shape = CircleShape,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MediSyncApplication
    val repository = app.profilePhotoRepository

    val localFile by if (userId != null) {
        repository.observePhoto(userId)
            .collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    LaunchedEffect(userId, photoKey, token) {
        if (userId != null && userId > 0 && token.isNotBlank()) {
            repository.refresh(
                userId = userId,
                token = token,
                expectedPhotoKey = photoKey
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Color(0xFFE1F5FE)),
        contentAlignment = Alignment.Center
    ) {
        if (localFile != null) {
            AsyncImage(
                model = localFile,
                contentDescription = "Profile photo of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0288D1)
            )
        }
    }
}
private fun String.getInitialsForPhoto(): String {
    return this.removePrefix("Dr. ")
        .removePrefix("Dr ")
        .trim()
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
}
