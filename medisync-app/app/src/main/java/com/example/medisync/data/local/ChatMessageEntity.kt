package com.example.medisync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages_table")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val roomId: Int,
    val senderId: Int,
    val message: String?,
    val messageType: String = "text",
    val fileKey: String? = null,
    val fileName: String? = null,
    val fileType: String? = null,
    val fileSize: Long? = null,
    val sentAt: String,
    val updatedAt: String? = null,
    val status:String = "PENDING"
)