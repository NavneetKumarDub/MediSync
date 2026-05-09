//package com.example.medisync.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.medisync.ui.screens.patient.Appointment
//import com.example.medisync.ui.theme.ShadowAmbient
//import com.example.medisync.ui.theme.ShadowSpot
//
//// ─────────────────────────────────────────────
////  NextAppointmentCard
//// ─────────────────────────────────────────────
//@Composable
//fun NextAppointmentCard(
//    appt     : Appointment,
//    modifier : Modifier = Modifier
//) {
//
//    val dateLabel = appt.date
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp)
//            .shadow(
//                elevation    = 4.dp,
//                shape        = RoundedCornerShape(16.dp),
//                ambientColor = ShadowAmbient,   // tinted to match #F6F7F9 bg
//                spotColor    = ShadowSpot    // same tint, soft
//            )
//            .clip(RoundedCornerShape(16.dp))
//            .background(Color(0xFFFFFFFF))
//    ) {
//        // ── Top label strip ───────────────────────
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color(0xFFE6F7F0))
//                .padding(horizontal = 14.dp, vertical = 8.dp),
//            verticalAlignment     = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(6.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(7.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFF27AE7A))
//            )
//            Text(
//                text       = "Upcoming · $dateLabel",
//                fontSize   = 11.sp,
//                fontWeight = FontWeight.SemiBold,
//                color      = Color(0xFF1A8C61)
//            )
//        }
//
//        // ── Doctor row ────────────────────────────
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 14.dp, vertical = 14.dp),
//            verticalAlignment     = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            // Avatar
//            Box(
//                modifier = Modifier
//                    .size(50.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFFE6F7F0)),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text       = appt.doctorName.getInitials(),
//                    fontSize   = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    color      = Color(0xFF1A8C61)
//                )
//            }
//
//            // Name + specialty
//            Column(
//                modifier            = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(3.dp)
//            ) {
//                Text(
//                    text       = appt.doctorName,
//                    fontSize   = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color      = Color(0xFF111827),
//                    maxLines   = 1,
//                    overflow   = TextOverflow.Ellipsis
//                )
//                Text(
//                    text       = appt.specialty.substringBefore(" ·"),
//                    fontSize   = 12.sp,
//                    fontWeight = FontWeight.Normal,
//                    color      = Color(0xFF6B7280)
//                )
//            }
//        }
//
//        // ── Divider ───────────────────────────────
//        HorizontalDivider(
//            thickness = 1.dp,
//            color     = Color(0xFFE4E7EC),
//            modifier  = Modifier.padding(horizontal = 14.dp)
//        )
//
//        // ── Time / Mode / Date row ────────────────
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 14.dp, vertical = 12.dp),
//            verticalAlignment     = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Time
//            InfoChip(label = "Time", value = "3:00 PM")
//
//            VerticalLine()
//
//            // Mode
//            InfoChip(label = "Mode", value = "Online")
//
//            VerticalLine()
//
//            // Date
//            InfoChip(label = "Date", value = dateLabel)
//        }
//    }
//}
//
//// ─────────────────────────────────────────────
////  InfoChip — label above, value below
//// ─────────────────────────────────────────────
//@Composable
//private fun InfoChip(label: String, value: String) {
//    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
//        Text(
//            text       = label,
//            fontSize   = 10.sp,
//            fontWeight = FontWeight.Normal,
//            color      = Color(0xFF9CA3AF)
//        )
//        Text(
//            text       = value,
//            fontSize   = 13.sp,
//            fontWeight = FontWeight.SemiBold,
//            color      = Color(0xFF111827)
//        )
//    }
//}
//
//// ─────────────────────────────────────────────
////  Thin vertical divider between info cells
//// ─────────────────────────────────────────────
//@Composable
//private fun VerticalLine() {
//    Box(
//        modifier = Modifier
//            .width(1.dp)
//            .height(30.dp)
//            .background(Color(0xFFE4E7EC))
//    )
//}
//
//// ─────────────────────────────────────────────
////  Initials helper
//// ─────────────────────────────────────────────
//private fun String.getInitials(): String =
//    this.removePrefix("Dr. ")
//        .split(" ")
//        .take(2)
//        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
//        .joinToString("")
//
//// ─────────────────────────────────────────────
////  Preview
//// ─────────────────────────────────────────────
//@Preview(showBackground = true, backgroundColor = 0xFFF6F7F9)
//@Composable
//fun NextAppointmentCardPreview() {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color(0xFFF6F7F9))
//            .padding(vertical = 16.dp)
//    ) {
//        NextAppointmentCard(
//            appt = Appointment(
//                id = 1,
//                doctorName = "Dr. Anjali Sharma",
//                specialty = "Cardiology · General Checkup",
//                time = "3:00 pm",
//                status = "upcoming",
//                date = "12 Apr 2026",
//                mode = "online",
//            )
//        )
//    }
//}
