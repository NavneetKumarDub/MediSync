package com.example.medisync.networks

import com.example.medisync.data.repository.MessageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/user/register")
    suspend fun registerUser(@Body request: ProfileModels): RegisterResponse

    @PUT("api/patient/personal/{userId}")
    suspend fun updatePersonalProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: PersonalProfileRequest
    ): ProfileResponse

    @PUT("api/patient/medical/{userId}")
    suspend fun updateMedicalProfile(
        @Header("Authorization") token: String,
        @Path("userId")userId:Int,
        @Body request: MedicalProfileRequest
    ): ProfileResponse


    @PUT("api/patient/lifestyle/{userId}")
    suspend fun updateLifestyleProfile(
        @Header("Authorization") token: String,
        @Path("userId")userId:Int,
        @Body request: LifestyleProfileRequest
    ): ProfileResponse

    @PUT("api/doctor/personal/{userId}")
    suspend fun updateDoctorPersonal(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: DoctorPersonalRequest
    ): ProfileResponse

    @PUT("api/doctor/professional/{userId}")
    suspend fun updateDoctorProfessional(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: DoctorProfessionalRequest
    ): ProfileResponse

    @PUT("api/doctor/clinic/{userId}")
    suspend fun updateDoctorClinic(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: DoctorClinicRequest
    ): ProfileResponse

    @PUT("api/doctor/availability/{userId}")
    suspend fun updateDoctorAvailability(
        @Path("userId") userId: Int,
        @Body request: DoctorAvailabilityRequest
    ): ProfileResponse

    @GET("api/doctor/search")
    suspend fun searchDoctors(
        @Header("Authorization") token: String,
        @Query("q")                    query           : String  = "",
        @Query("consultation_type")    consultationType: String? = null,
        @Query("min_experience")       minExperience   : Int?    = null,
        @Query("min_fee")              minFee          : Int?    = null,
        @Query("max_fee")              maxFee          : Int?    = null,
        @Query("languages")            languages       : String? = null
    ): SearchResponse



    @GET("api/slots/{doctorId}/dates")
    suspend fun getDoctorAvailableDates(
        @Header("Authorization") token: String,
        @Path("doctorId") doctorId: Int
    ): AvailableDatesResponse

    @GET("api/slots/{doctorId}/slots")
    suspend fun getDoctorSlots(
        @Header("Authorization") token: String,
        @Path("doctorId") doctorId : Int,
        @Query("date")    date     : String
    ): SlotsResponse

    @GET("api/slots/{doctorId}/availability")
    suspend fun getDoctorAvailability(
        @Path("doctorId") doctorId: Int
    ): AvailabilityResponse

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request:VerifyOtpRequest
    ):VerifyOtpResponse

    // ApiService.kt — add
    @POST("api/appointments/book")
    suspend fun bookAppointment(
        @Header("Authorization") token: String,
        @Body body: BookAppointmentRequest
    ): Response<BookAppointmentResponse>

    @GET("api/appointments/patient")
    suspend fun getPatientAppointments(
        @Header("Authorization") token: String,
    ): Response<AppointmentsResponse>

    @GET("api/appointments/doctor")
    suspend fun getDoctorAppointments(
        @Header("Authorization") token: String,
    ): Response<AppointmentsResponse>

        @GET("api/chat/{roomId}/metadata")
        suspend fun getRoomMetadata(
            @Header("Authorization") token: String,
            @Path("roomId") roomId: Int
        ): Response<RoomMetadataResponse>

    @POST("api/chat/room")
    suspend fun getOrCreateChatRoom(
        @Header("Authorization") token: String,
        @Body request: GetOrCreateRoomRequest
    ): Response<GetOrCreateRoomResponse>

    @GET("api/chat/inbox")
    suspend fun getInbox(
        @Header("Authorization") token: String,
        @Query("since") since: String
    ): Response<InboxResponse>

    @GET("api/chat/room/{roomId}/messages")
    suspend fun getRoomMessages(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: Int,
        @Query("since") lastTimestamp: String? = null
    ): Response<List<MessageDto>>

    // Get slots for a day
    @GET("api/slots/regular")
    suspend fun getRegularSlots(
        @Header("Authorization") token: String,
        @Query("day") day: String
    ): RegularSlotsResponse

    // Add slot
    @POST("api/slots/regular")
    suspend fun addRegularSlot(
        @Header("Authorization") token: String,
        @Body request: AddSlotRequest
    ): AddSlotResponse

    // Delete slot
    @DELETE("api/slots/regular/{slotId}")
    suspend fun deleteRegularSlot(
        @Header("Authorization") token: String,
        @Path("slotId") slotId: Int
    ): DeleteSlotResponse

    @GET("api/patient/personal/{userId}")
    suspend fun getPersonalProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): PersonalProfileResponse

    @GET("api/patient/medical/{userId}")
    suspend fun getMedicalProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): MedicalProfileResponse

    @GET("api/patient/lifestyle/{userId}")
    suspend fun getLifestyleProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): LifestyleProfileResponse

    @GET("api/doctor/profile/{doctorId}")
    suspend fun getDoctorProfile(
        @Header("Authorization") token: String,
        @Path("doctorId") doctorId: Int
    ): Response<DoctorProfileResponse>


    @POST("api/slots/custom")
    suspend fun createCustomSlot(
        @Header("Authorization") token: String,
        @Body request: CreateCustomSlotRequest
    ): CreateCustomSlotResponse

    @DELETE("api/slots/custom/{slotId}")
    suspend fun deleteCustomSlot(
        @Header("Authorization") token: String,
        @Path("slotId") slotId: Int
    ): DeleteCustomSlotResponse

    @GET("api/slots/custom")
    suspend fun getSlotsByDate(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): GetSlotsByDateResponse

    @GET("api/slots/custom/dates")
    suspend fun getDatesWithSlots(
        @Header("Authorization") token: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): GetDatesWithSlotsResponse

    @POST("api/upload/presigned-url")
    suspend fun getPresignedUploadUrl(
        @Header("Authorization") token: String,
        @Body request: PresignedUrlRequest
    ): PresignedUrlResponse

    @POST("api/upload/confirm")
    suspend fun confirmProfilePhotoUpload(
        @Header("Authorization") token: String,
        @Body request: ConfirmUploadRequest
    ): ProfileResponse

    @GET("api/upload/profile/{userId}")
    suspend fun getProfilePhotoUrl(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): ProfilePhotoResponse

    @DELETE("api/upload/profile/{userId}")
    suspend fun deleteProfilePhoto(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): ProfileResponse


}


