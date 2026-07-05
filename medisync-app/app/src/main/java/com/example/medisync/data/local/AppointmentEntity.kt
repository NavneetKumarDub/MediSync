package com.example.medisync.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments_table")
data class AppointmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "appointment_id")
    val id: Int,

    val status: String,
    val type: String,
    val date: String,
    val time: String,
    val displayName: String,
    val subtitle: String,

    @ColumnInfo(name = "photoUrl")
    val profilePhotoKey: String?,

    @ColumnInfo(name = "room_id")
    val roomId: Int?,

    @ColumnInfo(name = "doctor_id")
    val doctorId: Int?,

    @ColumnInfo(name = "patient_id")
    val patientId: Int?,

    @ColumnInfo(name = "consultation_fee")
    val consultationFee: Double?,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String?
)