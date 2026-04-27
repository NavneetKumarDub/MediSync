package com.example.medisync.networks

import com.google.gson.annotations.SerializedName


data class ProfileModels(
    val phone: String,
    val name: String,
    val role: String,
)

data class PersonalProfileRequest(
    val name:String,
    val email: String,
    val gender:String,
    val dob:String,
    val blood_group:String,
    val marital_status:String,
    val height:String,
    val weight:String,
    val emergency_contact:String
)
data class MedicalProfileRequest(
    val allergies:String,
    val current_medications:String,
    val past_medications:String,
    val chronic_diseases:String,
    val injuries:String,
    val surgeries:String
)

data class LifestyleProfileRequest(
    val smoking:String,
    val alcohol:String,
    val activity_level:String,
    val food_preference:String,
    val occupation:String
)

data class DoctorPersonalRequest(
    val email: String,
    val gender: String,
    val dob: String,
    val marital_status: String,
    val about: String
)

data class DoctorProfessionalRequest(
    val license_number: String,
    val speciality: String,
    val sub_speciality: String,
    val qualification: String,
    val experience_years: Int,
    val languages: String,
    val consultation_fee: String,
    val consultation_type: String
)

data class DoctorClinicRequest(
    val clinic_name: String,
    val address: String,
    val city: String,
    val pincode: String
)

data class DoctorAvailabilityRequest(
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val slot_duration_minutes: Int
)
// Add these at the bottom of ProfileModels.kt
data class DoctorSearchResult(
    @SerializedName("doctor_id")         val doctorId        : Int    = 0,
    @SerializedName("doctor_name")       val doctorName      : String = "",
    @SerializedName("speciality")        val speciality      : String? = null,
    @SerializedName("experience_years")  val experienceYears : Int?    = null,
    @SerializedName("consultation_fee")  val consultationFee : Double? = null,
    @SerializedName("consultation_type") val consultationType: String? = null,
    @SerializedName("languages")         val languages       : String? = null,
    @SerializedName("city")              val city            : String? = null,
    @SerializedName("about")             val about           : String? = null
)

data class SearchFilters(
    val consultationType: String? = null,
    val minExperience   : Int?    = null,
    val minFee          : Int?    = null,
    val maxFee          : Int?    = null,
    val languages       : String? = null
)

data class SearchResponse(
    val doctors: List<DoctorSearchResult>
)

data class DoctorProfileResponse(
    val doctor: DoctorDetail
)

data class DoctorDetail(
    @SerializedName("doctor_id")         val doctorId        : Int     = 0,
    @SerializedName("doctor_name")       val doctorName      : String  = "",
    @SerializedName("speciality")        val speciality      : String? = null,
    @SerializedName("sub_speciality")    val subSpeciality   : String? = null,
    @SerializedName("qualification")     val qualification   : String? = null,
    @SerializedName("experience_years")  val experienceYears : Int?    = null,
    @SerializedName("consultation_fee")  val consultationFee : Double? = null,
    @SerializedName("consultation_type") val consultationType: String? = null,
    @SerializedName("languages")         val languages       : String? = null,
    @SerializedName("about")             val about           : String? = null,
    @SerializedName("gender")            val gender          : String? = null,
    @SerializedName("profile_photo")     val profilePhoto    : String? = null,
    @SerializedName("clinic_name")       val clinicName      : String? = null,
    @SerializedName("address")           val address         : String? = null,
    @SerializedName("city")              val city            : String? = null,
    @SerializedName("pincode")           val pincode         : String? = null
)


data class SlotItem(
    @SerializedName("id")               val id             : Int,
    @SerializedName("start_time")       val startTime      : String,
    @SerializedName("end_time")         val endTime        : String,
    @SerializedName("consultation_fee") val consultationFee: String,
    @SerializedName("status")           val status         : String
)

data class SlotsResponse(
    val slots: List<SlotItem>
)

data class AvailableDatesResponse(
    val dates: List<String>
)
data class AvailabilitySlot(
    @SerializedName("day_of_week")           val dayOfWeek          : String = "",
    @SerializedName("start_time")            val startTime          : String = "",
    @SerializedName("end_time")              val endTime            : String = "",
    @SerializedName("slot_duration_minutes") val slotDurationMinutes: Int    = 15
)

data class AvailabilityResponse(
    val availability: List<AvailabilitySlot>
)

// ── Request ──────────────────────────────────────────────
data class BookAppointmentRequest(
    @SerializedName("slotId") val slotId: Int
)

// ── Response from POST /api/appointments/book ────────────
data class BookAppointmentResponse(
    val appointment: AppointmentDto,
    val doctor: DoctorSnapshot,
    @SerializedName("roomId") val roomId: Int? = null
)

data class AppointmentDto(
    val id: Int,
    @SerializedName("scheduledAt") val scheduledAt: String,
    val status: String,
    val type: String,
    val fee: Double
)

data class DoctorSnapshot(
    val id: Int,
    val name: String,
    @SerializedName("profile_photo") val profilePhoto: String? = null,
    val speciality: String? = null
)

data class PatientSnapshot(
    val id: Int,
    val name: String,
    @SerializedName("profile_photo") val profilePhoto: String? = null
)

// ── WebSocket push payload (doctor side) ─────────────────
// Matches: sendToUser(doctorId, 'appointment:new', { appointment, patient, roomId })
data class IncomingAppointment(
    val appointment: AppointmentDto,
    val patient: PatientSnapshot,
    @SerializedName("roomId") val roomId: Int? = null
)
