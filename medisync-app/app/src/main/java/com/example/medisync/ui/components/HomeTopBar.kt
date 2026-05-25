package com.example.medisync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  Colors
// ─────────────────────────────────────────────
val TopBarText    = Color(0xFF0288D1)
val SearchBarBg   = Color(0xEEFFFFFF)
val SearchHint    = Color(0xFF9CA3AF)
private val HeaderGlassTop = Color(0xFF03A9F4)
private val HeaderGlassMid = Color(0xA84DBFF8)
private val HeaderGlassBottom = Color(0x40EAF8FF)
private val HeaderGlassBorder = Color(0x552A9DF4)

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
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                ambientColor = Color(0x1A03A9F4),
                spotColor = Color(0x2603A9F4)
            )
            .graphicsLayer {
                shape = RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd   = 24.dp
                )
                clip = true
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HeaderGlassTop,
                        HeaderGlassMid,
                        Color(0x78CDEFFF),
                        HeaderGlassBottom
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.92f),
                        HeaderGlassBorder
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .statusBarsPadding()
            .padding(
                start  = 16.dp,
                end    = 16.dp,
                top    = 22.dp,
                bottom = 18.dp
            )
    ) {
        // ── Profile + Search ──────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SearchBarBg)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.86f),
                        shape = CircleShape
                    )
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

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SearchBarBg)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.86f),
                        shape = RoundedCornerShape(18.dp)
                    )
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
}

// ─────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF6F7F9)
@Composable
fun HomeTopBarPreview() {
    HomeTopBar()
}
