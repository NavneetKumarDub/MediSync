package com.example.medisync.ui.screens.video

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.medisync.ui.components.videocomponent.VideoCallBottomBar
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VideoRoomScreen() {
    // 1. Connection & Media States
    var isPeerConnected by remember { mutableStateOf(false) }
    var isRemoteVideoOn by remember { mutableStateOf(false) }
    var isLocalVideoOn by remember { mutableStateOf(true) }
    var isMicOn by remember { mutableStateOf(true) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 2. UI Visibility States
    var isControlsVisible by remember { mutableStateOf(true) }

    // 3. Auto-hide Timer Logic
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(6000) // Controls will stay for 4 seconds
            isControlsVisible = false
        }
    }

    // 1. Get screen dimensions and density
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val marginPx = with(density) { 16.dp.toPx() }
    val pipWidthPx = with(density) { 100.dp.toPx() }
    val pipHeightPx = with(density) { 150.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

// These are the "Fence" values
    val minX = -(screenWidthPx - pipWidthPx - (marginPx * 2))
    val maxX = 0f
    val minY = 0f
    val maxY = screenHeightPx - pipHeightPx - (marginPx * 2)


    // Import androidx.compose.ui.geometry.Rect
    val constraints = Rect(
        left = minX,
        top = minY,
        right = maxX,
        bottom = maxY
    )

    Scaffold(
        containerColor = Color.Black
    ) { _ -> // We ignore innerPadding to keep the video edge-to-edge

        // MAIN STAGE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { isControlsVisible = !isControlsVisible }
        ) {
            // LAYER 1: Background (Remote Video or Avatar)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isPeerConnected && isRemoteVideoOn) {
                    // Placeholder for WebRTC Remote SurfaceView
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
                } else {
                    // Avatar Scenario
                    Box(
                        modifier = Modifier.size(120.dp).background(Color.Gray, shape = RoundedCornerShape(100.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Avatar", color = Color.White)
                    }
                }
            }

            // LAYER 2: Floating Back Button (Top Left)
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopStart).padding(top = 48.dp, start = 16.dp)
            ) {
                IconButton(
                    onClick = { /* Handle Back Navigation */ },
                    modifier = Modifier.background(Color(0x66000000), shape = RoundedCornerShape(100.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            // LAYER 3: Local PiP (Top Right)
            // This stays visible even when controls are hidden
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd) // Keep the starting position top-right
                    .offset {
                        // This physically moves the card based on the drag
                        IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                    }
                    .padding(top = 48.dp, end = 16.dp)
                    .size(width = 100.dp, height = 150.dp)
                    .pointerInput(Unit) {
                        // This listens for the actual drag gesture
                        detectDragGestures { change, dragAmount ->
                            change.consume() // Stops the "tap" from leaking to the background video
                            val newX = (offset.x + dragAmount.x).coerceIn(minX, maxX)
                            val newY = (offset.y + dragAmount.y).coerceIn(minY, maxY)

                            offset = Offset(newX, newY)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                if (isLocalVideoOn) {
                    // Placeholder for Local SurfaceView
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        Text("You", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // LAYER 4: Bottom Control Bar
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                VideoCallBottomBar(
                    isMicOn = isMicOn,
                    isVideoOn = isLocalVideoOn,
                    onMicToggle = { isMicOn = !isMicOn },
                    onVideoToggle = { isLocalVideoOn = !isLocalVideoOn },
                    onEndCall = { /* Handle disconnect */ },
                    modifier = Modifier.background(Color.Transparent)
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VideoRoomScreenPreview() {
    MaterialTheme {
        // This will show you the full-screen layout with the
        // floating PiP and the bottom bar in their initial states.
        VideoRoomScreen()
    }
}