package com.example.medisync.ui.components

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.medisync.networks.AppointmentItem


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
private fun statusConfig(status: String?) = when (status?.lowercase()) {
    // If the database says "accepted" OR "upcoming", show the blue "Upcoming" badge
    "accepted", "upcoming" -> StatusConfig("Upcoming",  ColorUpcoming)

    // Catch pending appointments
    "pending"              -> StatusConfig("Pending",   Color(0xFFFFA500))

    // Ongoing/Online
    "ongoing", "online"    -> StatusConfig("Ongoing",   ColorOngoing)

    // Past/Completed
    "completed", "past"    -> StatusConfig("Completed", ColorCompleted)

    "cancelled"            -> StatusConfig("Cancelled", ColorCancelled)

    // Give it a visible gray color so you can easily see if a new status slips through
    else -> StatusConfig("Unknown", Color.LightGray)
}


// ─────────────────────────────────────────────
//  Router
// ─────────────────────────────────────────────
@Composable
fun AppointmentCard(appt: AppointmentItem,onClick: () -> Unit) {
    CardShell(appt = appt ,onClick = onClick)
}

// ─────────────────────────────────────────────
//  CardShell — Option C layout
// ─────────────────────────────────────────────
@Composable
private fun CardShell(appt: AppointmentItem,onClick:() -> Unit) {
    val cfg = statusConfig(appt.status)

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable{onClick()}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreenBg)
        ) {
            // Left accent strip
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
                // Avatar
                Avatar(initials = appt.displayName.getInitials())

                // Text block
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Doctor name
                    Text(
                        text       = appt.displayName ?: "Unknown User",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextHeading,
                        lineHeight = 20.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )

                    // Status line
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(cfg.color)
                        )

                        Text(
                            text       = cfg.label,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = cfg.color,
                            lineHeight = 15.sp
                        )

                        Text(
                            text       = "·",
                            fontSize   = 12.sp,
                            color      = TextSub,
                            lineHeight = 15.sp
                        )

                        Icon(
                            imageVector        = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint               = TextSub,
                            modifier           = Modifier.size(11.dp)
                        )

                        // Specialty · Date Time
                        val specialtyPart = appt.speciality?.substringBefore(" ·") ?: "General"
                        val datePart = appt.date ?: "No Date"
                        val timePart = appt.startTime ?: "N/A"
                        Text(
                            text = "$specialtyPart · $datePart · $timePart",
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 17.dp)
                .height(1.dp)
                .background(Color(0xFFE4E7EC))
        )
    }
}


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

private fun String?.getInitials(): String {
    if (this.isNullOrBlank()) return "??"

    return this.removePrefix("Dr. ")
        .removePrefix("Dr ")
        .trim()
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
}

