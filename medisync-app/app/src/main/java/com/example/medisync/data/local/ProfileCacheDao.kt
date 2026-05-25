package com.example.medisync.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ProfileCacheDao {
    @Query("SELECT * FROM profile_cache_table WHERE user_id = :userId AND role = :role LIMIT 1")
    suspend fun getProfile(userId: Int, role: String): ProfileCacheEntity?

    @Upsert
    suspend fun upsertProfile(profile: ProfileCacheEntity)

    @Query("UPDATE profile_cache_table SET photo_uri = NULL, updated_at = :updatedAt WHERE user_id = :userId AND role = :role")
    suspend fun clearPhoto(userId: Int, role: String, updatedAt: Long)
}
