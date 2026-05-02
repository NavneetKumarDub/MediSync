package com.example.medisync.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.ChatWebSocketManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.time.Instant
import java.util.UUID

data class ChatMessage(
    val localId: String = UUID.randomUUID().toString(),
    val serverId: Int? = null,
    val text: String,
    val senderId: Int,
    val timestamp: Long,
    val isMine: Boolean,
    val isRead: Boolean = false,
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val otherUserName: String = "Loading...",
    val isConnected: Boolean = false,
    val isReconnecting: Boolean = false,
    val headerStatus: String = "",
    val error: String? = null,
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = -1
    private var currentRoomId: Int = -1

    init {
        // Observe WebSocket connection state → update header
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
            }
        }

        // Observe chat:* events from the shared WebSocket
        viewModelScope.launch {
            ChatWebSocketManager.events
                .filter { it.type.startsWith("chat:") }
                .collect { event -> handleChatEvent(event) }
        }
    }

    fun joinRoom(roomId: Int, context: Context) {
        currentRoomId = roomId
        viewModelScope.launch {
            // 1. Load current user ID so we know which bubbles are "mine"
            currentUserId = TokenManager.getUserId(context) ?: -1

            // 2. Fetch Metadata AND History via REST API (Parallel)
            // This ensures the UI is populated before the socket even opens
            fetchRoomMetadata(roomId, context)
            fetchChatHistory(roomId, context)

            // 3. Connect WebSocket for LIVE messages only
            ChatWebSocketManager.connect(context)

            // 4. Join the room (Server will now only send NEW messages)
            ChatWebSocketManager.send("chat:join", mapOf("roomId" to roomId))
        }
    }

    private fun fetchChatHistory(roomId: Int, context: Context) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(context)
                if (token != null) {
                    val response = RetrofitInstance.api.getRoomMessages("Bearer $token", roomId)
                    if (response.isSuccessful && response.body() != null) {
                        // Map the API models to your ChatMessage UI model
                        val history = response.body()!!.messages.map { apiMsg ->
                            ChatMessage(
                                serverId = apiMsg.id,
                                text = apiMsg.text,
                                senderId = apiMsg.senderId,
                                timestamp = parseServerTimestamp(apiMsg.createdAt),
                                isMine = apiMsg.senderId == currentUserId,
                                isRead = false // Or map from API if available
                            )
                        }
                        _uiState.update { it.copy(messages = history) }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to fetch history via REST", e)
            }
        }
    }
    private fun fetchRoomMetadata(roomId: Int, context: Context) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(context)
                if (token != null) {
                    val response = RetrofitInstance.api.getRoomMetadata("Bearer $token", roomId)
                    if (response.isSuccessful) {
                        val name = response.body()?.displayName ?: "User"
                        _uiState.update { it.copy(otherUserName = name) }
                    } else {
                        _uiState.update { it.copy(otherUserName = "Chat") }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch metadata", e)
                _uiState.update { it.copy(otherUserName = "Chat") }
            }
        }
    }

    fun sendMessage(roomId: Int, text: String) {
        // Optimistic add — show immediately, update when server confirms
        val localMsg = ChatMessage(
            text = text,
            senderId = currentUserId,
            timestamp = System.currentTimeMillis(),
            isMine = true,
            serverId = null,
            isRead = false
        )
        _uiState.update { it.copy(messages = it.messages + localMsg) }

        ChatWebSocketManager.send("chat:message", mapOf(
            "roomId" to roomId,
            "text" to text,
            "localId" to localMsg.localId// SENDING localId TO SERVER
        ))
    }

    fun markAsRead(roomId: Int, messageId: Int) {
        ChatWebSocketManager.send("chat:read", mapOf(
            "roomId" to roomId,
            "messageId" to messageId
        ))
    }

    private fun handleChatEvent(event: ChatWebSocketManager.ServerEvent) {
        val data = event.data as? JsonObject ?: return

        when (event.type) {
            "chat:joined" -> {
                Log.d(TAG, "Joined room ${data["roomId"]?.jsonPrimitive?.content}")
            }

            "chat:message" -> {
                val senderId = data["senderId"]?.jsonPrimitive?.int ?: return
                val serverId = data["messageId"]?.jsonPrimitive?.int ?: return
                val text = data["text"]?.jsonPrimitive?.content ?: return
                val sentAtStr = data["sentAt"]?.jsonPrimitive?.content

                // Use contentOrNull to safely handle if the server forgets to send it
                val localIdFromServer = data["localId"]?.jsonPrimitive?.contentOrNull

                val timestamp = parseServerTimestamp(sentAtStr)

                _uiState.update { state ->
                    val isMine = senderId == currentUserId

                    if (isMine) {
                        // 1st Try: Match by the exact localId
                        var existingIndex = state.messages.indexOfLast {
                            it.localId == localIdFromServer
                        }

                        // 2nd Try: If the backend didn't send localId, match by text that hasn't been confirmed yet
                        if (existingIndex == -1) {
                            existingIndex = state.messages.indexOfLast {
                                it.isMine && it.serverId == null && it.text == text
                            }
                        }

                        // If we found the temporary optimistic message, update it!
                        if (existingIndex >= 0) {
                            val updated = state.messages.toMutableList()
                            updated[existingIndex] = updated[existingIndex].copy(
                                serverId = serverId,
                                timestamp = timestamp
                            )
                            return@update state.copy(messages = updated)
                        }
                    }

                    // If it's not ours, or we somehow didn't find the original, add it as a new message
                    state.copy(
                        messages = state.messages + ChatMessage(
                            serverId = serverId,
                            text = text,
                            senderId = senderId,
                            timestamp = timestamp,
                            isMine = isMine,
                        )
                    )
                }
            }

            "chat:read" -> {
                val messageId = data["messageId"]?.jsonPrimitive?.int ?: return
                _uiState.update { state ->
                    state.copy(messages = state.messages.map { msg ->
                        if (msg.serverId == messageId) msg.copy(isRead = true) else msg
                    })
                }
            }
        }
    }

    private fun parseMessageFromHistory(obj: JsonObject): ChatMessage? {
        val id = obj["id"]?.jsonPrimitive?.int ?: return null
        val senderId = obj["sender_id"]?.jsonPrimitive?.int ?: return null
        val text = obj["message"]?.jsonPrimitive?.content ?: return null
        val sentAt = obj["sent_at"]?.jsonPrimitive?.content
        val isRead = obj["is_read"]?.jsonPrimitive?.boolean ?: false

        return ChatMessage(
            serverId = id,
            text = text,
            senderId = senderId,
            timestamp = parseServerTimestamp(sentAt),
            isMine = senderId == currentUserId,
            isRead = isRead
        )
    }

    @SuppressLint("NewApi")
    private fun parseServerTimestamp(sentAt: String?): Long {
        if (sentAt == null) return System.currentTimeMillis()
        return try {
            // Postgres returns ISO timestamps like "2026-04-20T09:30:00.000Z"
            // Simple parse — fall back to current time on any error
            Instant.parse(sentAt).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (currentRoomId != -1) {
            ChatWebSocketManager.send("chat:leave", mapOf("roomId" to currentRoomId))
        }
    }

    companion object { private const val TAG = "ChatViewModel" }
}