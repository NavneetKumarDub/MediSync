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
import com.example.medisync.utils.FileCacheManager
import com.example.medisync.viewmodels.ChatSession
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.util.concurrent.CancellationException



data class MessageDto(
    @SerializedName("id")
    val id: String,

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
    val sentAt: String,

    @SerializedName("syncVersion")
    val syncVersion: Long? = null
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
        val messageId = java.util.UUID.randomUUID().toString()
        val now = java.time.Instant.now().toString()

        val messageType = if (fileType.startsWith("image/")) "image" else "file"

        val tempMessage = ChatMessageEntity(
            id = messageId,
            roomId = roomId,
            senderId = myUserId,
            message = null,
            messageType = messageType,
            fileKey = fileKey,
            fileName = fileName,
            fileType = fileType,
            fileSize = fileSize,
            status = "PENDING",
            sentAt = now,
            updatedAt = now
        )

        chatMessageDao.insertMessage(tempMessage)

        ChatWebSocketManager.send(
            "chat:message",
            mapOf(
                "roomId" to roomId,
                "text" to null,
                "messageId" to messageId,
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
            val lastSync =
                chatDao.getLastSyncTimestamp()
                    ?: "1970-01-01T00:00:00Z"

            val response = api.getInbox(
                token = "Bearer $token",
                since = lastSync
            )

            if (!response.isSuccessful) {
                Log.e(
                    "ChatSync",
                    "Inbox request failed: ${response.code()}"
                )
                return
            }

            val networkChats = response.body()?.chats.orEmpty()

            if (networkChats.isEmpty()) {
                return
            }

            val entities = networkChats.map { dto ->
                ChatInboxEntity(
                    roomId = dto.roomId,
                    otherUserId = dto.userId,
                    displayName = dto.name,
                    profilePhotoKey = dto.profilePhoto,
                    lastMessage = dto.lastMessage,
                    lastMessageTime = dto.lastMessageTime,
                    unreadCount = dto.unreadCount ?: 0,
                    updatedAt = dto.updatedAt
                )
            }

            chatDao.upsertChats(entities)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(
                "ChatSync",
                "Network unavailable; using cached inbox: ${error.message}"
            )
        }
    }

    fun getMessagesForRoom(roomId: Int): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesForRoom(roomId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendMessage(roomId: Int, myUserId: Int, text: String) {
        val messageId = java.util.UUID.randomUUID().toString() // Universal ID

        var now = java.time.Instant.now()
        val lastDbTimeStr = chatMessageDao.getLastSentAt(roomId)
        if(lastDbTimeStr != null){
            val lastDbTime = java.time.Instant.parse(lastDbTimeStr)
            if(now.isBefore(lastDbTime)){
                now = lastDbTime.plusMillis(1)
            }
        }
        val finalSenAt = now.toString()
        val tempMessage = ChatMessageEntity(
            id = messageId,
            roomId = roomId,
            senderId = myUserId,
            message = text,
            status = "PENDING",
            sentAt = finalSenAt,
            updatedAt = finalSenAt
        )
        chatMessageDao.insertMessage(tempMessage)

        ChatWebSocketManager.send("chat:message", mapOf(
            "roomId" to roomId,
            "text" to text,
            "messageId" to messageId,
            "messageType" to "text"
        ))
    }
    suspend fun syncMissingMessages(roomId: Int , token: String) {
        try {
            val lastSyncVersion = chatMessageDao.getLastSyncVersion(roomId) ?: 0L

            val response = api.getRoomMessages("Bearer $token",roomId, lastSyncVersion.toString())

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
                        status = if(msg.isRead) "READ" else "DELIVERED",
                        sentAt = msg.sentAt,
                        syncVersion = msg.syncVersion ?: 0L
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
    suspend fun updateMessageStatusById(messageId: String, newStatus: String): Int {
        return chatMessageDao.updateMessageStatusById(messageId, newStatus)
    }
    suspend fun upsertMessage(message: ChatMessageEntity) {
        chatMessageDao.insertMessage(message)
    }
    suspend fun insertOutgoingMessage(message: ChatMessageEntity) {
        chatMessageDao.insertOrIgnoreMessage(message)
        
        chatDao.updateInboxSnippet(
            roomId = message.roomId,
            message = message.message ?: message.fileName ?: "File",
            time = message.sentAt,
            incrementBy = 0 // Never add an unread badge for your own messages!
        )
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

    suspend fun markMessageAsReadLocally(messageId: String) {
        chatMessageDao.markSingleMessageAsRead(messageId)
    }
//    suspend fun resendAllPendingMessages(){
//        val pendingMessages = chatMessageDao.getAllPendingMessages()
//        val messageByRoom = pendingMessages.groupBy { it.roomId }
//        for((roomId,messages) in messageByRoom){
//            ChatWebSocketManager.send("chat:join",mapOf("roomId" to roomId))
//            kotlinx.coroutines.delay(500)
//
//            for (msg in messages) {
//                if (msg.messageType == "text" && msg.message != null) {
//                    ChatWebSocketManager.send("chat:message", mapOf(
//                        "roomId" to roomId,
//                        "text" to msg.message,
//                        "messageId" to msg.id,
//                        "messageType" to "text"
//                    ))
//                }
//            }
//        }
//
//    }
        suspend fun resendAllPendingMessages(){
            val pendingMessages = chatMessageDao.getAllPendingMessages()
            val messageByRoom = pendingMessages.groupBy { it.roomId }
            for((roomId,messages) in messageByRoom){
                ChatWebSocketManager.send("chat:join",mapOf("roomId" to roomId))
                kotlinx.coroutines.delay(500)

                for (msg in messages) {
                    // Check if it's a text OR if it has a fileKey (for images/files)
                    if (msg.message != null || msg.fileKey != null) {
                        ChatWebSocketManager.send("chat:message", mapOf(
                            "roomId" to roomId,
                            "text" to msg.message,
                            "messageId" to msg.id,
                            "messageType" to msg.messageType,
                            "fileKey" to msg.fileKey,
                            "fileName" to msg.fileName,
                            "fileType" to msg.fileType,
                            "fileSize" to msg.fileSize,
                            // saveAsReport isn't stored in entity, but we can pass false for retries or add it to db if needed
                            "saveAsReport" to false
                        ))
                    }
                }
            }
        }
}
