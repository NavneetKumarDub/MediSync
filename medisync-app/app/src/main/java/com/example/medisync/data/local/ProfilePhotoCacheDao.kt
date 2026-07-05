package com.example.medisync.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfilePhotoCacheDao {
    @Query("SELECT * FROM profile_photo_cache WHERE user_id = :userId")
    fun observePhoto(userId: Int): Flow<ProfilePhotoCacheEntity?>

    @Query("SELECT * FROM profile_photo_cache WHERE user_id = :userId")
    suspend fun getPhoto(userId: Int): ProfilePhotoCacheEntity?

    @Upsert
    suspend fun upsertPhoto(photo: ProfilePhotoCacheEntity)

    @Query("DELETE FROM profile_photo_cache WHERE user_id = :userId")
    suspend fun deletePhoto(userId: Int)
}