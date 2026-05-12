package com.example.medisync

import android.app.Application
import com.example.medisync.data.local.MediSyncDatabase
import com.example.medisync.data.repository.AppointmentRepository
import com.example.medisync.networks.RetrofitInstance

class MediSyncApplication : Application() {

    // 1. Initialize the Database (Lazy means it only creates when needed)
    val database by lazy { MediSyncDatabase.getDatabase(this) }

    // 2. Initialize the Repository
    // We pass the ApiService and the Dao into the Repository here
    val appointmentRepository by lazy {
        AppointmentRepository(
            apiService = RetrofitInstance.api,
            appointmentDao = database.appointmentDao()
        )
    }
}