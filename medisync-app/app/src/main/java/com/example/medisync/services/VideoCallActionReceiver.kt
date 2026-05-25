package com.example.medisync.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VideoCallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            VideoCallForegroundService.ACTION_TOGGLE_MIC ->
                VideoCallActionBus.send(VideoCallAction.TOGGLE_MIC)

            VideoCallForegroundService.ACTION_USE_SPEAKER ->
                VideoCallActionBus.send(VideoCallAction.USE_SPEAKER)

            VideoCallForegroundService.ACTION_END_CALL -> {
                VideoCallActionBus.send(VideoCallAction.END_CALL)
                VideoCallForegroundService.stop(context)
            }
        }
    }
}
