package com.example.medisync.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medisync.data.repository.PatientRecordsRepository
import com.example.medisync.networks.PatientRecordDto
import com.example.medisync.utils.FileCacheManager
import com.example.medisync.utils.PatientRecordsLocalCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PatientRecordsUiState(
    val isLoading: Boolean = true,
    val records: List<PatientRecordDto> = emptyList(),
    val cachedRecordUris: Map<String, String> = emptyMap(),
    val error: String? = null
)

class PatientRecordsViewModel(
    context: Context,
    private val repository: PatientRecordsRepository,
    private val token: String
) : ViewModel() {
    private val appContext = context.applicationContext

    private val _uiState = MutableStateFlow(PatientRecordsUiState())
    val uiState: StateFlow<PatientRecordsUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    fun loadRecords() {
        viewModelScope.launch {
            val cachedRecords = PatientRecordsLocalCache.load(appContext, token)
            if (cachedRecords.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    records = cachedRecords,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            try {
                val records = repository.getPatientRecords(token)
                PatientRecordsLocalCache.save(appContext, token, records)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    records = records,
                    error = null
                )
            } catch (e: Exception) {
                if (cachedRecords.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        records = cachedRecords,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load records"
                    )
                }
            }
        }
    }

    fun refreshRecords() {
        viewModelScope.launch {
            val currentRecords = _uiState.value.records
            val cachedRecords = PatientRecordsLocalCache.load(appContext, token)
            try {
                _uiState.value = _uiState.value.copy(error = null)
                val records = repository.getPatientRecords(token)
                PatientRecordsLocalCache.save(appContext, token, records)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    records = records,
                    cachedRecordUris = emptyMap(),
                    error = null
                )
            } catch (e: Exception) {
                val fallbackRecords = currentRecords.ifEmpty { cachedRecords }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    records = fallbackRecords,
                    error = if (fallbackRecords.isEmpty()) {
                        e.message ?: "Failed to refresh records"
                    } else {
                        null
                    }
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

    fun openRecordCached(
        context: Context,
        record: PatientRecordDto,
        onFileReady: (Uri) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val appContext = context.applicationContext
                val file = FileCacheManager.getOrDownloadFile(
                    context = appContext,
                    fileKey = record.fileKey,
                    fileName = record.fileName,
                    fileType = record.fileType
                ) {
                    repository.getRecordViewUrl(token, record.fileKey)
                }
                onFileReady(FileCacheManager.contentUri(appContext, file))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to open file"
                )
            }
        }
    }

    fun cacheRecords(context: Context) {
        viewModelScope.launch {
            val appContext = context.applicationContext
            _uiState.value.records.forEach { record ->
                if (_uiState.value.cachedRecordUris.containsKey(record.fileKey)) return@forEach

                try {
                    val file = FileCacheManager.getOrDownloadFile(
                        context = appContext,
                        fileKey = record.fileKey,
                        fileName = record.fileName,
                        fileType = record.fileType
                    ) {
                        repository.getRecordViewUrl(token, record.fileKey)
                    }

                    val uri = FileCacheManager.contentUri(appContext, file).toString()
                    _uiState.value = _uiState.value.copy(
                        cachedRecordUris = _uiState.value.cachedRecordUris + (record.fileKey to uri)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    class Factory(
        private val context: Context,
        private val repository: PatientRecordsRepository,
        private val token: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientRecordsViewModel(context, repository, token) as T
        }
    }
}
