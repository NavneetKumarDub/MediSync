package com.example.medisync.networks

import retrofit2.Response
import retrofit2.http.Body
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
        @Path("userId") userId: Int,
        @Body request: PersonalProfileRequest
    ): ProfileResponse

    @PUT("api/patient/medical/{userId}")
    suspend fun updateMedicalProfile(
        @Path("userId")userId:Int,
        @Body request: MedicalProfileRequest
    ): ProfileResponse


    @PUT("api/patient/lifestyle/{userId}")
    suspend fun updateLifestyleProfile(
        @Path("userId")userId:Int,
        @Body request: LifestyleProfileRequest
    ): ProfileResponse

    @PUT("api/doctor/personal/{userId}")
    suspend fun updateDoctorPersonal(
        @Path("userId") userId: Int,
        @Body request: DoctorPersonalRequest
    ): ProfileResponse

    @PUT("api/doctor/professional/{userId}")
    suspend fun updateDoctorProfessional(
        @Path("userId") userId: Int,
        @Body request: DoctorProfessionalRequest
    ): ProfileResponse

    @PUT("api/doctor/clinic/{userId}")
    suspend fun updateDoctorClinic(
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
        @Query("q")                    query           : String  = "",
        @Query("consultation_type")    consultationType: String? = null,
        @Query("min_experience")       minExperience   : Int?    = null,
        @Query("min_fee")              minFee          : Int?    = null,
        @Query("max_fee")              maxFee          : Int?    = null,
        @Query("languages")            languages       : String? = null
    ): SearchResponse

    @GET("api/doctor/profile/{doctorId}")
    suspend fun getDoctorProfile(
        @Path("doctorId") doctorId: Int
    ): DoctorProfileResponse

    @GET("api/slots/{doctorId}/dates")
    suspend fun getDoctorAvailableDates(
        @Path("doctorId") doctorId: Int
    ): AvailableDatesResponse

    @GET("api/slots/{doctorId}/slots")
    suspend fun getDoctorSlots(
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
        @Header("Authorization") token: String
    ): Response<InboxResponse>

    @GET("api/chat/room/{roomId}/messages")
    suspend fun getRoomMessages(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: Int
    ): Response<MessageHistoryResponse>


}

