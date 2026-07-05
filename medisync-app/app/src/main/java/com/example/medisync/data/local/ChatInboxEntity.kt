package com.example.medisync.data.local


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_inbox_table")
data class ChatInboxEntity(
    @PrimaryKey
    @ColumnInfo(name = "room_id")
    val roomId: Int,

    @ColumnInfo(name = "other_user_id")
    val otherUserId: Int,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "photo_url")
    val profilePhotoKey: String?,

    @ColumnInfo(name = "last_message")
    val lastMessage: String?,

    @ColumnInfo(name = "last_message_time")
    val lastMessageTime: String?,

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String?
)