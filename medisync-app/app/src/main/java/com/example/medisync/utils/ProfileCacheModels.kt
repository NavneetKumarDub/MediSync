package com.example.medisync.utils

data class PatientProfileCacheData(
    val name: String = "",
    val email: String = "",
    val gender: String = "",
    val dob: String = "",
    val bloodGroup: String = "",
    val maritalStatus: String = "",
    val height: Int = 0,
    val weight: Int = 0,
    val emergencyContact: String = "",
    val allergies: String = "",
    val currentMedications: String = "",
    val pastMedications: String = "",
    val chronicDiseases: String = "",
    val injuries: String = "",
    val surgeries: String = "",
    val smoking: String = "",
    val alcohol: String = "",
    val activityLevel: String = "",
    val foodPreference: String = "",
    val occupation: String = ""
)

data class DoctorProfileCacheData(
    val email: String = "",
    val gender: String = "",
    val dob: String = "",
    val maritalStatus: String = "",
    val about: String = "",
    val licenseNumber: String = "",
    val speciality: String = "",
    val subSpeciality: String = "",
    val qualification: String = "",
    val experienceYears: String = "",
    val languages: String = "",
    val consultationFee: String = "",
    val consultationType: String = "",
    val clinicName: String = ""
)
