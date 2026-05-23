package com.example.medisync.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.medisync.MainActivity
import com.example.medisync.R
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.SaveFcmTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediSyncFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            val jwt = TokenManager.getToken(applicationContext) ?: return@launch

            runCatching {
                RetrofitInstance.api.saveFcmToken(
                    token = "Bearer $jwt",
                    body = SaveFcmTokenRequest(token = token)
                )
            }.onFailure {
                Log.e("FCM", "Failed to save FCM token", it)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM_DATA", "data=${message.data}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "MediSync"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new notification"

        showNotification(
            title = title,
            body = body,
            data = message.data
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ){
        val channelId = "medisync_notifications"

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MediSync Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Messages and appointment updates"
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("notification_type", data["type"])
            putExtra("roomId", data["roomId"])
            putExtra("senderId", data["senderId"])
            putExtra("messageId", data["messageId"])
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}