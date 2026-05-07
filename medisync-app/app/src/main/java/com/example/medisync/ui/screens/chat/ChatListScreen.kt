package com.example.medisync.ui.screens.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.R
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.InboxChat
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.viewmodels.ChatInboxViewModel
import com.example.medisync.viewmodels.InboxUiState

val MediScreenNeutralBg = Color(0xFFF6F7F9)
val MediCardWhite = Color(0xFFFFFFFF)
val MediTopBarWhite = Color(0xFFFFFFFF)
val MediDividerGray = Color(0xFFE4E7EC)
val MediSearchGray = Color(0xFFEEF0F3)

val MediTextDark = Color(0xFF111827)
val MediTextMuted = Color(0xFF6B7280)
val MediTextPlaceholder = Color(0xFF9CA3AF)

val MediSkyBluePrimary = Color(0xFF03A9F4)
val MediSkyBlueSoftBg = Color(0xFFE1F5FE)
val MediSkyBlueText = Color(0xFF0288D1)
val MediSkyBlueBorder = Color(0xFFB3E5FC)

val MediChipActiveBg = MediSkyBlueSoftBg
val MediChipActiveBorder = MediSkyBlueBorder
val MediChipActiveText = MediSkyBlueText
val MediChipIdleBg = Color(0xFFEEF0F3)
val MediChipIdleText = MediTextMuted

val filterTabs = listOf("All", "Unread", "Doctors")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: ChatInboxViewModel = viewModel()
) {
    val context = LocalContext.current
    var activeFilter by remember { mutableStateOf("All") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchInbox(context)
    }

    val isLoading = uiState is InboxUiState.Loading
    val chats = if (uiState is InboxUiState.Success) {
        (uiState as InboxUiState.Success).chats
    } else {
        emptyList()
    }

    val list = remember(activeFilter, chats) {
        when (activeFilter) {
            "All" -> chats
            "Doctors" -> chats.filter { it.speciality != null }
            else -> chats
        }
    }
    var userRole by remember { mutableStateOf("patient") }

    LaunchedEffect(Unit) {
        userRole = TokenManager.getRole(context) ?: "patient"
    }
    val navItems = if (userRole == "doctor") NavItems.doctor else NavItems.patient

    Scaffold(
        containerColor = MediScreenNeutralBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MediTextDark
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MediTopBarWhite
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                navItems = navItems,
                selectedIndex = selectedTab,
                onItemSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MediScreenNeutralBg)
                .padding(innerPadding)
        ) {
            HorizontalDivider(thickness = 1.dp, color = MediDividerGray)
            Spacer(Modifier.height(12.dp))

            SearchBar()
            Spacer(Modifier.height(10.dp))

            FilterRow(active = activeFilter, onSelect = { activeFilter = it })
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MediSkyBluePrimary)
                }
            } else if (uiState is InboxUiState.Error) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as InboxUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                ChatList(list, navController)
            }
        }
    }
}

@Composable
fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MediCardWhite)
            .border(1.dp, MediDividerGray, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MediTextPlaceholder,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Search messages...",
            color = MediTextPlaceholder,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun FilterRow(active: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filterTabs.forEach { tab ->
            val isActive = tab == active
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) MediChipActiveBg else MediChipIdleBg)
                    .then(
                        if (isActive) Modifier.border(
                            width = 1.dp,
                            color = MediChipActiveBorder,
                            shape = RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) MediChipActiveText else MediChipIdleText
                )
            }
        }
    }
}

@Composable
fun ChatList(list: List<InboxChat>, navController: NavController) {
    if (list.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MediTextPlaceholder,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "No messages found",
                    color = MediTextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Start a new conversation",
                    color = MediTextPlaceholder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            itemsIndexed(list, key = { _, chat -> chat.roomId }) { index, chat ->
                ChatItemCard(
                    chat = chat,
                    onClick = {
                        navController.navigate("chat/${chat.roomId}")
                    }
                )
            }
        }
    }
}

@Composable
fun ChatItemCard(chat: InboxChat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MediCardWhite)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MediSkyBlueSoftBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = chat.name.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediSkyBlueText
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MediTextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.speciality ?: "Patient",
                color = MediTextMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F7F9)
@Composable
fun ChatListPreview() {
    ChatListScreen(navController = rememberNavController(), 2, {})
}