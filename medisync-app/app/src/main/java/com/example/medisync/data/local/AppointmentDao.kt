package com.example.medisync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT MAX(updated_at) FROM appointments_table")
    suspend fun getLatestUpdatedAt(): String?

    @Query("SELECT * FROM appointments_table ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    // Delete: Clears the cache on logout
    @Query("DELETE FROM appointments_table")
    suspend fun clearAll()
}