package com.example.medisync.ui.components

import android.annotation.SuppressLint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
fun formatSmartDate(rawDate: String?): String {
    if (rawDate == null) return "No Date"
    return try {
        val date = LocalDate.parse(rawDate)
        val currentYear = LocalDate.now().year

        val pattern = if (date.year == currentYear) "d MMM" else "d MMM yyyy"
        date.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
    } catch (e: Exception) {
        rawDate
    }
}