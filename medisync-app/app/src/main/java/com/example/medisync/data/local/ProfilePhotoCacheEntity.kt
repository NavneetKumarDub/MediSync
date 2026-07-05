package com.example.medisync.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_photo_cache")
data class ProfilePhotoCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "photo_key")
    val photoKey: String,

    @ColumnInfo(name = "local_path")
    val localPath: String,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long
)