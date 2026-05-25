package com.example.medisync.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "profile_cache_table",
    primaryKeys = ["user_id", "role"]
)
data class ProfileCacheEntity(
    @ColumnInfo(name = "user_id")
    val userId: Int,

    val role: String,

    @ColumnInfo(name = "data_json")
    val dataJson: String,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String?,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
