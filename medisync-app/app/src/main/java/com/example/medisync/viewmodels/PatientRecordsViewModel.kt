package com.example.medisync.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.repository.PatientRecordsRepository
import com.example.medisync.networks.PatientRecordDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PatientRecordsUiState(
    val isLoading: Boolean = true,
    val records: List<PatientRecordDto> = emptyList(),
    val error: String? = null
)

class PatientRecordsViewModel(
    private val repository: PatientRecordsRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientRecordsUiState())
    val uiState: StateFlow<PatientRecordsUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    fun loadRecords() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val records = repository.getPatientRecords(token)
                _uiState.value = PatientRecordsUiState(
                    isLoading = false,
                    records = records
                )
            } catch (e: Exception) {
                _uiState.value = PatientRecordsUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load records"
                )
            }
        }
    }

    fun openRecord(fileKey: String, onUrlReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.getRecordViewUrl(token, fileKey)
                onUrlReady(url)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to open file"
                )
            }
        }
    }

    class Factory(
        private val repository: PatientRecordsRepository,
        private val token: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientRecordsViewModel(repository, token) as T
        }
    }
}