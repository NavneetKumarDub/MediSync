package com.example.medisync.ui.screens.video

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.medisync.ui.components.videocomponent.VideoCallBottomBar
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.services.VideoCallAction
import com.example.medisync.services.VideoCallActionBus
import com.example.medisync.services.VideoCallForegroundService
import com.example.medisync.viewmodels.AudioOutputKind
import com.example.medisync.viewmodels.VideoCallViewModel
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import io.getstream.webrtc.android.ui.VideoTextureViewRenderer

@Composable
fun VideoRoomPermissionGate(
    navController: NavController,
    roomId: Int,
    isInPipMode: Boolean = false,
    isPipModeRequested: Boolean = false,
    onRequestPip: () -> Unit = {},
    onRemoteVideoAvailabilityChanged: (Boolean) -> Unit = {},
    onPreparePipUiChanged: ((() -> Unit)?) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val cameraGranted = permissionsMap[Manifest.permission.CAMERA] == true
        val micGranted = permissionsMap[Manifest.permission.RECORD_AUDIO] == true
        hasPermissions = cameraGranted && micGranted
    }

    if (hasPermissions) {
        VideoRoomScreen(
            navController = navController,
            roomId = roomId,
            isInPipMode = isInPipMode,
            isPipModeRequested = isPipModeRequested,
            onRequestPip = onRequestPip,
            onRemoteVideoAvailabilityChanged = onRemoteVideoAvailabilityChanged,
            onPreparePipUiChanged = onPreparePipUiChanged,
            onHangUp = onNavigateBack,
            onBack = onNavigateBack
        )
    } else {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera & Microphone Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MediSync needs access to your camera and microphone to connect you with your doctor securely.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    }
                ) {
                    Text("Grant Permissions")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateBack) {
                    Text("Cancel and Go Back")
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VideoRoomScreen(
    navController: NavController,
    roomId: Int = 0,
    isInPipMode: Boolean = false,
    isPipModeRequested: Boolean = false,
    viewModel: VideoCallViewModel = viewModel(),
    onRequestPip: () -> Unit = {},
    onRemoteVideoAvailabilityChanged: (Boolean) -> Unit = {},
    onPreparePipUiChanged: ((() -> Unit)?) -> Unit = {},
    onBack: () -> Unit = {},
    onHangUp: () -> Unit = {}
){
    val context = LocalContext.current
    val remoteVideoTrack by viewModel.remoteVideoTrack.collectAsState()
    val isPeerConnected by viewModel.isPeerConnected.collectAsState()
    val isLocalVideoOn by viewModel.isLocalVideoOn.collectAsState()
    val isMicOn by viewModel.isMicOn.collectAsState()
    val audioOutputs by viewModel.audioOutputs.collectAsState()
    val selectedAudioOutput by viewModel.selectedAudioOutput.collectAsState()
    val localVideoTrack by viewModel.localVideoTrack.collectAsState()
    val latestAudioOutputs by rememberUpdatedState(audioOutputs)
    val latestLocalVideoTrack by rememberUpdatedState(localVideoTrack)
    val latestRemoteVideoTrack by rememberUpdatedState(remoteVideoTrack)

    var offset by remember { mutableStateOf(Offset.Zero) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPreparingForPip by remember { mutableStateOf(false) }
    val isPipLayout = isInPipMode || isPipModeRequested || isPreparingForPip
    val shouldShowFullCallUi = !isPipLayout

    fun endActiveCall() {
        VideoCallForegroundService.stop(context)
        viewModel.endCall()
        onHangUp()
    }

    fun requestPipOrEndWaitingCall() {
        if (remoteVideoTrack != null) {
            isPreparingForPip = true
            onRequestPip()
        } else {
            endActiveCall()
        }
    }

    BackHandler {
        requestPipOrEndWaitingCall()
    }

    LaunchedEffect(roomId) {
        VideoCallForegroundService.start(context, roomId)
        viewModel.refreshAudioOutputs()
        viewModel.connect(roomId)
    }

    LaunchedEffect(remoteVideoTrack) {
        onRemoteVideoAvailabilityChanged(remoteVideoTrack != null)
    }

    LaunchedEffect(isInPipMode, isPipModeRequested) {
        if (!isInPipMode && !isPipModeRequested) {
            isPreparingForPip = false
        }
    }

    LaunchedEffect(Unit) {
        VideoCallActionBus.actions.collect { action ->
            when (action) {
                VideoCallAction.TOGGLE_MIC -> viewModel.toggleMic()
                VideoCallAction.USE_SPEAKER -> {
                    latestAudioOutputs
                        .firstOrNull { it.kind == AudioOutputKind.SPEAKER }
                        ?.let { viewModel.selectAudioOutput(it) }
                }
                VideoCallAction.END_CALL -> endActiveCall()
            }
        }
    }


    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(6000)
            isControlsVisible = false
        }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val marginPx = with(density) { 16.dp.toPx() }
    val pipWidthPx = with(density) { 100.dp.toPx() }
    val pipHeightPx = with(density) { 150.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val minX = -(screenWidthPx - pipWidthPx - (marginPx * 2))
    val maxX = 0f
    val minY = 0f
    val maxY = screenHeightPx - pipHeightPx - (marginPx * 2)

    val localRenderer = remember {
        VideoTextureViewRenderer(context).apply {
            init(
                viewModel.eglBaseContext,
                object : RendererCommon.RendererEvents {
                    override fun onFirstFrameRendered() = Unit

                    override fun onFrameResolutionChanged(
                        videoWidth: Int,
                        videoHeight: Int,
                        rotation: Int
                    ) = Unit
                }
            )
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            setMirror(true)
        }
    }

    val remoteRenderer = remember {
        SurfaceViewRenderer(context).apply {
            init(viewModel.eglBaseContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            setEnableHardwareScaler(true)
            setMirror(false)
        }
    }

    DisposableEffect(localRenderer) {
        onPreparePipUiChanged {
            isPreparingForPip = true
            latestLocalVideoTrack?.removeSink(localRenderer)
            localRenderer.pauseVideo()
            localRenderer.visibility = View.GONE

            remoteRenderer.visibility = View.VISIBLE
            latestRemoteVideoTrack?.removeSink(remoteRenderer)
            latestRemoteVideoTrack?.addSink(remoteRenderer)
        }

        onDispose {
            onPreparePipUiChanged(null)
        }
    }

    LaunchedEffect(isPipLayout) {
        remoteRenderer.setScalingType(
            if (isPipLayout) {
                RendererCommon.ScalingType.SCALE_ASPECT_FIT
            } else {
                RendererCommon.ScalingType.SCALE_ASPECT_FILL
            }
        )

        delay(250)
        remoteVideoTrack?.removeSink(remoteRenderer)
        remoteVideoTrack?.addSink(remoteRenderer)
    }
    LaunchedEffect(localVideoTrack, isPipLayout) {
        if (isPipLayout) {
            localVideoTrack?.removeSink(localRenderer)
            localRenderer.pauseVideo()
            localRenderer.visibility = View.GONE
        } else {
            localRenderer.visibility = View.VISIBLE
            localRenderer.resumeVideo()
            delay(300)
            if (localVideoTrack != null) {
                localVideoTrack?.removeSink(localRenderer)
                localVideoTrack?.addSink(localRenderer)
            }
        }
    }

    LaunchedEffect(remoteVideoTrack, isPipLayout) {
        remoteRenderer.visibility = View.VISIBLE
        remoteVideoTrack?.removeSink(remoteRenderer)
        remoteVideoTrack?.addSink(remoteRenderer)
    }

    LaunchedEffect(isPeerConnected) {
        if (isPeerConnected) {
            delay(1500)
            viewModel.reattachLocalSink()
        }
    }
    LaunchedEffect(remoteVideoTrack) {
        if (remoteVideoTrack != null) {
            delay(500)
            if (shouldShowFullCallUi) {
                localVideoTrack?.removeSink(localRenderer)
                localVideoTrack?.addSink(localRenderer)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            localVideoTrack?.removeSink(localRenderer)
            remoteVideoTrack?.removeSink(remoteRenderer)

            Thread.sleep(100)
            localRenderer.pauseVideo()
            remoteRenderer.clearImage()
            remoteRenderer.release()
        }
    }

    Scaffold(containerColor = Color.Black) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { isControlsVisible = !isControlsVisible }
        ) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isPeerConnected && remoteVideoTrack != null) {
                    AndroidView(
                        factory = { remoteRenderer },
                        update = {},
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (isPeerConnected) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color(0xFF2E7D32), shape = RoundedCornerShape(100.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("DR", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Camera Off", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Waiting for the other person to join...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isControlsVisible && shouldShowFullCallUi,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 48.dp, start = 16.dp)
            ) {
                IconButton(
                    onClick = {
                        requestPipOrEndWaitingCall()
                    },
                    modifier = Modifier.background(Color(0x66000000), shape = RoundedCornerShape(100.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            if (shouldShowFullCallUi) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .padding(top = 48.dp, end = 16.dp)
                        .size(width = 100.dp, height = 150.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = (offset.x + dragAmount.x).coerceIn(minX, maxX)
                                val newY = (offset.y + dragAmount.y).coerceIn(minY, maxY)
                                offset = Offset(newX, newY)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { localRenderer },
                            update = {},
                            modifier = Modifier.fillMaxSize()
                        )

                        if (!isLocalVideoOn) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "You",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            
            AnimatedVisibility(
                visible = isControlsVisible && shouldShowFullCallUi,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                VideoCallBottomBar(
                    isMicOn = isMicOn,
                    isVideoOn = isLocalVideoOn,
                    audioOutputs = audioOutputs,
                    selectedAudioOutput = selectedAudioOutput,
                    onMicToggle = { viewModel.toggleMic() },
                    onVideoToggle = { viewModel.toggleVideo() },
                    onAudioOutputSelected = { viewModel.selectAudioOutput(it) },
                    onEndCall = { endActiveCall() },
                    onFlipCamera = { viewModel.flipCamera() },
                    modifier = Modifier.background(Color.Transparent)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun VideoRoomScreenPreview() {
    MaterialTheme {
        VideoRoomScreen(
            navController = rememberNavController(),
            roomId = 0,
            onHangUp = {}
        )
    }
}
