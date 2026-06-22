package com.example.medisync.ui.navigation

import android.os.SystemClock
import androidx.navigation.NavController

private var lastBackClickTime = 0L

fun NavController.safePopBackStack(
    debounceMillis: Long = 600L
): Boolean {
    val now = SystemClock.elapsedRealtime()
    if (now - lastBackClickTime < debounceMillis) return false
    lastBackClickTime = now

    return popBackStack()
}
