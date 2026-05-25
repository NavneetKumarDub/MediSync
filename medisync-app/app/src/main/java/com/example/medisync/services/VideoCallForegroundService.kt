package com.example.medisync.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.medisync.R
import com.example.medisync.VideoCallActivity

class VideoCallForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val roomId = intent?.getIntExtra(EXTRA_ROOM_ID, 0) ?: 0
        createChannel()

        val notification = buildNotification(roomId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    private fun buildNotification(roomId: Int): Notification {
        val openIntent = Intent(this, VideoCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", "video_call")
            putExtra("roomId", roomId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            roomId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Video call in progress")
            .setContentText("Tap to return to your call")
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                "Mute",
                actionPendingIntent(ACTION_TOGGLE_MIC, roomId, 1)
            )
            .addAction(
                0,
                "Speaker",
                actionPendingIntent(ACTION_USE_SPEAKER, roomId, 2)
            )
            .addAction(
                0,
                "End",
                actionPendingIntent(ACTION_END_CALL, roomId, 3)
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun actionPendingIntent(action: String, roomId: Int, requestOffset: Int): PendingIntent {
        val intent = Intent(this, VideoCallActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_ROOM_ID, roomId)
        }

        return PendingIntent.getBroadcast(
            this,
            roomId + requestOffset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Video calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active MediSync video call"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "video_call_channel"
        private const val NOTIFICATION_ID = 4001
        private const val EXTRA_ROOM_ID = "roomId"

        const val ACTION_TOGGLE_MIC = "com.example.medisync.video.TOGGLE_MIC"
        const val ACTION_USE_SPEAKER = "com.example.medisync.video.USE_SPEAKER"
        const val ACTION_END_CALL = "com.example.medisync.video.END_CALL"

        fun start(context: Context, roomId: Int) {
            val intent = Intent(context, VideoCallForegroundService::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomId)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, VideoCallForegroundService::class.java))
        }
    }
}
