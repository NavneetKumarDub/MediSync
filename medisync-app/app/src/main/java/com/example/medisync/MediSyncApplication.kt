package com.example.medisync

import android.app.Application
import com.example.medisync.data.local.MediSyncDatabase
import com.example.medisync.data.repository.AppointmentRepository
import com.example.medisync.data.repository.ChatInboxRepository
import com.example.medisync.networks.RetrofitInstance

class MediSyncApplication : Application() {

    val database by lazy { MediSyncDatabase.getDatabase(this) }

    val appointmentRepository by lazy {
        AppointmentRepository(
            apiService = RetrofitInstance.api,
            appointmentDao = database.appointmentDao()
        )
    }

    val chatInboxRepository by lazy {
        ChatInboxRepository(
            chatDao = database.chatInboxDao() ,
            chatMessageDao = database.chatMessageDao(),
            api = RetrofitInstance.api
        )
    }
}