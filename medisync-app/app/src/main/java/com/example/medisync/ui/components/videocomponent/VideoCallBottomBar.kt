package com.example.medisync.ui.components.videocomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.medisync.ui.theme.natureGreen

val bottomBarColor = natureGreen

@Composable
fun VideoCallBottomBar(
    isMicOn: Boolean,
    isVideoOn: Boolean,
    onMicToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onEndCall: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 1.dp, end = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = bottomBarColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                CallControlButton(
                    icon = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    isActive = isMicOn,
                    onClick = onMicToggle,
                    size = 45.dp
                )

                // End Call Button
                CallControlButton(
                    icon = Icons.Default.CallEnd,
                    isActive = false,
                    isDestructive = true,
                    size = 45.dp,
                    onClick = onEndCall
                )

                // Video Button
                CallControlButton(
                    icon = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    isActive = isVideoOn,
                    onClick = onVideoToggle,
                    size = 45.dp
                )

                // Flip Camera Button
                CallControlButton(
                    icon = Icons.Default.FlipCameraAndroid,
                    isActive = true,
                    onClick = onFlipCamera,
                    size = 45.dp
                )
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isDestructive: Boolean = false,
    size: Dp = 56.dp,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isDestructive -> Color.Red
        isActive -> Color(0xF73AC1FC)
        else -> Color.White
    }

    val iconColor = when {
        isDestructive -> Color.White
        isActive -> Color.White
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(size / 2)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VideoCallBottomBarPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Doctor's Video Feed Here", color = Color.LightGray)

            VideoCallBottomBar(
                isMicOn = true,
                isVideoOn = false,
                onMicToggle = {},
                onVideoToggle = {},
                onEndCall = {},
                onFlipCamera = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}