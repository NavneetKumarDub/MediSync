package com.example.medisync.ui.screens.chat


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// ─────────────────────────────────────────────
//  Design System
// ─────────────────────────────────────────────
val ScreenBg         = Color(0xFFF6F7F9)
val CardBg           = Color(0xFFFFFFFF)
val TopBarBg         = Color(0xFFFFFFFF)
val DividerColor     = Color(0xFFE4E7EC)
val SearchBg         = Color(0xFFEEF0F3)

val TextPrimary      = Color(0xFF111827)
val TextSecondary    = Color(0xFF6B7280)
val TextHint         = Color(0xFF9CA3AF)

val GreenPrimary     = Color(0xFF27AE7A)
val GreenLight       = Color(0xFFE6F7F0)
val GreenText        = Color(0xFF1A8C61)
val GreenBorder      = Color(0xFFB2DFD0)

val ChipActiveBg     = GreenLight
val ChipActiveBorder = GreenBorder
val ChipActiveText   = GreenText
val ChipIdleBg       = Color(0xFFEEF0F3)
val ChipIdleText     = TextSecondary

val filterTabs = listOf("All", "Unread", "Doctors")

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    selectedTab : Int,
    onTabSelected: (Int) -> Unit,
    viewModel: ChatInboxViewModel = viewModel()
) {
    val context = LocalContext.current
    var activeFilter by remember { mutableStateOf("All") }

    // Read the UI State from the new ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Fetch the inbox data when the screen opens
    LaunchedEffect(Unit) {
        viewModel.fetchInbox(context)
    }

    // Determine loading state and extract chat list
    val isLoading = uiState is InboxUiState.Loading
    val chats = if (uiState is InboxUiState.Success) {
        (uiState as InboxUiState.Success).chats
    } else {
        emptyList()
    }

    // Filter logic
    val list = remember(activeFilter, chats) {
        when (activeFilter) {
            "All" -> chats
            "Doctors" -> chats.filter { it.speciality != null }
            // Add other filter conditions here if needed
            else -> chats
        }
    }
    var userRole by remember { mutableStateOf("patient") }

    LaunchedEffect(Unit) {
        userRole = TokenManager.getRole(context) ?: "patient"
    }
    // 2. Choose the correct navigation list
    val navItems = if (userRole == "doctor") NavItems.doctor else NavItems.patient

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Messages", // Changed from Appointments
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBg
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                navItems = navItems,
                selectedIndex  = selectedTab,
                onItemSelected = onTabSelected
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { /* TODO: Open New Chat Screen */ },
                shape          = CircleShape,
                containerColor = GreenPrimary,
                contentColor   = Color.White
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.plus),
                    contentDescription = "New Message",
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(innerPadding)
        ) {
            HorizontalDivider(thickness = 1.dp, color = DividerColor)
            Spacer(Modifier.height(12.dp))

            SearchBar()
            Spacer(Modifier.height(10.dp))

            FilterRow(active = activeFilter, onSelect = { activeFilter = it })
            Spacer(Modifier.height(4.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
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

// ─────────────────────────────────────────────
//  Search Bar
// ─────────────────────────────────────────────
@Composable
fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector        = Icons.Default.Search,
            contentDescription = null,
            tint               = TextHint,
            modifier           = Modifier.size(18.dp)
        )
        Text(
            text       = "Search messages...",
            color      = TextHint,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────
//  Filter Chips
// ─────────────────────────────────────────────
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
                    .background(if (isActive) ChipActiveBg else ChipIdleBg)
                    .then(
                        if (isActive) Modifier.border(
                            width = 1.dp,
                            color = ChipActiveBorder,
                            shape = RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = tab,
                    fontSize   = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isActive) ChipActiveText else ChipIdleText
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Chat List
// ─────────────────────────────────────────────
@Composable
fun ChatList(list: List<InboxChat>, navController: NavController) {
    if (list.isEmpty()) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint               = TextHint,
                    modifier           = Modifier.size(40.dp)
                )
                Text(
                    text       = "No messages found",
                    color      = TextSecondary,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text       = "Start a new conversation",
                    color      = TextHint,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    } else {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
        ) {
            items(list, key = { it.roomId }) { chat ->
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

// ─────────────────────────────────────────────
//  Chat Item Card (Adapted to match your design system)
// ─────────────────────────────────────────────
@Composable
fun ChatItemCard(chat: InboxChat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture Placeholder
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = GreenLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = chat.name.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenText
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.speciality ?: "Patient",
                    color = TextSecondary,
                    fontSize = 14.sp
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
fun ChatListPreview() {
    ChatListScreen(navController = rememberNavController(), 2, {})
}