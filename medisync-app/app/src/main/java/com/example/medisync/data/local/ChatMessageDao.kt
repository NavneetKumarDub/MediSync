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
    suspend fun markSingleMessageAsRead(messageId: String)



    @Query("UPDATE chat_messages_table SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatusById(messageId: String, newStatus: String): Int // Add ': Int' here
    @Query("SELECT MAX(sentAt) FROM chat_messages_table WHERE roomId = :roomId and status != 'PENDING'")
    suspend fun getLastSyncTimestamp(roomId: Int): String?

    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreMessage(message: ChatMessageEntity)


    @Query("SELECT * FROM chat_messages_table WHERE status = 'PENDING'")
    suspend fun getAllPendingMessages(): List<ChatMessageEntity>




}

