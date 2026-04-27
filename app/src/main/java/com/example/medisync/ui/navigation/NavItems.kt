package com.example.medisync.ui.navigation

import com.example.medisync.R

object NavItems {
    val patient = listOf(
        NavItem("Home", R.drawable.homefilled, R.drawable.homeunfilled),
        NavItem("Appointment", R.drawable.bookingfilled, R.drawable.bookingunfilled),
        NavItem("AI Chat", R.drawable.aifilled, R.drawable.aiunfilled),
        NavItem("Records", R.drawable.reportfilled, R.drawable.reportunfilled),
    )

    val doctor = listOf(
        NavItem("Home", R.drawable.homefilled, R.drawable.homeunfilled),
        NavItem("Schedule", R.drawable.bookingfilled, R.drawable.bookingunfilled),
        NavItem("Slots", R.drawable.aifilled, R.drawable.aiunfilled),
        NavItem("Dashboard", R.drawable.reportfilled, R.drawable.reportunfilled),
    )
}