package com.example.medisync.data.repository

import com.example.medisync.networks.ApiService
import com.example.medisync.networks.PatientRecordDto

class PatientRecordsRepository(
    private val api: ApiService
) {
    suspend fun getPatientRecords(token: String): List<PatientRecordDto> {
        val response = api.getPatientRecords("Bearer $token")

        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Failed to load patient records")
        }

        return response.body()!!.records
    }
    suspend fun getRecordViewUrl(token: String, fileKey: String): String {
        val response = api.getChatFileViewUrl(
            token = "Bearer $token",
            key = fileKey
        )

        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Failed to open file")
        }

        return response.body()!!.viewUrl
    }
}