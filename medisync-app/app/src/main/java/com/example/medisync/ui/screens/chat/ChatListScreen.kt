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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.InboxChat
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.components.SearchBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.theme.natureGreen // Make sure this is imported!
import com.example.medisync.viewmodels.ChatInboxViewModel
import com.example.medisync.viewmodels.InboxUiState

// Unified White Background for WhatsApp style
private val MediScreenNeutralBg = Color(0xFFFFFFFF)
private val MediCardWhite = Color(0xFFFFFFFF)
private val MediTopBarWhite = Color(0xFFFFFFFF)

// Subtle gray for search bar and dividers
private val MediSearchGray = Color(0xFFF3F4F6)
private val MediDividerGray = Color(0xFFF0F2F5)

private val MediTextDark = Color(0xFF111827)
private val MediTextMuted = Color(0xFF6B7280)
private val MediTextPlaceholder = Color(0xFF6B7280) // Darker placeholder for readability

// Avatar colors
private val MediSkyBlueSoftBg = Color(0xFFE1F5FE)
private val MediSkyBlueText = Color(0xFF0288D1)
private val AvtarColor = Color(0xFF3E505D)

// Filter Chip colors (Using your natureGreen theme)
private val MediChipActiveBg = natureGreen.copy(alpha = 0.1f)
private val MediChipActiveBorder = natureGreen.copy(alpha = 0.3f)
private val MediChipActiveText = natureGreen
private val MediChipIdleBg = Color(0xFFF3F4F6)
private val MediChipIdleText = MediTextMuted

private val filterTabs = listOf("All", "Unread", "Favourites", "Groups") // Updated to match WhatsApp screenshot

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

    // avatar popup state
    var showAvatarDialog by remember { mutableStateOf(false) }
    var selectedAvatarUrl by remember { mutableStateOf<String?>(null) }
    var selectedAvatarName by remember { mutableStateOf("") }

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
            "Doctors", "Favourites" -> chats.filter { it.speciality != null }
            else -> chats
        }
    }

    var userRole by remember { mutableStateOf("patient") }
    LaunchedEffect(Unit) {
        userRole = TokenManager.getRole(context) ?: "patient"
    }
    val navItems = if (userRole == "doctor") NavItems.doctor else NavItems.patient


    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MediScreenNeutralBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ChatList",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = natureGreen
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
                    .background(MediScreenNeutralBg) // Now solid white
                    .padding(innerPadding)
            ) {
                // Removed the top divider to match WhatsApp
                Spacer(Modifier.height(8.dp))
                SearchBar()
                Spacer(Modifier.height(12.dp))
                FilterRow(active = activeFilter, onSelect = { activeFilter = it })
                Spacer(Modifier.height(8.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = natureGreen)
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
                    ChatList(
                        list = list,
                        navController = navController,
                        onAvatarClick = { name, photoUrl ->
                            selectedAvatarName = name
                            selectedAvatarUrl = photoUrl
                            showAvatarDialog = true
                        }
                    )
                }
            }
        }

        // Popup Avatar Dialog Box (unchanged, pure Box overlay)
        if (showAvatarDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showAvatarDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .wrapContentHeight()
                        .offset(y = (-80).dp)
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(AvtarColor)
                        ) {
                            if (selectedAvatarUrl != null) {
                                AsyncImage(
                                    model = selectedAvatarUrl,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MediSkyBlueSoftBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedAvatarName.take(1).uppercase(),
                                        fontSize = 100.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MediSkyBlueText
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.25f))
                                    .align(Alignment.TopCenter)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = selectedAvatarName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat",
                                tint = natureGreen, // Updated to your theme color
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { showAvatarDialog = false }
                            )
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = natureGreen, // Updated to your theme color
                                modifier = Modifier
                                    .size(26.dp)
                                    .clickable { showAvatarDialog = false }
                            )
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = natureGreen, // Updated to your theme color
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { showAvatarDialog = false }
                            )
                        }
                    }
                }
            }
        }
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
                    .clip(RoundedCornerShape(24.dp)) // WhatsApp style pill shape
                    .background(if (isActive) MediChipActiveBg else MediChipIdleBg)
                    .then(
                        if (isActive) Modifier.border(
                            width = 1.dp,
                            color = MediChipActiveBorder, // Border only on active
                            shape = RoundedCornerShape(24.dp)
                        ) else Modifier // No border on idle tabs
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) MediChipActiveText else MediChipIdleText
                )
            }
        }
    }
}

@Composable
fun ChatList(
    list: List<InboxChat>,
    navController: NavController,
    onAvatarClick: (name: String, photoUrl: String?) -> Unit
) {
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
                    onAvatarClick = { onAvatarClick(chat.name, chat.profilePhoto) },
                    onClick = { navController.navigate("chat/${chat.roomId}") }
                )
            }
        }
    }
}

@Composable
fun ChatItemCard(
    chat: InboxChat,
    onAvatarClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MediCardWhite) // Solid white background, matches screen
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // tappable avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MediSkyBlueSoftBg)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (chat.profilePhoto != null) {
                AsyncImage(
                    model = chat.profilePhoto,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
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

        Text(
            text = "Yesterday",
            color = MediTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Top) // Aligns to the top right of the row
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ChatListPreview() {
    ChatListScreen(navController = rememberNavController(), 2, {})
}