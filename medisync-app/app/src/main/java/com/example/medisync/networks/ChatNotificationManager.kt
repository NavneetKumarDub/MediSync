package com.example.medisync.networks

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.ChatMessageEntity
import com.example.medisync.data.repository.ChatInboxRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

class ChatNotificationManager(
    private val repository: ChatInboxRepository,
    private val context: Context // Pass context here once
    ) {
    private var listeningJob: Job? = null
    private var myUserId: Int? = null // Store the ID internally

    @RequiresApi(Build.VERSION_CODES.O)
    fun startListening(scope: CoroutineScope) {
        if (listeningJob?.isActive == true) return

        listeningJob = scope.launch {
            myUserId = TokenManager.getUserId(context)
            ChatWebSocketManager.events
                .filter { it.type.startsWith("chat:") }
                .collect { event ->
                    handleGlobalChatEvent(event)
                }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleGlobalChatEvent(event: ChatWebSocketManager.ServerEvent) {
        val data = event.data as? JsonObject ?: return

        when (event.type) {
            "chat:message" -> {
                val roomId = data["roomId"]?.jsonPrimitive?.int ?: return
                val clientTempId = data["clientTempId"]?.jsonPrimitive?.contentOrNull // Get the fingerprint
                val senderId = data["senderId"]?.jsonPrimitive?.int ?: return
                val serverId = data["messageId"]?.jsonPrimitive?.int ?: return
                val text = data["text"]?.jsonPrimitive?.content ?: return
                val sentAt = data["sentAt"]?.jsonPrimitive?.content ?: return

                if (senderId == myUserId && clientTempId != null) {
                    repository.reconcileMessageId(clientTempId, serverId, sentAt)
                } else {
                    // IT IS A NEW MESSAGE:
                    // Either from someone else or a sync event, insert as normal.
                    val incomingMessage = ChatMessageEntity(
                        id = serverId, // Use the server ID
                        clientTempId = clientTempId,
                        roomId = roomId,
                        senderId = senderId,
                        message = text,
                        isRead = false,
                        sentAt = sentAt,
                        updatedAt = sentAt
                    )
                    repository.insertIncomingMessage(incomingMessage)
                }
            }

            "chat:read" -> {
                val messageId = data["messageId"]?.jsonPrimitive?.int ?: return
                repository.markMessageAsReadLocally(messageId)
            }
        }
    }
}