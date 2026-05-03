package com.example.medisync.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── COLORS ──
private val ScrollChipSelected = Color(0xFF2E7D32)
private val ScrollChipUnselected = Color(0xFFFFFFFF)
private val ScrollChipSelectedText = Color(0xFFFFFFFF)
private val ScrollChipUnselectedText = Color(0xFF6B7280)
private val ScrollChipBorder = Color(0xFFE5E7EB)
private val ScrollChipSelectedBorder = Color(0xFF2E7D32)

@Composable
fun HorizontalScrollSelector(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                label = {
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        fontWeight = if (selectedItem == item)
                            FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScrollChipSelected,
                    selectedLabelColor = ScrollChipSelectedText,
                    containerColor = ScrollChipUnselected,
                    labelColor = ScrollChipUnselectedText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedItem == item,
                    selectedBorderColor = ScrollChipSelectedBorder,
                    borderColor = ScrollChipBorder
                )
            )
        }
    }
}