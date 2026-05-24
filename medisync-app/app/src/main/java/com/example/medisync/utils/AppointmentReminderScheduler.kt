package com.example.medisync.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medisync.data.local.AppointmentEntity
import com.example.medisync.services.AppointmentReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AppointmentReminderScheduler {

    fun scheduleAppointmentReminders(
        context: Context,
        appointment: AppointmentEntity
    ) {
        val appointmentDateTime = parseAppointmentDateTime(
            date = appointment.date,
            time = appointment.time
        ) ?: return

        val startMillis = appointmentDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val tenMinBeforeMillis = startMillis - (10 * 60 * 1000)

        val now = System.currentTimeMillis()

        if (tenMinBeforeMillis > now) {
            scheduleNotification(
                context = context,
                appointment = appointment,
                triggerAtMillis = tenMinBeforeMillis,
                requestCode = appointment.id * 10 + 1,
                title = "Appointment in 10 minutes",
                body = "Your appointment with ${appointment.displayName} starts at ${appointment.time}"
            )
        }

        if (startMillis > now) {
            scheduleNotification(
                context = context,
                appointment = appointment,
                triggerAtMillis = startMillis,
                requestCode = appointment.id * 10 + 2,
                title = "Appointment starting now",
                body = "Your appointment with ${appointment.displayName} is starting now"
            )
        }
    }

    private fun scheduleNotification(
        context: Context,
        appointment: AppointmentEntity,
        triggerAtMillis: Long,
        requestCode: Int,
        title: String,
        body: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AppointmentReminderReceiver::class.java).apply {
            putExtra("appointmentId", appointment.id)
            putExtra("roomId", appointment.roomId ?: 0)
            putExtra("title", title)
            putExtra("body", body)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun parseAppointmentDateTime(
        date: String,
        time: String
    ): LocalDateTime? {
        return try {
            val cleanDate = date.substringBefore("T")
            val cleanTime = time
                .substringBefore("+")
                .substringBefore("Z")
                .substringBefore(".")

            val localDate = LocalDate.parse(cleanDate)
            val localTime = LocalTime.parse(cleanTime)

            LocalDateTime.of(localDate, localTime)
        } catch (e: Exception) {
            null
        }
    }
}