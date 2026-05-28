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

    
    @Query("DELETE FROM appointments_table")
    suspend fun clearAll()

    @Query("SELECT * FROM appointments_table WHERE appointment_id = :appointmentId LIMIT 1")
    fun getAppointmentById(appointmentId: Int): Flow<AppointmentEntity?>
}