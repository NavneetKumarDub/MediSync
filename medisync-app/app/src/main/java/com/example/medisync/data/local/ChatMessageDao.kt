package com.example.medisync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages_table WHERE roomId = :roomId ORDER BY sentAt ASC")
    fun getMessagesForRoom(roomId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<ChatMessageEntity>)

    // For when you open the room
    @Query("UPDATE chat_messages_table SET isRead = 1 WHERE roomId = :roomId AND senderId != :myUserId")
    suspend fun markAllAsReadLocally(roomId: Int, myUserId: Int)

    // NEW: For live WebSocket read receipts
    @Query("UPDATE chat_messages_table SET isRead = 1 WHERE id = :messageId")
    suspend fun markSingleMessageAsRead(messageId: Int)

    @Query("SELECT MAX(sentAt) FROM chat_messages_table WHERE roomId = :roomId")
    suspend fun getLastSyncTimestamp(roomId: Int): String?

    // Add this to your DAO
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages_table SET id = :serverId WHERE clientTempId = :tempId")
    suspend fun updateMessageId(tempId: String, serverId: Int)

    // In ChatMessageDao.kt
    @Query("UPDATE chat_messages_table SET id = :serverId, sentAt = :serverSentAt WHERE clientTempId = :tempId")
    suspend fun reconcileMessageId(tempId: String, serverId: Int, serverSentAt: String)}