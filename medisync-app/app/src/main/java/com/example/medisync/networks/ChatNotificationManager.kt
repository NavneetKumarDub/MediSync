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
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import java.time.Instant

class ChatNotificationManager(
    private val repository: ChatInboxRepository,
    private val context: Context 
    ) {
    private var listeningJob: Job? = null
    private var myUserId: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun startListening(scope: CoroutineScope) {
        if (listeningJob?.isActive == true) return

        listeningJob = scope.launch {
            myUserId = TokenManager.getUserId(context)

            // 1. Concurrently listen to connection state for retries
            launch {
                ChatWebSocketManager.state.collect { state ->
                    if (state == ChatWebSocketManager.State.CONNECTED) {
                        val token = TokenManager.getToken(context)
                        if (token != null) {
                            // Re-sync the inbox to catch missing messages globally
                            repository.syncChats(token)
                            // Retry pending messages globally!
                            repository.resendAllPendingMessages()
                        }
                    }
                }
            }

            // 2. Listen to incoming WebSocket events
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
                Log.d("Chatmessage","inside chat:message")

                val roomId = data["roomId"]?.jsonPrimitive?.int ?: return
                val senderId = data["senderId"]?.jsonPrimitive?.int ?: return
                val serverId = data["messageId"]?.jsonPrimitive?.content ?: return
                val text = data["text"]?.jsonPrimitive?.contentOrNull
                val sentAt = data["sentAt"]?.jsonPrimitive?.content ?: return

                val messageType = data["messageType"]?.jsonPrimitive?.contentOrNull ?: "text"
                val fileKey = data["fileKey"]?.jsonPrimitive?.contentOrNull
                val fileName = data["fileName"]?.jsonPrimitive?.contentOrNull
                val fileType = data["fileType"]?.jsonPrimitive?.contentOrNull
                val fileSize = data["fileSize"]?.jsonPrimitive?.longOrNull
                val syncVersion = data["syncVersion"]?.jsonPrimitive?.long ?: 0L

                 if(senderId != myUserId) {

                    val incomingMessage = ChatMessageEntity(
                        id = serverId,
                        roomId = roomId,
                        senderId = senderId,
                        message = text,
                        messageType = messageType,
                        fileKey = fileKey,
                        fileName = fileName,
                        fileType = fileType,
                        fileSize = fileSize,
                        status = "DELIVERED",
                        sentAt = sentAt,
                        updatedAt = sentAt,
                        syncVersion = syncVersion
                    )
                     repository.insertIncomingMessage(incomingMessage)
                 }
            }
            "chat:ack" ->{
                Log.d("Chatack","inside chat:ack")
                val roomId = data["roomId"]?.jsonPrimitive?.int ?: return
                val senderId = data["senderId"]?.jsonPrimitive?.int ?: return
                val serverId = data["messageId"]?.jsonPrimitive?.content ?: return
                val text = data["text"]?.jsonPrimitive?.contentOrNull
                val sentAt = data["sentAt"]?.jsonPrimitive?.content ?: return

                val messageType = data["messageType"]?.jsonPrimitive?.contentOrNull ?: "text"
                val fileKey = data["fileKey"]?.jsonPrimitive?.contentOrNull
                val fileName = data["fileName"]?.jsonPrimitive?.contentOrNull
                val fileType = data["fileType"]?.jsonPrimitive?.contentOrNull
                val fileSize = data["fileSize"]?.jsonPrimitive?.longOrNull
                val syncVersion = data["syncVersion"]?.jsonPrimitive?.long ?: 0L

              //  repository.updateMessageStatusById(serverId, "SENT")


                val myMessage = ChatMessageEntity(
                    id = serverId,
                    roomId = roomId,
                    senderId = senderId,
                    message = text,
                    messageType = messageType,
                    fileKey = fileKey,
                    fileName = fileName,
                    fileType = fileType,
                    fileSize = fileSize,
                    status = "SENT",
                    sentAt = sentAt,
                    updatedAt = sentAt,
                    syncVersion = syncVersion
                )

                repository.upsertMessage(myMessage)


            }

            "chat:read" -> {
                val messageId = data["messageId"]?.jsonPrimitive?.content ?: return
                repository.markMessageAsReadLocally(messageId)
            }


        }
    }
}