package com.example.medisync.networks


data class AppointmentDto(
    val appointmentId: Int,           
    val status: String,               
    val type: String,                 
    val slotDate: String,             
    val slotStartTime: String,        
    val consultationFee: Double,      
    val otherPartyName: String,       
    val otherPartyPhoto: String?      
)