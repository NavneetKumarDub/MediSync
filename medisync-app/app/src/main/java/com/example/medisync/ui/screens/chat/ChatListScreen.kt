package com.example.medisync.ui.screens.chat

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.medisync.data.TokenManager
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.components.SearchBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.viewmodels.ChatInboxViewModel
import com.example.medisync.data.local.ChatInboxEntity
import com.example.medisync.networks.RetrofitInstance

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val MediScreenNeutralBg = Color(0xFFFFFFFF)
private val MediCardWhite = Color(0xFFFFFFFF)
private val MediTopBarWhite = Color(0xFFFFFFFF)


private val MediSearchGray = Color(0xFFF3F4F6)
private val MediDividerGray = Color(0xFFF0F2F5)

private val MediTextDark = Color(0xFF111827)
private val MediTextMuted = Color(0xFF6B7280)
private val MediTextPlaceholder = Color(0xFF6B7280) 


private val MediSkyBlueSoftBg = Color(0xFFE1F5FE)
private val MediSkyBlueText = Color(0xFF0288D1)
private val AvtarColor = Color(0xFF3E505D)


private val MediChipActiveBg = natureGreen.copy(alpha = 0.1f)
private val MediChipActiveBorder = natureGreen.copy(alpha = 0.3f)
private val MediChipActiveText = natureGreen
private val MediChipIdleBg = Color(0xFFF3F4F6)
private val MediChipIdleText = MediTextMuted
private val MediActionBlue = Color(0xFF38BDF8)

private val filterTabs = listOf("All", "Unread", "Favourites", "Groups") 

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: ChatInboxViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as com.example.medisync.MediSyncApplication)
            .let { ChatInboxViewModel.Factory(it.chatInboxRepository) }
    )
) {
    val context = LocalContext.current
    var activeFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    
    var showAvatarDialog by remember { mutableStateOf(false) }
    var selectedAvatarUrl by remember { mutableStateOf<String?>(null) }
    var selectedAvatarName by remember { mutableStateOf("") }
    var selectedAvatarRoomId by remember { mutableStateOf<Int?>(null) }

    val chats by viewModel.inboxChats.collectAsState()
    LaunchedEffect(Unit) {
        val token = TokenManager.getToken(context)
        if (token != null) {
            viewModel.triggerSync(token)
        }
    }



    val list = remember(activeFilter, chats, searchQuery) {
        val filteredByTab = when (activeFilter) {
            "All" -> chats
            "Unread" -> chats.filter { it.unreadCount > 0 }

            else -> chats
        }

        filteredByTab.filter { it.matchesChatSearch(searchQuery) }
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
                    .background(MediScreenNeutralBg) 
                    .padding(innerPadding)
            ) {
                
                Spacer(Modifier.height(8.dp))
                SearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search chats"
                )
                Spacer(Modifier.height(12.dp))
                FilterRow(active = activeFilter, onSelect = { activeFilter = it })
                Spacer(Modifier.height(8.dp))


                ChatList(
                    list = list,
                    navController = navController,
                    onAvatarClick = { chat ->
                        selectedAvatarRoomId = chat.roomId
                        selectedAvatarName = chat.displayName
                        selectedAvatarUrl = chat.photoUrl
                        showAvatarDialog = true
                    }
                )

            }
        }

        
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
                                    model = "${RetrofitInstance.MINIO_BASE_URL}$selectedAvatarUrl",
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
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    val roomId = selectedAvatarRoomId
                                    if (roomId != null) {
                                        showAvatarDialog = false
                                        val encodedName = Uri.encode(selectedAvatarName)
                                        val encodedUrl = Uri.encode(selectedAvatarUrl)
                                        navController.navigate("chat/$roomId?name=$encodedName&photoUrl=$encodedUrl")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Chat",
                                    tint = MediActionBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Chat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MediActionBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ChatInboxEntity.matchesChatSearch(query: String): Boolean {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) return true

    return displayName.contains(cleanQuery, ignoreCase = true) ||
            lastMessage.orEmpty().contains(cleanQuery, ignoreCase = true) ||
            roomId.toString().contains(cleanQuery)
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
                    .clip(RoundedCornerShape(24.dp)) 
                    .background(if (isActive) MediChipActiveBg else MediChipIdleBg)
                    .then(
                        if (isActive) Modifier.border(
                            width = 1.dp,
                            color = MediChipActiveBorder, 
                            shape = RoundedCornerShape(24.dp)
                        ) else Modifier 
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatList(
    list: List<ChatInboxEntity>,
    navController: NavController,
    onAvatarClick: (ChatInboxEntity) -> Unit
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
                    onAvatarClick = { onAvatarClick(chat) },
                    onClick = {
                        val encodedName = Uri.encode(chat.displayName)
                        val encodedUrl = Uri.encode(chat.photoUrl)

                        navController.navigate("chat/${chat.roomId}?name=$encodedName&photoUrl=$encodedUrl")
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatItemCard(
    chat: ChatInboxEntity,
    onAvatarClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MediCardWhite) 
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MediSkyBlueSoftBg)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (chat.photoUrl != null) {
                AsyncImage(
                    model = "${RetrofitInstance.MINIO_BASE_URL}${chat.photoUrl}",
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = chat.displayName.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediSkyBlueText
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MediTextDark
            )
            Text(
                text = chat.lastMessage ?: "",
                color = MediTextMuted,
                fontSize = 12.sp,
            )
        }
        Text(
            text = formatChatTime(chat.lastMessageTime),
            color = MediTextMuted,
            fontSize = 12.sp,
        )


    }
}



@RequiresApi(Build.VERSION_CODES.O)
fun formatChatTime(isoString: String?): String {
    if (isoString.isNullOrEmpty()) return ""

    return try {
        
        val instant = Instant.parse(isoString)

        
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())

        
        when {
            localDate.isEqual(today) -> {
                
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
                timeFormatter.format(instant)
            }
            localDate.isEqual(today.minusDays(1)) -> {
                
                "Yesterday"
            }
            else -> {
                
                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy").withZone(ZoneId.systemDefault())
                dateFormatter.format(instant)
            }
        }
    } catch (e: Exception) {
        "" 
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ChatListPreview() {
    ChatListScreen(navController = rememberNavController(), 2, {})
}
