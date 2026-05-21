package com.example.medisync.ui.screens.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.ChatMessageEntity
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.viewmodels.ChatViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── WhatsApp-style color palette with your blue accent ──
private val ChatBg         = Color(0xFFE7F0F4)
private val MyBubble       = Color(0xFF2A9DF4)
private val MyBubbleText   = Color.White
private val OtherBubble    = Color.White
private val OtherBubbleText = Color(0xFF111B21)
private val TimeTextMine   = Color.White.copy(alpha = 0.75f)
private val TimeTextOther  = Color(0xFF667781)
private val HeaderBg       = Color(0xFF2A9DF4)
private val HeaderText     = Color.White
private val InputBarBg     = Color(0xFFF5F6F6)
private val DateChipBg     = Color(0xFFD1E7F0)
private val DateChipText   = Color(0xFF1F4D6B)
private val ReadTick       = Color(0xFF53BDEB)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    roomId: Int,
    otherUserName: String,
    photoUrl: String? = null
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val app = context.applicationContext as MediSyncApplication

    var myUserId by remember { mutableStateOf<Int?>(null) }
    var myToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        myUserId = TokenManager.getUserId(context) ?: 0
        myToken = TokenManager.getToken(context) ?: ""
    }
    if (myUserId == null || myToken == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MyBubble)
        }
        return
    }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(
            repository = app.chatInboxRepository,
            roomId = roomId,
            myUserId = myUserId!!,
            token = myToken!!
        )
    )


    val uiState by chatViewModel.uiState.collectAsState()
    val messages by chatViewModel.messages.collectAsState()

    var inputText by remember { mutableStateOf("") }

    val lastMessageId = messages.lastOrNull()?.clientTempId ?: messages.lastOrNull()?.id?.toString()
    LaunchedEffect(lastMessageId) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }


    Scaffold(
        containerColor = ChatBg,
        topBar = {
            ChatTopBar(
                name = otherUserName,
                subtitle = uiState.headerStatus,
                photoUrl = photoUrl,
                onBack = { navController.popBackStack() },
                onJoin = { navController.navigate("video_room/$roomId") }
            )
        },
        bottomBar = {
            Column {
                if (uiState.isReconnecting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .background(Color(0xFFFFA726))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reconnecting…", fontSize = 12.sp, color = Color.White)
                    }
                }
                ChatInputBar(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        val trimmed = inputText.trim()
                        if (trimmed.isNotBlank()) {
                            chatViewModel.sendMessage(trimmed)
                            inputText = ""

                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val reversedList = messages.reversed()

            items(reversedList, key = { it.clientTempId ?: it.id.toString() }) { msg ->                val isMine = msg.senderId == myUserId
                MessageBubble(
                    message = msg,
                    isMine = isMine,
                    onVisible = {
                        if (!isMine && !msg.isRead) {
                            chatViewModel.markAsRead(msg.id)
                        }
                    }
                )
            }

            // 2. Put the DateChip at the END of the code, so it renders at the TOP of the screen!
            item { DateChip("Today") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    name: String,
    subtitle: String,
    photoUrl: String?,
    onBack: () -> Unit,
    onJoin: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBg,
            titleContentColor = HeaderText,
            navigationIconContentColor = HeaderText,
            actionIconContentColor = HeaderText
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = "${RetrofitInstance.MINIO_BASE_URL}$photoUrl",
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (name.isNotBlank() && name != "Loading...") {
                                name.split(" ").filter { it.isNotEmpty() }
                                    .take(2).joinToString("") { it.first().uppercase() }
                            } else {
                                ""
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Column {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        },
        actions = {
            JoinPill(onClick = onJoin)
            Spacer(Modifier.width(8.dp))
        }
    )
}

@Composable
private fun JoinPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = null,
            tint = HeaderBg,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Join",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = HeaderBg
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MessageBubble(
    message: ChatMessageEntity,
    isMine: Boolean,
    onVisible: () -> Unit
) {
    val timeFormatted = remember(message.sentAt) {
        try {
            val instant = Instant.parse(message.sentAt)
            val formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            "..."
        }
    }

    LaunchedEffect(message.id) { onVisible() }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isMine) 14.dp else 2.dp,
                            bottomEnd = if (isMine) 2.dp else 14.dp
                        )
                    )
                    .background(if (isMine) MyBubble else OtherBubble)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = message.message, // Updated from message.text
                        fontSize = 14.5.sp,
                        color = if (isMine) MyBubbleText else OtherBubbleText,
                        lineHeight = 20.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = if (isMine) TimeTextMine else TimeTextOther
                        )
                        if (isMine) {
                            Spacer(Modifier.width(4.dp))
                            when {
                                message.id < 0 -> Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TimeTextMine
                                )
                                message.isRead -> Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = ReadTick
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TimeTextMine
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChip(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DateChipBg)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(label, fontSize = 11.sp, color = DateChipText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatBg)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .navigationBarsPadding()
            .imePadding(),

        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(InputBarBg)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.EmojiEmotions,
                    contentDescription = "Emoji",
                    tint = Color(0xFF8696A0),
                    modifier = Modifier.size(22.dp)
                )
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Message", fontSize = 14.sp, color = Color(0xFF8696A0)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f).heightIn(min = 40.dp, max = 120.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.5.sp),
                maxLines = 5
            )
            IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = Color(0xFF8696A0),
                    modifier = Modifier.size(20.dp)
                )
            }
            if (value.isBlank()) {
                IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color(0xFF8696A0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MyBubble),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSend, modifier = Modifier.size(46.dp)) {
                Icon(
                    imageVector = if (value.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                    contentDescription = if (value.isNotBlank()) "Send" else "Voice",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}