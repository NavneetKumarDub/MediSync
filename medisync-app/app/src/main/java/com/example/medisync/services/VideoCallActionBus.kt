package com.example.medisync.services

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class VideoCallAction {
    TOGGLE_MIC,
    USE_SPEAKER,
    END_CALL
}

object VideoCallActionBus {
    private val _actions = MutableSharedFlow<VideoCallAction>(extraBufferCapacity = 8)
    val actions = _actions.asSharedFlow()

    fun send(action: VideoCallAction) {
        _actions.tryEmit(action)
    }
}
