package com.example.medisync.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.AppointmentItem
import com.example.medisync.networks.RetrofitInstance
import kotlinx.coroutines.launch

class AppointmentViewModel : ViewModel() {

    // Holds your list of real appointments
    var appointments by mutableStateOf<List<AppointmentItem>>(emptyList())
        private set

    // Tracks if the app is currently waiting for the server
    var isLoading by mutableStateOf(false)
        private set

    fun fetchPatientAppointments(context: Context) {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = TokenManager.getToken(context)
                if (token != null) {
                    val response = RetrofitInstance.api.getPatientAppointments("Bearer $token")
                    if (response.isSuccessful) {
                        appointments = response.body()?.appointments ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // Handle errors like no internet here later
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchDoctorAppointments(context: Context) {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = TokenManager.getToken(context)
                if (token != null) {
                    val response = RetrofitInstance.api.getDoctorAppointments("Bearer $token")
                    if (response.isSuccessful) {
                        appointments = response.body()?.appointments ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // Handle errors like no internet here later
            } finally {
                isLoading = false
            }
        }
    }
}