package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.navigation.NavItem
import com.example.medisync.ui.theme.natureGreen

private val BottomBarBg = Color.White
private val SelectedText = natureGreen
private val UnselectedText = Color(0xFF6B7280)
private val TopDivider = Color(0xFFE9EEF3)
private val SelectedGlassTop = Color(0x66EAF8FF)
private val SelectedGlassMid = Color(0x332A9DF4)
private val SelectedGlassBottom = Color(0x1A2A9DF4)
private val SelectedGlassBorder = Color(0x552A9DF4)

@Composable
fun BottomNavBar(
    navItems: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BottomBarBg)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.6.dp)
                .background(TopDivider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                NavBarItem(
                    item = item,
                    isSelected = selectedIndex == index,
                    onClick = { onItemSelected(index) }
                )
            }
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
            .width(78.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(
                brush = if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(
                            SelectedGlassTop,
                            SelectedGlassMid,
                            SelectedGlassBottom
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                },
                shape = RoundedCornerShape(18.dp)
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.92f),
                                SelectedGlassBorder
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(
                id = if (isSelected) item.iconFilled else item.iconOutlined
            ),
            contentDescription = item.label,
            tint = if (isSelected) SelectedText else UnselectedText,
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) SelectedText else UnselectedText,
            maxLines = 1
        )
    }
}
