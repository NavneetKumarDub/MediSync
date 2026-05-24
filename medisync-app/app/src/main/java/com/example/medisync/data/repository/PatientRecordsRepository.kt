package com.example.medisync.data.repository

import com.example.medisync.networks.ApiService
import com.example.medisync.networks.PatientRecordDto

class PatientRecordsRepository(
    private val api: ApiService
) {
    suspend fun getPatientRecords(token: String): List<PatientRecordDto> {
        val response = api.getPatientRecords("Bearer $token")

        android.util.Log.d("PATIENT_RECORDS_API", "code=${response.code()} body=${response.body()}")

        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Failed to load patient records")
        }

        android.util.Log.d("PATIENT_RECORDS_API", "records size=${response.body()!!.records.size}")

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