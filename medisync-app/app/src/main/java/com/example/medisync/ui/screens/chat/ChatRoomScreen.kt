package com.example.medisync.ui.screens.chat

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.medisync.MediSyncApplication
import com.example.medisync.VideoCallActivity
import com.example.medisync.data.TokenManager
import com.example.medisync.data.local.ChatMessageEntity
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.viewmodels.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.medisync.ui.navigation.safePopBackStack


private val ChatBg         = Color(0xFFF2FAFF)
private val MyBubble       = Color(0xFFE5F5FF)
private val MyBubbleText   = Color(0xFF111B21)
private val OtherBubble    = Color.White
private val OtherBubbleText = Color(0xFF111B21)
private val TimeTextMine   = Color(0xFF667781)
private val TimeTextOther  = Color(0xFF667781)
private val HeaderBg       = Color(0xFF38BDF8)
private val HeaderText     = Color.White
private val InputBarBg     = Color.White
private val DateChipBg     = Color(0xFFD1E7F0)
private val DateChipText   = Color(0xFF1F4D6B)
private val ReadTick       = Color(0xFF53BDEB)
private val MediaBorder    = Color(0xFFE2E8F0)

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
    var imagePreviewUrl by remember { mutableStateOf<String?>(null) }
    var imageUrls by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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
    var pickedFile by remember { mutableStateOf<PickedChatFile?>(null) }
    var saveAsReport by remember { mutableStateOf(false) }
    var myRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        myRole = TokenManager.getRole(context)
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val type = context.contentResolver.getType(uri) ?: "application/octet-stream"
            pickedFile = PickedChatFile(
                uri = uri,
                name = getFileName(context, uri),
                type = type,
                size = getFileSize(context, uri)
            )
            saveAsReport = false
        }
    }

    val lastMessageId = messages.lastOrNull()?.clientTempId ?: messages.lastOrNull()?.id?.toString()
    LaunchedEffect(lastMessageId) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    pickedFile?.let { file ->
        AlertDialog(
            onDismissRequest = { pickedFile = null },
            title = { Text("Send file?") },
            text = {
                Column {
                    Text(file.name)

                    if (myRole == "doctor") {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = saveAsReport,
                                onCheckedChange = { saveAsReport = it }
                            )
                            Text("Also save as patient report")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatViewModel.sendFile(
                            context = context,
                            uri = file.uri,
                            fileName = file.name,
                            fileType = file.type,
                            fileSize = file.size,
                            saveAsReport = myRole == "doctor" && saveAsReport
                        )
                        pickedFile = null
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickedFile = null }) {
                    Text("Cancel")
                }
            }
        )
    }


    Scaffold(
        containerColor = ChatBg,
        topBar = {
            ChatTopBar(
                name = otherUserName,
                subtitle = if (uiState.isConnected) "Online" else "",
                photoUrl = photoUrl,
                onBack = { navController.safePopBackStack() },
                onJoin = {
                    val intent = Intent(context, VideoCallActivity::class.java).apply {
                        putExtra("roomId", roomId)
                    }
                    context.startActivity(intent)
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotBlank()) {
                        chatViewModel.sendMessage(trimmed)
                        inputText = ""
                    }
                },
                onAttachClick = {
                    filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                },
                focusRequester = focusRequester
            )
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

            items(reversedList, key = { it.clientTempId ?: it.id.toString() }) { msg ->
                val fileKey = msg.fileKey

                LaunchedEffect(fileKey) {
                    if (
                        msg.messageType == "image" &&
                        fileKey != null &&
                        imageUrls[fileKey] == null
                    ) {
                        chatViewModel.openFileCached(
                            context = context,
                            fileKey = fileKey,
                            fileName = msg.fileName ?: "image",
                            fileType = msg.fileType
                        ) { uri ->
                            imageUrls = imageUrls + (fileKey to uri.toString())
                        }
                    }
                }
                val isMine = msg.senderId == myUserId
                MessageBubble(
                    message = msg,
                    isMine = isMine,
                    imageUrl = msg.fileKey?.let { imageUrls[it] },
                    onVisible = {
                        if (!isMine && !msg.isRead) {
                            chatViewModel.markAsRead(msg.id)
                        }
                    },
                    onFileClick = { fileKey, fileType ->
                        chatViewModel.openFileCached(
                            context = context,
                            fileKey = fileKey,
                            fileName = msg.fileName ?: "File",
                            fileType = fileType
                        ) { uri ->
                            if (fileType?.startsWith("image/") == true) {
                                imagePreviewUrl = uri.toString()
                            } else {
                                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, fileType ?: "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(Intent.createChooser(openIntent, "Open file"))
                            }
                        }
                    }
                )
            }

            
            item { DateChip("Today") }
        }
    }
    imagePreviewUrl?.let { url ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { imagePreviewUrl = null },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = { imagePreviewUrl = null },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
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
    imageUrl: String?,
    onVisible: () -> Unit,
    onFileClick: (String, String?) -> Unit
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

    val isMediaMessage = message.messageType != "text"
    val bubbleShape = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 14.dp,
        bottomStart = if (isMine) 14.dp else 2.dp,
        bottomEnd = if (isMine) 2.dp else 14.dp
    )
    val bubbleBg = if (isMediaMessage) Color.White else if (isMine) MyBubble else OtherBubble
    val contentTextColor = if (isMine && !isMediaMessage) MyBubbleText else OtherBubbleText
    val timeTextColor = if (isMine && !isMediaMessage) TimeTextMine else TimeTextOther

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = if (message.messageType == "image") 230.dp else 246.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .then(
                        if (isMediaMessage) {
                            Modifier.border(0.6.dp, MediaBorder, bubbleShape)
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    if (message.messageType == "text") {
                        Text(
                            text = message.message ?: "",
                            fontSize = 14.5.sp,
                            color = contentTextColor,
                            lineHeight = 20.sp
                        )
                    } else if (message.messageType == "image") {
                        Column(
                            modifier = Modifier.clickable {
                                message.fileKey?.let { key ->
                                    onFileClick(key, message.fileType)
                                }
                            }
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = message.fileName ?: "Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    } else {
                        Text(
                            text = message.fileName ?: "File",
                            fontSize = 14.5.sp,
                            color = contentTextColor,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                message.fileKey?.let { key ->
                                    onFileClick(key, message.fileType)
                                }
                            }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = timeTextColor
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
    onSend: () -> Unit,
    onAttachClick: () -> Unit,
    focusRequester: FocusRequester
){
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
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 120.dp)
                    .focusRequester(focusRequester),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.5.sp),
                maxLines = 5
            )
            IconButton(onClick = onAttachClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = Color(0xFF8696A0),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (value.isNotBlank()) HeaderBg else Color(0xFFCFEFFF)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSend, modifier = Modifier.size(46.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private data class PickedChatFile(
    val uri: Uri,
    val name: String,
    val type: String,
    val size: Long?
)

private fun getFileName(context: android.content.Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) {
            return it.getString(nameIndex)
        }
    }
    return "file"
}

private fun getFileSize(context: android.content.Context, uri: Uri): Long? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        if (it.moveToFirst() && sizeIndex >= 0) {
            return it.getLong(sizeIndex)
        }
    }
    return null
}
