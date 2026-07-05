package com.example.medisync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AppointmentEntity::class,
        ChatInboxEntity::class,
        ChatMessageEntity::class,
        ProfileCacheEntity::class,
        ProfilePhotoCacheEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class MediSyncDatabase : RoomDatabase() {

    abstract fun appointmentDao(): AppointmentDao
    abstract fun chatInboxDao(): ChatInboxDao

    abstract fun chatMessageDao(): ChatMessageDao

    abstract fun profileCacheDao(): ProfileCacheDao

    abstract fun profilePhotoCacheDao(): ProfilePhotoCacheDao

    companion object {
        @Volatile
        private var INSTANCE: MediSyncDatabase? = null

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS profile_photo_cache (
                user_id INTEGER NOT NULL PRIMARY KEY,
                photo_key TEXT NOT NULL,
                local_path TEXT NOT NULL,
                cached_at INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }
        fun getDatabase(context: Context): MediSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediSyncDatabase::class.java,
                    "medisync_offline_database"
                )
                    .addMigrations(MIGRATION_9_10)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
