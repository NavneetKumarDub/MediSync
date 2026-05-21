package com.example.medisync.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.medisync.data.local.ChatInboxDao
import com.example.medisync.data.local.ChatInboxEntity
import com.example.medisync.data.local.ChatMessageDao
import com.example.medisync.data.local.ChatMessageEntity
import com.example.medisync.networks.ApiService
import com.example.medisync.networks.ChatWebSocketManager
import com.example.medisync.viewmodels.ChatSession
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.CancellationException

data class ChatInboxResponse(
    @SerializedName("chats") val chats: List<ChatInboxDto>
)

data class ChatInboxDto(
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("other_user_id") val otherUserId: Int,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("profile_photo") val photoUrl: String?,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("last_message_time") val lastMessageTime: String?,
    @SerializedName("unread_count") val unreadCount: Int?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class MessageDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("roomId")
    val roomId: Int,

    @SerializedName("senderId")
    val senderId: Int,

    // Let's say your Node server actually calls it "text" in JSON
    @SerializedName("text")
    val text: String,

    @SerializedName("isRead")
    val isRead: Boolean,

    @SerializedName("sentAt")
    val sentAt: String
)

class ChatInboxRepository(
    private val chatDao: ChatInboxDao,
    private val chatMessageDao: ChatMessageDao,
    private val api: ApiService
) {

    val allChats: Flow<List<ChatInboxEntity>> = chatDao.getAllChats()

    suspend fun syncChats(token: String) {
        try {
            val lastSync = chatDao.getLastSyncTimestamp() ?: "1970-01-01T00:00:00Z"

            val response = api.getInbox("Bearer $token", lastSync)

            if (response.isSuccessful) {
                response.body()?.chats?.let { networkChats ->
                    if (networkChats.isNotEmpty()) {
                        val entities = networkChats.map { dto ->
                            ChatInboxEntity(
                                roomId = dto.roomId,
                                otherUserId = dto.userId,
                                displayName = dto.name,
                                photoUrl = dto.profilePhoto,
                                lastMessage = dto.lastMessage,
                                lastMessageTime = dto.lastMessageTime,
                                unreadCount = dto.unreadCount ?: 0,
                                updatedAt = dto.updatedAt
                            )
                        }
                        chatDao.upsertChats(entities)
                    }
                }
            } else {
                Log.e("ChatSync", "Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ChatSync", "Network error, operating offline: ${e.message}")
        }
    }

    fun getMessagesForRoom(roomId: Int): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesForRoom(roomId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendMessage(roomId: Int, myUserId: Int, text: String) {
        val localId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val fingerprint = java.util.UUID.randomUUID().toString()

        val tempMessage = ChatMessageEntity(
            id = localId,
            clientTempId = fingerprint,
            roomId = roomId,
            senderId = myUserId,
            message = text,
            isRead = true,
            sentAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        chatMessageDao.insertMessage(tempMessage)

        ChatWebSocketManager.send("chat:message", mapOf(
            "roomId" to roomId,
            "text" to text,
            "clientTempId" to fingerprint
        ))
    }
    suspend fun syncMissingMessages(roomId: Int , token: String) {
        try {
            val lastTimestamp = chatMessageDao.getLastSyncTimestamp(roomId)

            val response = api.getRoomMessages("Bearer $token",roomId, lastTimestamp)

            if (response.isSuccessful && response.body() != null) {
                val newMessages = response.body()!!.map { msg ->
                    ChatMessageEntity(
                        id = msg.id,
                        roomId = roomId,
                        senderId = msg.senderId,
                        message = msg.text,
                        isRead = msg.isRead,
                        sentAt = msg.sentAt
                    )
                }
                if (newMessages.isNotEmpty()) {
                    chatMessageDao.upsertMessages(newMessages)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ChatRepository", "Network offline or unreachable during sync: ${e.message}")
        }
    }
    suspend fun reconcileMessageId(tempId: String, serverId: Int, serverSentAt: String) {
        chatMessageDao.reconcileMessageId(tempId, serverId, serverSentAt)
    }
    suspend fun insertIncomingMessage(message: ChatMessageEntity) {
        chatMessageDao.insertOrIgnoreMessage(message)
        val increment = if (ChatSession.activeRoomId == message.roomId) 0 else 1

        // 3. Update the Chat List UI!
        chatDao.updateInboxSnippet(
            roomId = message.roomId,
            message = message.message,
            time = message.sentAt,
            incrementBy = increment
        )
    }

    // Takes the incoming read receipt and updates SQLite
    suspend fun markMessageAsReadLocally(messageId: Int) {
        chatMessageDao.markSingleMessageAsRead(messageId)
    }
}