package com.example.medisync.ui.screens.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.WebSocketManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.util.UUID

/**
 * Represents one chat message in the UI.
 *  - localId: UUID assigned on client, used as LazyColumn key (never changes)
 *  - serverId: DB id from server, null if still sending
 *  - isMine: true if this user sent it
 *  - isRead: flipped true when server confirms read (for sent messages)
 */
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
    val isConnected: Boolean = false,
    val isReconnecting: Boolean = false,
    val headerStatus: String = "",   // "Online", "Reconnecting…", etc.
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
            WebSocketManager.state.collect { state ->
                _uiState.update {
                    it.copy(
                        isConnected = state == WebSocketManager.State.CONNECTED,
                        isReconnecting = state == WebSocketManager.State.RECONNECTING,
                        headerStatus = when (state) {
                            WebSocketManager.State.CONNECTED -> "Online"
                            WebSocketManager.State.RECONNECTING -> "Reconnecting…"
                            WebSocketManager.State.CONNECTING -> "Connecting…"
                            WebSocketManager.State.DISCONNECTED -> ""
                        }
                    )
                }
            }
        }

        // Observe chat:* events from the shared WebSocket
        viewModelScope.launch {
            WebSocketManager.events
                .filter { it.type.startsWith("chat:") }
                .collect { event -> handleChatEvent(event) }
        }
    }

    fun joinRoom(roomId: Int, context: Context) {
        currentRoomId = roomId
        viewModelScope.launch {
            // Load saved userId so we know which messages are "mine"
            currentUserId = TokenManager.getUserId(context) ?: -1

            // Make sure WS is connected
            WebSocketManager.connect(context)

            // Send join request
            WebSocketManager.send("chat:join", mapOf("roomId" to roomId))
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

        WebSocketManager.send("chat:message", mapOf(
            "roomId" to roomId,
            "text" to text
        ))
    }

    fun markAsRead(roomId: Int, messageId: Int) {
        WebSocketManager.send("chat:read", mapOf(
            "roomId" to roomId,
            "messageId" to messageId
        ))
    }

    private fun handleChatEvent(event: WebSocketManager.ServerEvent) {
        val data = event.data as? JsonObject ?: return

        when (event.type) {
            "chat:joined" -> {
                Log.d(TAG, "Joined room ${data["roomId"]?.jsonPrimitive?.content}")
            }

            "chat:history" -> {
                // Bulk load of past messages
                val messages = data["messages"]?.jsonArray ?: return
                val parsed = messages.mapNotNull { parseMessageFromHistory(it.jsonObject) }
                _uiState.update { it.copy(messages = parsed) }
            }

            "chat:message" -> {
                // New message arrived (could be our own echoed back, or from other person)
                val senderId = data["senderId"]?.jsonPrimitive?.int ?: return
                val serverId = data["messageId"]?.jsonPrimitive?.int ?: return
                val text = data["text"]?.jsonPrimitive?.content ?: return
                val sentAtStr = data["sentAt"]?.jsonPrimitive?.content

                val timestamp = parseServerTimestamp(sentAtStr)

                _uiState.update { state ->
                    val isMine = senderId == currentUserId

                    // If this is our echoed message, find the optimistic entry and update it
                    if (isMine) {
                        val existingIndex = state.messages.indexOfLast {
                            it.isMine && it.serverId == null && it.text == text
                        }
                        if (existingIndex >= 0) {
                            val updated = state.messages.toMutableList()
                            updated[existingIndex] = updated[existingIndex].copy(
                                serverId = serverId,
                                timestamp = timestamp
                            )
                            return@update state.copy(messages = updated)
                        }
                    }

                    // New message from other user (or ours from another device)
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

    private fun parseServerTimestamp(sentAt: String?): Long {
        if (sentAt == null) return System.currentTimeMillis()
        return try {
            // Postgres returns ISO timestamps like "2026-04-20T09:30:00.000Z"
            // Simple parse — fall back to current time on any error
            java.time.Instant.parse(sentAt).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (currentRoomId != -1) {
            WebSocketManager.send("chat:leave", mapOf("roomId" to currentRoomId))
        }
    }

    companion object { private const val TAG = "ChatViewModel" }
}