package com.example.medisync.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DoctorAvatar(
    name: String,
    userId: Int,
    photoKey: String?,
    token: String,
    size: Dp = 52.dp
) {
    // Just use your new ProfilePhoto component directly!
    ProfilePhoto(
        userId = userId,
        photoKey = photoKey,
        token = token,
        name = name,
        size = size
    )
}