package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.screens.patient.Appointment
import com.example.medisync.ui.screens.patient.Status

// ─────────────────────────────────────────────
//  Colors
// ─────────────────────────────────────────────
private val ScreenBg    = Color(0xFFF6F7F9)
private val TextHeading = Color(0xFF111827)
private val TextSub     = Color(0xFF9CA3AF)
private val AvatarBg    = Color(0xFFE6F7F0)
private val AvatarText  = Color(0xFF1A8C61)

// Status colors — dot + label text
private val ColorUpcoming  = Color(0xFF2475D0)  // blue
private val ColorOngoing   = Color(0xFF27AE7A)  // green
private val ColorCompleted = Color(0xFF6B7280)  // gray
private val ColorCancelled = Color(0xFFD94040)  // red

// ─────────────────────────────────────────────
//  Status config — bundles everything per status
// ─────────────────────────────────────────────
private data class StatusConfig(
    val label : String,
    val color : Color
)

private fun statusConfig(status: Status) = when (status) {
    Status.UPCOMING  -> StatusConfig("Upcoming",  ColorUpcoming)
    Status.ONGOING   -> StatusConfig("Ongoing",   ColorOngoing)
    Status.PAST      -> StatusConfig("Completed", ColorCompleted)
    Status.CANCELLED -> StatusConfig("Cancelled", ColorCancelled)
    Status.ONLINE    -> StatusConfig("Online",    ColorOngoing)
    Status.OFFLINE   -> StatusConfig("Offline",   ColorCompleted)
}

// ─────────────────────────────────────────────
//  Router
// ─────────────────────────────────────────────
@Composable
fun AppointmentCard(appt: Appointment) {
    CardShell(appt = appt)
}

// ─────────────────────────────────────────────
//  CardShell — Option C layout
//
//  |strip|  [Avatar]   Dr. Anjali Sharma
//                      ● Upcoming · Cardiology · Today 3:00 PM
//
//  — No border, no elevation
//  — Card bg = screen bg (invisible box)
//  — Thin bottom divider as only separator
//  — Status shown inline under doctor name
// ─────────────────────────────────────────────
@Composable
private fun CardShell(appt: Appointment) {
    val cfg = statusConfig(appt.status)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreenBg)
        ) {
            // Left accent strip — status color, 3dp wide
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(cfg.color)
            )

            // Card content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                // Avatar — dominates left, anchors the card
                Avatar(initials = appt.doctorName.getInitials())

                // Text block
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Doctor name — hero text
                    Text(
                        text       = appt.doctorName,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextHeading,
                        lineHeight = 20.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )

                    // Option C: ● Upcoming · Cardiology · Today 3:00 PM
                    // All in one line — dot, status word colored, rest muted
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Filled colored dot
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(cfg.color)
                        )

                        // Status word in status color
                        Text(
                            text       = cfg.label,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = cfg.color,
                            lineHeight = 15.sp
                        )

                        // Separator dot
                        Text(
                            text       = "·",
                            fontSize   = 12.sp,
                            color      = TextSub,
                            lineHeight = 15.sp
                        )

                        // Clock icon — tiny visual cue for time
                        Icon(
                            imageVector        = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint               = TextSub,
                            modifier           = Modifier.size(11.dp)
                        )

                        // Specialty · Date Time — muted, single line
                        Text(
                            text = "${appt.specialty.substringBefore(" ·")} · ${appt.time}",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color      = TextSub,
                            lineHeight = 15.sp,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Thin bottom divider — only card separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 17.dp) // aligns with text, not strip
                .height(1.dp)
                .background(Color(0xFFE4E7EC))
        )
    }
}

// ─────────────────────────────────────────────
//  Avatar
// ─────────────────────────────────────────────
@Composable
private fun Avatar(initials: String) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(AvatarBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = AvatarText
        )
    }
}

// ─────────────────────────────────────────────
//  Initials helper
// ─────────────────────────────────────────────
private fun String.getInitials(): String =
    this.removePrefix("Dr. ")
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
