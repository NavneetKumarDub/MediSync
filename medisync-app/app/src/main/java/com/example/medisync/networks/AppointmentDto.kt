package com.example.medisync.networks

// DTO stands for Data Transfer Object. This is exactly what comes from the internet.
data class AppointmentDto(
    val appointmentId: Int,           // From appointments.id
    val status: String,               // From appointments.status
    val type: String,                 // From appointments.type
    val slotDate: String,             // From appointment_slots.date
    val slotStartTime: String,        // From appointment_slots.start_time
    val consultationFee: Double,      // From appointment_slots.consultation_fee
    val otherPartyName: String,       // From users.name (Doctor or Patient name)
    val otherPartyPhoto: String?      // From users.profile_photo_key
)