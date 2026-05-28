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
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import com.example.medisync.viewmodels.AudioOutputDevice
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VolumeUp
import com.example.medisync.viewmodels.AudioOutputKind
val bottomBarColor = natureGreen

@Composable
fun VideoCallBottomBar(
    isMicOn: Boolean,
    isVideoOn: Boolean,
    audioOutputs: List<AudioOutputDevice>,
    selectedAudioOutput: AudioOutputDevice?,
    onMicToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onAudioOutputSelected: (AudioOutputDevice) -> Unit,
    onEndCall: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioIcon = when (selectedAudioOutput?.kind) {
        AudioOutputKind.BLUETOOTH -> Icons.Default.BluetoothAudio
        AudioOutputKind.EARPIECE -> Icons.Default.PhoneInTalk
        AudioOutputKind.SPEAKER,
        null -> Icons.Default.VolumeUp
    }
    var isAudioMenuOpen by remember { mutableStateOf(false) }

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
                CallControlButton(
                    icon = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    isActive = isVideoOn,
                    onClick = onVideoToggle,
                    size = 45.dp
                )
                var isAudioMenuOpen by remember { mutableStateOf(false) }

                Box {
                    CallControlButton(
                        icon = audioIcon,
                        isActive = true,
                        onClick = { isAudioMenuOpen = true },
                        size = 45.dp
                    )

                    DropdownMenu(
                        expanded = isAudioMenuOpen,
                        onDismissRequest = { isAudioMenuOpen = false },
                        modifier = Modifier
                            .background(Color(0xFF202124), RoundedCornerShape(18.dp))
                            .width(210.dp)
                    ) {
                        audioOutputs.forEach { output ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = output.name,
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    val icon = when (output.kind) {
                                        AudioOutputKind.BLUETOOTH -> Icons.Default.BluetoothAudio
                                        AudioOutputKind.SPEAKER -> Icons.Default.VolumeUp
                                        AudioOutputKind.EARPIECE -> Icons.Default.PhoneInTalk
                                    }

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                },
                                trailingIcon = {
                                    if (output.kind == selectedAudioOutput?.kind) {
                                        Text("✓", color = Color.White)
                                    }
                                },
                                onClick = {
                                    onAudioOutputSelected(output)
                                    isAudioMenuOpen = false
                                }
                            )
                        }
                    }
                }
                CallControlButton(
                    icon = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    isActive = isMicOn,
                    onClick = onMicToggle,
                    size = 45.dp
                )
                CallControlButton(
                    icon = Icons.Default.FlipCameraAndroid,
                    isActive = true,
                    onClick = onFlipCamera,
                    size = 45.dp
                )
                CallControlButton(
                    icon = Icons.Default.CallEnd,
                    isActive = false,
                    isDestructive = true,
                    size = 45.dp,
                    onClick = onEndCall
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
























