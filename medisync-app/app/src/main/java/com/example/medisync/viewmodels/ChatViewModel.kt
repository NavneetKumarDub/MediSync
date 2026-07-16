package com.example.medisync.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.local.ChatMessageEntity
import com.example.medisync.data.repository.ChatInboxRepository
import com.example.medisync.networks.ChatWebSocketManager
import com.example.medisync.utils.FileCacheManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive


object ChatSession {
    var activeRoomId: Int? = null
}
data class ChatUiState(
    val isConnected: Boolean = false,
    val isReconnecting: Boolean = false,
    val isOtherUserOnline: Boolean = false,
    val isOtherUserTyping: Boolean = false
) {
    val otherUserStatusText: String
        get() = when {
            isOtherUserTyping -> "Typing..."
            isOtherUserOnline -> "Online"
            else -> ""
        }
}

class ChatViewModel(
    private val repository: ChatInboxRepository,
    private val roomId: Int,
    val myUserId: Int,
    private val token: String,
    val otherUserId:Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private var typingJob: Job? = null

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
                android.util.Log.d("ChatLifecycle", "WebSocket State Changed: $state")

                _uiState.update {
                    it.copy(
                        isConnected = state == ChatWebSocketManager.State.CONNECTED,
                        isReconnecting = state == ChatWebSocketManager.State.RECONNECTING,

                    )
                }
                if (state == ChatWebSocketManager.State.CONNECTED) {
                    android.util.Log.d("connected", "WebSocket CONNECTED! Joining room and resending pending messages...")
                    ChatWebSocketManager.send("chat:join", mapOf("roomId" to roomId))
                    delay(1000)
                    repository.syncMissingMessages(roomId, token)

                    ChatWebSocketManager.send("chat:subscribe_presence", mapOf("userId" to otherUserId))



                }
            }

        }
        viewModelScope.launch{
            ChatWebSocketManager.events
                .filter{it.type == "chat:presence" || it.type == "chat:typing"}
                .collect{event ->
                    val data = event.data as? JsonObject?:return@collect
                    if (event.type == "chat:presence") {
                        val eventUserId = data["userId"]?.jsonPrimitive?.int
                        val isOnline = data["isOnline"]?.jsonPrimitive?.boolean
                        if (eventUserId == otherUserId && isOnline != null) {
                            _uiState.update { it.copy(isOtherUserOnline = isOnline) }
                        }
                    }
                    else if (event.type == "chat:typing") {
                        val eventUserId = data["userId"]?.jsonPrimitive?.int
                        val isTyping = data["isTyping"]?.jsonPrimitive?.boolean
                        if (eventUserId == otherUserId && isTyping != null) {
                            _uiState.update { it.copy(isOtherUserTyping = isTyping) }
                        }
                    }
                }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(roomId, myUserId, text.trim())


        }
    }
    fun onUserTyping(){
        if(typingJob?.isActive != true){
            ChatWebSocketManager.send("chat:typing",mapOf(
                "roomId" to roomId,
                "targetUserId" to otherUserId,
                "isTyping" to true
            ))
        }
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2000)
            ChatWebSocketManager.send("chat:typing",mapOf(
                "roomId" to roomId,
                "targetUserId" to otherUserId,
                "isTyping" to false
            ))
        }
    }
    fun openFile(
        fileKey: String,
        onUrlReady: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val url = repository.getChatFileViewUrl(
                    token = token,
                    key = fileKey
                )
                onUrlReady(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openFileCached(
        context: Context,
        fileKey: String,
        fileName: String,
        fileType: String?,
        onFileReady: (Uri) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext
                val file = FileCacheManager.getOrDownloadFile(
                    context = appContext,
                    fileKey = fileKey,
                    fileName = fileName,
                    fileType = fileType
                ) {
                    repository.getChatFileViewUrl(
                        token = token,
                        key = fileKey
                    )
                }
                onFileReady(FileCacheManager.contentUri(appContext, file))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendFile(
        context: Context,
        uri: Uri,
        fileName: String,
        fileType: String,
        fileSize: Long?,
        saveAsReport: Boolean
    ) {
        viewModelScope.launch {
            try {
                repository.uploadAndSendFileMessage(
                    context = context,
                    token = token,
                    roomId = roomId,
                    myUserId = myUserId,
                    uri = uri,
                    fileName = fileName,
                    fileType = fileType,
                    fileSize = fileSize,
                    saveAsReport = saveAsReport
                )
            } catch (e: Throwable) {
                android.util.Log.e("CHAT_FILE_UPLOAD", "Upload failed", e)
                e.printStackTrace()
            }
        }
    }
    fun markAsRead(messageId: String) {
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
        private val token: String,
        private val otherUserId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository, roomId, myUserId, token,otherUserId) as T
        }
    }
}
