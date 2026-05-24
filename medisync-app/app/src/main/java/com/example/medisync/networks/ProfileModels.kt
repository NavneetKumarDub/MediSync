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
    @SerializedName("license_number")    val licenseNumber   : String? = null,
    @SerializedName("about")             val about           : String? = null,
    @SerializedName("gender")            val gender          : String? = null,
    @SerializedName("email")             val email           : String? = null,
    @SerializedName("dob")               val dob             : String? = null,
    @SerializedName("marital_status")    val maritalStatus   : String? = null,
    @SerializedName("profile_photo")     val profilePhoto    : String? = null,
    @SerializedName("clinic_name")       val clinicName      : String? = null,
    @SerializedName("address")           val address         : String? = null,
    @SerializedName("city")              val city            : String? = null,
    @SerializedName("pincode")           val pincode         : String? = null,
    @SerializedName("lat")               val lat             : Double? = null,
    @SerializedName("lng")               val lng             : Double? = null
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

data class BookAppointmentResponse(
    val appointment: ApptDetails?, // Make these nullable just in case
    val doctor: DocDetails?,
    @SerializedName("roomId") val roomId: Int? = null
)

data class ApptDetails(
    val id: Int,
    val status: String?,
    val type: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("start_time") val startTime: String?,
    val date: String?,

    // CRITICAL FIX: Postgres numeric/decimal types often return as Strings.
    // Change this to String? to prevent crashes. You can convert it to Double later if needed.
    val fee: String?
)

data class DocDetails(
    val id: Int,
    val name: String?,
    @SerializedName("profile_photo") val profilePhoto: String?,
    val speciality: String?
)

data class PatientSnapshot(
    val id: Int,
    val name: String,
    @SerializedName("profile_photo") val profilePhoto: String? = null
)

// ── WebSocket push payload (doctor side) ─────────────────
// Matches: sendToUser(doctorId, 'appointment:new', { appointment, patient, roomId })
data class IncomingAppointment(
    val appointment: ApptDetails,
    val patient: PatientSnapshot,
    @SerializedName("roomId") val roomId: Int? = null
)

// ── Response from GET /api/appointments/patient ────────────
data class AppointmentsResponse(
    val appointments: List<AppointmentItem>
)


