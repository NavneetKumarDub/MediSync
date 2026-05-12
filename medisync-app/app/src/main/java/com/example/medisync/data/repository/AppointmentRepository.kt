package com.example.medisync.data.repository

import android.content.Context
import android.util.Log
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.AppointmentDao
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.networks.ApiService
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val apiService: ApiService,
    private val appointmentDao: AppointmentDao
) {

    val appointments: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()

    suspend fun syncPatientAppointments(context: Context) {
        sync(context, isDoctor = false)
    }

    suspend fun syncDoctorAppointments(context: Context) {
        sync(context, isDoctor = true)
    }

    private suspend fun sync(context: Context, isDoctor: Boolean) {
        try {
            val token = TokenManager.getToken(context) ?: return

            val response = if (isDoctor) {
                apiService.getDoctorAppointments("Bearer $token")
            } else {
                apiService.getPatientAppointments("Bearer $token")
            }

            if (response.isSuccessful) {
                val networkList = response.body()?.appointments ?: emptyList()

                val entities = networkList.map { item ->
                    AppointmentEntity(
                        id          = item.appointmentId,
                        status      = item.status ?: "pending",
                        type        = item.type ?: "Offline",
                        date        = item.date ?: "No Date",
                        time        = item.startTime ?: "No Time",
                        displayName = item.displayName ?: "Unknown",
                        subtitle    = item.speciality ?: "Patient",
                        photoUrl    = item.profilePhoto
                    )
                }

                appointmentDao.insertAppointments(entities)
                Log.d("Sync", "Cached ${entities.size} appointments")
            } else {
                Log.e("Sync", "Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("Sync", "Offline mode: showing cached data", e)
        }
    }
    suspend fun clearAll(context: Context) {
        appointmentDao.clearAll()
    }
}