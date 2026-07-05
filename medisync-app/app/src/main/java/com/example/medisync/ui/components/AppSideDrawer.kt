package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppDrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private val DrawerBg = Color.White
private val AccentBlue = Color(0xFF2A9DF4)
private val TextDark = Color(0xFF111B21)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun AppSideDrawer(
    name: String,
    phone: String,
    userId: Int,            // ADDED
    photoKey: String?,      // CHANGED from photoUrl
    token: String,          // ADDED
    items: List<AppDrawerItem>,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = DrawerBg,
        drawerContentColor = TextDark,
        modifier = Modifier.width(304.dp)
    ) {
        DrawerProfileHeader(
            name = name,
            phone = phone,
            userId = userId,
            photoKey = photoKey,
            token = token,
            onClick = onProfileClick
        )

        Spacer(Modifier.height(8.dp))

        items.forEach { item ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = item.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = false,
                onClick = item.onClick,
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = AccentBlue
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = TextDark,
                    unselectedIconColor = AccentBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .height(52.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        Divider(color = Color(0xFFE5E7EB))

        NavigationDrawerItem(
            label = {
                Text(
                    text = "Logout",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            selected = false,
            onClick = onLogoutClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null
                )
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedTextColor = Color(0xFFD32F2F),
                unselectedIconColor = Color(0xFFD32F2F)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .height(52.dp)
        )
    }
}

@Composable
private fun DrawerProfileHeader(
    name: String,
    phone: String,
    userId: Int,
    photoKey: String?,
    token: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    userId = userId,
                    photoKey = photoKey,
                    token = token,
                    name = name
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { "MediSync User" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )

                    if (phone.isNotBlank()) {
                        Text(
                            text = phone,
                            fontSize = 13.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    userId: Int,
    photoKey: String?,
    token: String,
    name: String
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(AccentBlue.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        // Use the new offline-capable component!
        ProfilePhoto(
            userId = userId,
            photoKey = photoKey,
            token = token,
            name = name,
            size = 58.dp,
            modifier = Modifier.fillMaxSize()
        )
    }
}