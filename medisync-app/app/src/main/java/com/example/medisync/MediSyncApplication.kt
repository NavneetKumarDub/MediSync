package com.example.medisync

import android.app.Application
import com.example.medisync.data.local.MediSyncDatabase
import com.example.medisync.data.repository.AppointmentRepository
import com.example.medisync.data.repository.ChatInboxRepository
import com.example.medisync.networks.RetrofitInstance
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MediSyncApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "medisync_notifications",
                "MediSync Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Messages and appointment updates"
                enableVibration(true)
                enableLights(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

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