package com.example.medisync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppointmentEntity::class,ChatInboxEntity::class], version = 3, exportSchema = false)
abstract class MediSyncDatabase : RoomDatabase() {

    abstract fun appointmentDao(): AppointmentDao
    abstract fun chatInboxDao(): ChatInboxDao

    companion object {
        @Volatile
        private var INSTANCE: MediSyncDatabase? = null

        fun getDatabase(context: Context): MediSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediSyncDatabase::class.java,
                    "medisync_offline_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}