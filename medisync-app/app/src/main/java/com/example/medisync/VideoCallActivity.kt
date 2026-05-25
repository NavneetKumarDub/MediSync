package com.example.medisync

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.rememberNavController
import com.example.medisync.ui.screens.video.VideoRoomPermissionGate
import com.example.medisync.ui.theme.MediSyncTheme

class VideoCallActivity : ComponentActivity() {

    private val isInPipModeState = mutableStateOf(false)
    private val isPipModeRequestedState = mutableStateOf(false)
    private var hasRemoteVideo = false
    private var preparePipUi: (() -> Unit)? = null

    private fun buildPipParams(): PictureInPictureParams? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(9, 16))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
        }

        return builder.build()
    }

    private fun updatePipParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildPipParams()?.let { setPictureInPictureParams(it) }
        }
    }

    private fun prepareForPip() {
        isPipModeRequestedState.value = true
        preparePipUi?.invoke()
        updatePipParams()
    }

    private fun requestPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasRemoteVideo && !isFinishing) {
            prepareForPip()
            buildPipParams()?.let { params ->
                enterPictureInPictureMode(params)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val roomId = intent.getIntExtra("roomId", 0)

        updatePipParams()

        setContent {
            MediSyncTheme {
                VideoRoomPermissionGate(
                    navController = rememberNavController(),
                    roomId = roomId,
                    isInPipMode = isInPipModeState.value,
                    isPipModeRequested = isPipModeRequestedState.value,
                    onRequestPip = { requestPip() },
                    onRemoteVideoAvailabilityChanged = { available ->
                        hasRemoteVideo = available
                    },
                    onPreparePipUiChanged = { preparePipUi = it },
                    onNavigateBack = { finish() }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (hasRemoteVideo) {
            prepareForPip()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipModeState.value = isInPictureInPictureMode

        if (!isInPictureInPictureMode) {
            isPipModeRequestedState.value = false
        }
    }
}
