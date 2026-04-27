package com.example.medisync.networks

data class VerifyOtpRequest(
    val idToken: String
)

data class VerifyOtpResponse(
    val token: String,
    val user: AuthUser,
    val isNewUser: Boolean
)

data class AuthUser(
    val id: Int,
    val phone: String,
    val name: String?,
    val role: String?
)