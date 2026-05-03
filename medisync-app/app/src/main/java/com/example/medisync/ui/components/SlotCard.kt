package com.example.medisync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ── COLORS ──
private val SlotCardBackground = Color(0xFFFFFFFF)
private val SlotCardBorder = Color(0xFFE5E7EB)
private val SlotCardGreen = Color(0xFF2E7D32)
private val SlotCardGray = Color(0xFF6B7280)
private val SlotCardBlack = Color(0xFF1A1A2E)
private val SlotCardDeleteBg = Color(0xFFDC2626)
private val SlotCardIndexBg = Color(0xFFF0FDF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotCard(
    index: Int,
    startTime: String,
    endTime: String,
    duration: Int,
    mode: String,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // ── DELETE BACKGROUND ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlotCardDeleteBg),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "delete",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        // ── SLOT CARD ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SlotCardBackground)
                .border(1.dp, SlotCardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── INDEX ──
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SlotCardIndexBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlotCardGreen
                )
            }

            // ── SLOT DETAILS ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$startTime — $endTime",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlotCardBlack
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$duration min",
                        fontSize = 12.sp,
                        color = SlotCardGray
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = SlotCardGray
                    )
                    Text(
                        text = mode,
                        fontSize = 12.sp,
                        color = if (mode == "Online") SlotCardGreen else SlotCardGray
                    )
                }
            }
        }
    }
}