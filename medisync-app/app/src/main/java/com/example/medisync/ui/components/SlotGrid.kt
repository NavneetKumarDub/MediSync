package com.example.medisync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medisync.networks.SlotItem

@Composable
fun SlotGrid(
    slots       : List<SlotItem>,
    selectedSlot: SlotItem?,
    onSelect    : (SlotItem) -> Unit
) {
    val rows = slots.chunked(3)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowSlots ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                rowSlots.forEach { slot ->
                    SlotChip(
                        slot       = slot,
                        isSelected = selectedSlot?.id == slot.id,
                        onClick    = {
                            if (slot.status == "available") onSelect(slot)
                        },
                        modifier   = Modifier.weight(1f)
                    )
                }
                
                repeat(3 - rowSlots.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}