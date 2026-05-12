package com.example.medisync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments_table")
data class AppointmentEntity(
    @PrimaryKey val id: Int,
    val status: String,
    val type: String,
    val date: String,
    val time: String,
    val displayName: String,
    val subtitle: String, // Speciality for doctors, or "Patient"
    val photoUrl: String?
)