data class AppointmentItem(
    @SerializedName("appointment_id", alternate = ["id"])
    val appointmentId: Int,

    @SerializedName("profile_photo")
    val profilePhoto: String? = null,

    @SerializedName("doctor_id")
    val doctorId: Int? = null,

    @SerializedName("patient_id")
    val patientId: Int? = null,

    @SerializedName("display_name", alternate = ["doctor_name", "patient_name", "name"])
    val displayName: String? = null,

    @SerializedName("speciality")
    val speciality: String? = null,

    @SerializedName("slot_date", alternate = ["date"])
    val date: String?,

    @SerializedName("start_time")
    val startTime: String?,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("room_id")
    val roomId: Int? = null,

    @SerializedName("consultation_fee", alternate = ["fee"])
    val fee: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class RoomMetadataResponse(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("other_role") val otherRole: String
)

data class GetOrCreateRoomResponse(
    @SerializedName("roomId") val roomId: Int,
    @SerializedName("isNew") val isNew: Boolean? // Nullable just to be safe
)
data class GetOrCreateRoomRequest(
    @SerializedName("targetUserId") val targetUserId: Int
)

data class InboxResponse(
    @SerializedName("chats") val chats: List<InboxChat>
)

data class InboxChat(
    @SerializedName("room_id") val roomId: Int,

    @SerializedName("other_user_id") val userId: Int,

    @SerializedName("display_name") val name: String,

    @SerializedName("profile_photo") val profilePhoto: String?,

    @SerializedName("speciality") val speciality: String?,

    @SerializedName("last_message") val lastMessage: String?,

    @SerializedName("last_message_time") val lastMessageTime: String?,

    @SerializedName("unread_count") val unreadCount: Int?,

    @SerializedName("updated_at") val updatedAt: String?
)

data class MessageHistoryResponse(
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val id: Int,
    @SerializedName("senderId") val senderId: Int,
    val text: String,
    @SerializedName("createdAt") val createdAt: String
)

//video call things
data class VideoSignal(
    val type: String,
    val roomId: Int,
    val id: String? = null,
    val targetId: String? = null,
    val sdp: SdpData? = null,
    val candidate: CandidateData? = null
)

data class SdpData(
    val type: String,
    val sdp: String
)

data class CandidateData(
    val candidate: String,
    @SerializedName("sdpMLineIndex") val sdpMLineIndex: Int,
    @SerializedName("sdpMid") val sdpMid: String
)

interface VideoSignalingListener {
    fun onRoomJoined(socketId: String)
    fun onUserJoined(userId: String)
    fun onUserLeft(userId: String)
    fun onOfferReceived(signal: VideoSignal)
    fun onAnswerReceived(signal: VideoSignal)
    fun onIceCandidateReceived(signal: VideoSignal)
    fun onConnectionClosed()
}

data class AddSlotRequest(
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val slot_duration_minutes: Int,
    val consultation_fee: Int,
    val consultation_type: String
)

data class RegularSlotItem(
    val id: Int,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val slot_duration_minutes: Int,
    val consultation_fee: String,
    val consultation_type: String
)

data class RegularSlotsResponse(
    val success: Boolean,
    val slots: List<RegularSlotItem>
)

data class AddSlotResponse(
    val success: Boolean,
    val message: String,
    val slot: RegularSlotItem?
)

data class DeleteSlotResponse(
    val success: Boolean,
    val message: String
)

data class PersonalProfileResponse(
    val data: PersonalProfileData?
)

data class PersonalProfileData(
    val user_id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val blood_group: String? = null,
    val marital_status: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val emergency_contact: String? = null
)

data class MedicalProfileResponse(
    val data: MedicalProfileData?
)

data class MedicalProfileData(
    val allergies: String? = null,
    val current_medications: String? = null,
    val past_medications: String? = null,
    val chronic_diseases: String? = null,
    val injuries: String? = null,
    val surgeries: String? = null
)

data class LifestyleProfileResponse(
    val data: LifestyleProfileData?
)

data class LifestyleProfileData(
    val smoking: String? = null,
    val alcohol: String? = null,
    val activity_level: String? = null,
    val food_preference: String? = null,
    val occupation: String? = null
)

data class CreateCustomSlotRequest(
    val date: String,
    val start_time: String,
    val end_time: String,
    val consultation_fee: Int,
    val consultation_type: String,
    val slot_duration_minutes: Int
)

data class CustomSlotItem(
    val id: Int,
    val date: String,
    val start_time: String,
    val end_time: String,
    val consultation_fee: String,
    val consultation_type: String,
    val slot_duration_minutes: Int,
    val status: String
)

data class CreateCustomSlotResponse(
    val success: Boolean,
    val message: String,
    val slot: CustomSlotItem?
)

data class DeleteCustomSlotResponse(
    val success: Boolean,
    val message: String
)

data class GetSlotsByDateResponse(
    val success: Boolean,
    val slots: List<CustomSlotItem>
)

data class GetDatesWithSlotsResponse(
    val success: Boolean,
    val dates: List<String>
)

data class ProfilePhotoResponse(
    val viewUrl: String
)

data class PresignedUrlRequest(
    val userId: Int,
    val fileName: String,
    val fileType: String
)

data class PresignedUrlResponse(
    val uploadUrl: String,
    val key: String
)

data class ConfirmUploadRequest(
    val userId: Int,
    val key: String
)
data class SaveFcmTokenRequest(
    val token: String,
    val platform: String = "android"
)
data class ChatFileUploadUrlRequest(
    val roomId: Int,
    val fileName: String,
    val fileType: String
)

data class ChatFileUploadUrlResponse(
    val uploadUrl: String,
    val key: String
)
data class ChatFileViewUrlResponse(
    val viewUrl: String
)

data class PatientRecordsResponse(
    val records: List<PatientRecordDto>
)

data class PatientRecordDto(
    val id: Int,
    val fileKey: String,
    val fileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val uploadedByName: String,
    val createdAt: String
)