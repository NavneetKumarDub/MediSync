package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.navigation.NavItem
import com.example.medisync.ui.theme.CapsuleGreen
import com.example.medisync.ui.theme.Iconselectedcolor
import com.example.medisync.ui.theme.natureGreen





// ── Component ──────────────────────────────────────────
@Composable
fun BottomNavBar(
    navItems:List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(natureGreen)
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment     = Alignment.Top
    ) {
        navItems.forEachIndexed { index, item ->
            NavBarItem(
                item       = item,
                isSelected = selectedIndex == index,
                onClick    = { onItemSelected(index) }
            )
        }
    }
}

@Composable
 fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Capsule highlight behind icon when active
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) CapsuleGreen else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isSelected) item.iconFilled else item.iconOutlined
                ),
                contentDescription = item.label,
                tint               = if (isSelected) Iconselectedcolor else Color.White,
                modifier           = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text       = item.label,
            fontSize   = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.W400,
            color      = Color.White
        )
    }
}




