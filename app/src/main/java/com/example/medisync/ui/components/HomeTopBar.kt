package com.example.medisync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.theme.natureGreen

// ─────────────────────────────────────────────
//  Colors
// ─────────────────────────────────────────────
val TopBarText    = Color(0xFFFFFFFF)
val SearchBarBg   = Color(0xFFFFFFFF)
val SearchHint    = Color(0xFF9CA3AF)

// ─────────────────────────────────────────────
//  HomeTopBar
// ─────────────────────────────────────────────
@Composable
fun HomeTopBar(
    location            : String = "Bangalore",
    onProfileClick      : () -> Unit = {},
    onLocationClick     : () -> Unit = {},
    onNotificationClick : () -> Unit = {},
    onSearchClick       : () -> Unit = {}
) {
    // graphicsLayer → background → padding (correct order)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shape = RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd   = 24.dp
                )
                clip = true
            }
            .background(natureGreen)
            .statusBarsPadding()
            .padding(
                start  = 16.dp,
                end    = 16.dp,
                top    = 16.dp,
                bottom = 24.dp
            )
    ) {
        // ── Row 1: Profile | Location | Notification ──
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile icon circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint               = TopBarText,
                    modifier           = Modifier.size(22.dp)
                )
            }

            // Location — centred
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLocationClick() },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint               = TopBarText,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = location,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TopBarText
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector        = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = TopBarText,
                    modifier           = Modifier.size(20.dp)
                )
            }

            // Notification bell with red dot
            Box(
                modifier         = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .clickable { onNotificationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint               = TopBarText,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                // Red dot badge
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD94040))
                        .align(Alignment.TopEnd)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Row 2: Search Bar ──────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SearchBarBg)
                .clickable { onSearchClick() }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = null,
                tint               = SearchHint,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text       = "Search doctors, specialities...",
                fontSize   = 14.sp,
                color      = SearchHint,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF6F7F9)
@Composable
fun HomeTopBarPreview() {
    HomeTopBar()
}