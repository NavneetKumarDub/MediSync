package com.example.medisync.data.repository

import android.util.Log
import com.example.medisync.data.local.ChatInboxDao
import com.example.medisync.data.local.ChatInboxEntity
import com.example.medisync.networks.ApiService
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow

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

class ChatInboxRepository(
    private val chatDao: ChatInboxDao,
    private val api: ApiService
) {


    val allChats: Flow<List<ChatInboxEntity>> = chatDao.getAllChats()


    suspend fun syncChats(token:String) {
        try {
            val lastSync = chatDao.getLastSyncTimestamp() ?: "1970-01-01T00:00:00Z"

            val response = api.getInbox(
                token = "Bearer $token",
                lastSync
            )

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
}