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

    
    @Query("UPDATE chat_messages_table SET status = 'READ' WHERE roomId = :roomId AND senderId != :myUserId")
    suspend fun markAllAsReadLocally(roomId: Int, myUserId: Int)

    
    @Query("UPDATE chat_messages_table SET status = 'READ' WHERE id = :messageId")
    suspend fun markSingleMessageAsRead(messageId: Int)

    @Query("UPDATE chat_messages_table set status = :newStatus where clientTempId = :tempId")
    suspend fun updateMessageStatusByTempId(tempId: String,newStatus:String)

    @Query("UPDATE chat_messages_table SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatusById(messageId: Int, newStatus: String)
    @Query("SELECT MAX(sentAt) FROM chat_messages_table WHERE roomId = :roomId")
    suspend fun getLastSyncTimestamp(roomId: Int): String?

    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages_table SET id = :serverId WHERE clientTempId = :tempId")
    suspend fun updateMessageId(tempId: String, serverId: Int)

    
    @Query("UPDATE chat_messages_table SET id = :serverId, sentAt = :serverSentAt WHERE clientTempId = :tempId")
    suspend fun reconcileMessageId(tempId: String, serverId: Int, serverSentAt: String)}