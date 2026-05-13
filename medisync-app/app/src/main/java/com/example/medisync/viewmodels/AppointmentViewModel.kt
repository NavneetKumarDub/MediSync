package com.example.medisync.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.collectLatest
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

class AppointmentViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    var appointments by mutableStateOf<List<AppointmentEntity>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)

    init {
        viewModelScope.launch {
            repository.appointments.collectLatest { list ->
                appointments = list
                isLoading = false
            }
        }
    }

    fun fetchPatientAppointments(context: Context) {
        viewModelScope.launch {
            try {
                repository.syncPatientAppointments(context)
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchDoctorAppointments(context: Context) {
        viewModelScope.launch {
            try {
                repository.syncDoctorAppointments(context)
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }
    fun clearAppointments(context: Context) {
        viewModelScope.launch {
            repository.clearAll(context)
        }
    }
    class Factory(private val repository: AppointmentRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(repository) as T
        }
    }
}