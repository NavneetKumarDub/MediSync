package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ── COLORS ──
private val SlotCardBackground = Color(0xFFFFFFFF)
private val SlotCardGreen = Color(0xFF2E7D32)
private val SlotCardGray = Color(0xFF6B7280)
private val SlotCardBlack = Color(0xFF1A1A2E)
private val SlotCardDeleteBg = Color(0xFFDC2626)

@Composable
fun SlotCard(
    index: Int,
    startTime: String,
    endTime: String,
    duration: Int,
    mode: String,
    fee: String,
    isOpen: Boolean,
    onSwipeOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxSlide = -200f

    // when isOpen changes from outside — update offsetX
    LaunchedEffect(isOpen) {
        offsetX = if (isOpen) maxSlide else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
    ) {

        // ── DELETE BACKGROUND ──
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .background(SlotCardDeleteBg)
                .align(Alignment.CenterEnd)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Delete",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        // ── CARD ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(SlotCardBackground)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < maxSlide / 2) {
                                offsetX = maxSlide
                                onSwipeOpen()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(maxSlide, 0f)
                        }
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── INDEX ──
            Text(
                text = index.toString(),
                fontSize = 14.sp,
                color = SlotCardGray
            )

            // ── DETAILS ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$startTime  –  $endTime",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlotCardBlack
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailChip(text = "$duration min")
                    Text("·", color = SlotCardGray, fontSize = 12.sp)
                    DetailChip(
                        text = mode,
                        textColor = if (mode == "Online") SlotCardGreen else SlotCardGray
                    )
                    Text("·", color = SlotCardGray, fontSize = 12.sp)
                    DetailChip(text = "₹$fee")
                }
            }
        }
    }
}


// ── DETAIL CHIP ──
@Composable
fun DetailChip(
    text: String,
    textColor: Color = Color(0xFF6B7280)
) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}