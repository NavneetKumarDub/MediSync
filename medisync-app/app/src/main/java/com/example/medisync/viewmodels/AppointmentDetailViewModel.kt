package com.example.medisync.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.networks.DoctorDetail
import com.example.medisync.data.repository.AppointmentRepository
import com.example.medisync.networks.DoctorRatingDto
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.SubmitDoctorRatingRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

data class AppointmentDetailUiState(
    val rating: DoctorRatingDto? = null,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val doctor: DoctorDetail? = null,
    val isRatingLoading: Boolean = false,
    val isSubmittingRating: Boolean = false,
    val error: String? = null
)

class AppointmentDetailViewModel(
    repository: AppointmentRepository,
    appointmentId: Int
) : ViewModel() {

    val appointment: StateFlow<AppointmentEntity?> =
        repository.getAppointmentById(appointmentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    private val _uiState = MutableStateFlow(AppointmentDetailUiState())
    val uiState: StateFlow<AppointmentDetailUiState> = _uiState.asStateFlow()

    fun loadRatingData(context: Context) {
        viewModelScope.launch {
            val token = TokenManager.getToken(context) ?: return@launch
            val currentAppointment = appointment.value ?: return@launch
            val doctorId = currentAppointment.doctorId ?: return@launch

            _uiState.value = _uiState.value.copy(
                isRatingLoading = true,
                error = null
            )

            try {
                val appointmentRatingRes = RetrofitInstance.api.getAppointmentRating(
                    token = "Bearer $token",
                    appointmentId = currentAppointment.id
                )

                val summaryRes = RetrofitInstance.api.getDoctorRatingSummary(
                    token = "Bearer $token",
                    doctorId = doctorId
                )

                val doctorProfileRes = RetrofitInstance.api.getDoctorProfile(
                    token = "Bearer $token",
                    doctorId = doctorId
                )

                _uiState.value = _uiState.value.copy(
                    rating = if (appointmentRatingRes.isSuccessful) {
                        appointmentRatingRes.body()?.rating
                    } else {
                        null
                    },
                    ratingAverage = if (summaryRes.isSuccessful) {
                        summaryRes.body()?.average ?: 0.0
                    } else {
                        0.0
                    },
                    ratingCount = if (summaryRes.isSuccessful) {
                        summaryRes.body()?.count ?: 0
                    } else {
                        0
                    },
                    doctor = if (doctorProfileRes.isSuccessful) {
                        doctorProfileRes.body()?.doctor
                    } else {
                        null
                    },
                    isRatingLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRatingLoading = false,
                    error = e.message ?: "Failed to load rating"
                )
            }
        }
    }

    fun submitRating(
        context: Context,
        rating: Int,
        comment: String?
    ) {
        viewModelScope.launch {
            val token = TokenManager.getToken(context) ?: return@launch
            val currentAppointment = appointment.value ?: return@launch

            _uiState.value = _uiState.value.copy(
                isSubmittingRating = true,
                error = null
            )

            try {
                val response = RetrofitInstance.api.submitDoctorRating(
                    token = "Bearer $token",
                    request = SubmitDoctorRatingRequest(
                        appointmentId = currentAppointment.id,
                        rating = rating,
                        comment = comment
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        rating = response.body()!!.rating,
                        isSubmittingRating = false
                    )

                    loadRatingData(context)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingRating = false,
                        error = "Failed to submit rating"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingRating = false,
                    error = e.message ?: "Failed to submit rating"
                )
            }
        }
    }

    class Factory(
        private val repository: AppointmentRepository,
        private val appointmentId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppointmentDetailViewModel(repository, appointmentId) as T
        }
    }
}
