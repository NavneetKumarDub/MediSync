package com.example.medisync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatInboxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChats(chats: List<ChatInboxEntity>)

    @Query("SELECT * FROM chat_inbox_table ORDER BY last_message_time DESC")
    fun getAllChats(): Flow<List<ChatInboxEntity>>

    @Query("SELECT MAX(updated_at) FROM chat_inbox_table")
    suspend fun getLastSyncTimestamp(): String?

    @Query("DELETE FROM chat_inbox_table")
    suspend fun clearAllChats()
}