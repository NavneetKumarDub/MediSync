package com.example.medisync.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.local.ChatMessageEntity
import com.example.medisync.data.repository.ChatInboxRepository
import com.example.medisync.networks.ChatWebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


object ChatSession {
    var activeRoomId: Int? = null
}
data class ChatUiState(
    val isConnected: Boolean = false,
    val isReconnecting: Boolean = false,
    val headerStatus: String = ""
)

class ChatViewModel(
    private val repository: ChatInboxRepository,
    private val roomId: Int,
    val myUserId: Int,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<ChatMessageEntity>> = repository.getMessagesForRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        ChatSession.activeRoomId = roomId
        viewModelScope.launch {
            ChatWebSocketManager.state.collect { state ->
                _uiState.update {
                    it.copy(
                        isConnected = state == ChatWebSocketManager.State.CONNECTED,
                        isReconnecting = state == ChatWebSocketManager.State.RECONNECTING,
                        headerStatus = when (state) {
                            ChatWebSocketManager.State.CONNECTED -> "Online"
                            ChatWebSocketManager.State.RECONNECTING -> "Reconnecting…"
                            ChatWebSocketManager.State.CONNECTING -> "Connecting…"
                            ChatWebSocketManager.State.DISCONNECTED -> ""
                        }
                    )
                }
                if (state == ChatWebSocketManager.State.CONNECTED) {
                    ChatWebSocketManager.send("chat:join", mapOf("roomId" to roomId))
                }
            }
        }


        viewModelScope.launch {
            repository.syncMissingMessages(roomId, token)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(roomId, myUserId, text.trim())


        }
    }

    fun markAsRead(messageId: Int) {
        ChatWebSocketManager.send("chat:read", mapOf(
            "roomId" to roomId,
            "messageId" to messageId
        ))
    }

    override fun onCleared() {
        super.onCleared()
        ChatWebSocketManager.send("chat:leave", mapOf("roomId" to roomId))
    }

    class Factory(
        private val repository: ChatInboxRepository,
        private val roomId: Int,
        private val myUserId: Int,
        private val token: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository, roomId, myUserId, token) as T
        }
    }
}