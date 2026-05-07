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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatIsoDate(isoString: String?): String {
    if (isoString.isNullOrEmpty()) return "Upcoming"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val parsedDate = parser.parse(isoString)

        val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()

        parsedDate?.let { formatter.format(it).uppercase() } ?: "Upcoming"
    } catch (e: Exception) {
        isoString.substringBefore("T")
    }
}

class AppointmentViewModel : ViewModel() {

    var appointments by mutableStateOf<List<AppointmentItem>>(emptyList())
        private set

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
            } finally {
                isLoading = false
            }
        }
    }
}