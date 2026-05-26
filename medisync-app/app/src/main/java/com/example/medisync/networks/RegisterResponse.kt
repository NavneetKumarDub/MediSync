package com.example.medisync.networks

data class RegisterResponse(
    val message: String,
    val user: UserData?
)

data class UserData(
    val id: Int,
    val phone: String,
    val name: String,
    val role: String
)


data class ProfileResponse(
    val message:String
)
