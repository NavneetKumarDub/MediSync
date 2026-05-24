package com.example.medisync.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.medisync.data.local.ChatInboxDao
import com.example.medisync.data.local.ChatInboxEntity
import com.example.medisync.data.local.ChatMessageDao
import com.example.medisync.data.local.ChatMessageEntity
import com.example.medisync.networks.ApiService
import com.example.medisync.networks.ChatFileUploadUrlRequest
import com.example.medisync.networks.ChatWebSocketManager
import com.example.medisync.viewmodels.ChatSession
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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

    @SerializedName("text")
    val text: String?,

    @SerializedName("messageType")
    val messageType: String = "text",

    @SerializedName("fileKey")
    val fileKey: String? = null,

    @SerializedName("fileName")
    val fileName: String? = null,

    @SerializedName("fileType")
    val fileType: String? = null,

    @SerializedName("fileSize")
    val fileSize: Long? = null,

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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendFileMessage(
        roomId: Int,
        myUserId: Int,
        fileName: String,
        fileType: String,
        fileSize: Long?,
        fileKey: String,
        saveAsReport: Boolean
    ) {
        val localId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val fingerprint = java.util.UUID.randomUUID().toString()
        val now = java.time.Instant.now().toString()

        val messageType = if (fileType.startsWith("image/")) "image" else "file"

        val tempMessage = ChatMessageEntity(
            id = localId,
            clientTempId = fingerprint,
            roomId = roomId,
            senderId = myUserId,
            message = null,
            messageType = messageType,
            fileKey = fileKey,
            fileName = fileName,
            fileType = fileType,
            fileSize = fileSize,
            isRead = true,
            sentAt = now,
            updatedAt = now
        )

        chatMessageDao.insertMessage(tempMessage)

        ChatWebSocketManager.send(
            "chat:message",
            mapOf(
                "roomId" to roomId,
                "text" to null,
                "clientTempId" to fingerprint,
                "messageType" to messageType,
                "fileKey" to fileKey,
                "fileName" to fileName,
                "fileType" to fileType,
                "fileSize" to fileSize,
                "saveAsReport" to saveAsReport
            )
        )
    }
    suspend fun uploadChatFile(
        context: Context,
        uploadUrl: String,
        uri: Uri,
        mimeType: String
    ) = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Unable to read selected file")

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        val request = Request.Builder()
            .url(uploadUrl)
            .put(requestBody)
            .build()

        val response = OkHttpClient().newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException(
                "File upload failed: ${response.code} ${response.body?.string()}"
            )
        }

        response.close()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun uploadAndSendFileMessage(
        context: Context,
        token: String,
        roomId: Int,
        myUserId: Int,
        uri: Uri,
        fileName: String,
        fileType: String,
        fileSize: Long?,
        saveAsReport: Boolean
    ) {
        val uploadRes = api.getChatFileUploadUrl(
            token = "Bearer $token",
            request = ChatFileUploadUrlRequest(
                roomId = roomId,
                fileName = fileName,
                fileType = fileType
            )
        )

        if (!uploadRes.isSuccessful || uploadRes.body() == null) {
            throw IllegalStateException("Failed to get upload URL")
        }

        val uploadData = uploadRes.body()!!

        uploadChatFile(
            context = context,
            uploadUrl = uploadData.uploadUrl,
            uri = uri,
            mimeType = fileType
        )

        sendFileMessage(
            roomId = roomId,
            myUserId = myUserId,
            fileName = fileName,
            fileType = fileType,
            fileSize = fileSize,
            fileKey = uploadData.key,
            saveAsReport = saveAsReport
        )
    }


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
            "clientTempId" to fingerprint,
            "messageType" to "text"
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
                        messageType = msg.messageType,
                        fileKey = msg.fileKey,
                        fileName = msg.fileName,
                        fileType = msg.fileType,
                        fileSize = msg.fileSize,
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

        chatDao.updateInboxSnippet(
            roomId = message.roomId,
            message = message.message ?: message.fileName ?: "File",
            time = message.sentAt,
            incrementBy = increment
        )
    }

    suspend fun markMessageAsReadLocally(messageId: Int) {
        chatMessageDao.markSingleMessageAsRead(messageId)
    }
    suspend fun getChatFileViewUrl(
        token: String,
        key: String
    ): String {
        val response = api.getChatFileViewUrl(
            token = "Bearer $token",
            key = key
        )

        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Failed to open file")
        }

        return response.body()!!.viewUrl
    }
}