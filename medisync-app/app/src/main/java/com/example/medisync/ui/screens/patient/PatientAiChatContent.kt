package com.example.medisync.ui.screens.patient

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.PatientRecordDto
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.viewmodels.PatientAiChatMessage
import com.example.medisync.viewmodels.PatientAiChatViewModel
import kotlinx.coroutines.launch
import com.example.medisync.ui.navigation.safePopBackStack

private val AiScreenBg = Color(0xFFFBFCFE)
private val AiBubbleBg = Color(0xFFF1F6FA)
private val UserBubbleBg = natureGreen
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF667085)

private data class PendingAiAttachment(
    val uri: String,
    val name: String,
    val mimeType: String,
    val savedRecord: PatientRecordDto? = null
)

@Composable
fun PatientAiChatScreen(
    navController: NavController
) {
    PatientAiChatContent(
        onBackClick = { navController.safePopBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAiChatContent(
    onBackClick: () -> Unit = {},
    viewModel: PatientAiChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showRecordSheet by remember { mutableStateOf(false) }
    var patientRecords by remember { mutableStateOf<List<PatientRecordDto>>(emptyList()) }
    var recordsError by remember { mutableStateOf<String?>(null) }
    var selectedAttachment by remember { mutableStateOf<PendingAiAttachment?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        selectedAttachment = PendingAiAttachment(
            uri = uri.toString(),
            name = "Selected image",
            mimeType = "image/*"
        )
        showAttachSheet = false
    }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        selectedAttachment = PendingAiAttachment(
            uri = uri.toString(),
            name = "Selected PDF",
            mimeType = "application/pdf"
        )
        showAttachSheet = false
    }

    LaunchedEffect(showRecordSheet) {
        if (!showRecordSheet) return@LaunchedEffect
        runCatching {
            val token = TokenManager.getToken(context) ?: error("Please login again")
            val response = RetrofitInstance.api.getPatientRecords("Bearer $token")
            if (!response.isSuccessful || response.body() == null) {
                error("Failed to load records")
            }
            patientRecords = response.body()!!.records
            recordsError = null
        }.onFailure { error ->
            recordsError = error.message ?: "Failed to load records"
        }
    }

    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            AttachmentSheetContent(
                onChooseImage = { imagePickerLauncher.launch("image/*") },
                onChoosePdf = { pdfPickerLauncher.launch("application/pdf") },
                onUseSavedRecord = {
                    showAttachSheet = false
                    showRecordSheet = true
                },
                onClose = { showAttachSheet = false }
            )
        }
    }

    if (showRecordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecordSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            SavedRecordSheetContent(
                records = patientRecords,
                error = recordsError,
                onRecordClick = { record ->
                    scope.launch {
                        runCatching {
                            val token = TokenManager.getToken(context) ?: error("Please login again")
                            val response = RetrofitInstance.api.getChatFileViewUrl(
                                token = "Bearer $token",
                                key = record.fileKey
                            )
                            if (!response.isSuccessful || response.body() == null) {
                                error("Failed to open record")
                            }
                            selectedAttachment = PendingAiAttachment(
                                uri = response.body()!!.viewUrl,
                                name = record.fileName,
                                mimeType = record.fileType ?: "application/pdf",
                                savedRecord = record
                            )
                            showRecordSheet = false
                        }.onFailure { error ->
                            recordsError = error.message ?: "Failed to use record"
                        }
                    }
                },
                onClose = { showRecordSheet = false }
            )
        }
    }

    Scaffold(
        containerColor = AiScreenBg,
        topBar = {
            AiChatTopBar(
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            AiChatInputBar(
                input = input,
                onInputChange = { input = it },
                onAttachClick = { showAttachSheet = true },
                isSending = viewModel.isSending,
                selectedAttachment = selectedAttachment,
                onRemoveSelectedAttachment = { selectedAttachment = null },
                onSendClick = {
                    val attachment = selectedAttachment
                    when {
                        attachment?.mimeType?.startsWith("image/") == true -> {
                            if (attachment.savedRecord != null) {
                                viewModel.sendSavedRecordMessage(
                                    record = attachment.savedRecord,
                                    viewUrl = attachment.uri,
                                    prompt = input
                                )
                            } else {
                                viewModel.sendImageMessage(
                                    context = context,
                                    uri = android.net.Uri.parse(attachment.uri),
                                    prompt = input
                                )
                            }
                            selectedAttachment = null
                            input = ""
                        }
                        attachment?.mimeType == "application/pdf" -> {
                            if (attachment.savedRecord != null) {
                                viewModel.sendSavedRecordMessage(
                                    record = attachment.savedRecord,
                                    viewUrl = attachment.uri,
                                    prompt = input
                                )
                            } else {
                                viewModel.sendPdfMessage(
                                    context = context,
                                    uri = android.net.Uri.parse(attachment.uri),
                                    prompt = input
                                )
                            }
                            selectedAttachment = null
                            input = ""
                        }
                        else -> {
                            viewModel.sendMessage(input)
                            input = ""
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(viewModel.messages, key = { it.id }) { message ->
                AiMessageBubble(message = message)
            }

            item {
                AnimatedVisibility(visible = viewModel.messages.size <= 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        QuickActionRow(
                            icon = Icons.Default.Image,
                            title = "Analyze image or report",
                            subtitle = "Upload support will connect here"
                        )
                        QuickActionRow(
                            icon = Icons.Default.Edit,
                            title = "Ask health question",
                            subtitle = "Symptoms, medicine, diet, follow-up"
                        )
                        QuickActionRow(
                            icon = Icons.Default.Search,
                            title = "Find health info",
                            subtitle = "General guidance with safety checks"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChatTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircleIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick
            )
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "AI Chat",
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureGreen
                )
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun AiMessageBubble(message: PatientAiChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (message.isUser) 20.dp else 6.dp,
                        bottomEnd = if (message.isUser) 6.dp else 20.dp
                    )
                )
                .background(
                    when {
                        message.imageUri != null -> Color.Transparent
                        message.isUser -> UserBubbleBg
                        else -> AiBubbleBg
                    }
                )
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            message.imageUri?.let { imageUri ->
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                if (message.text.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (message.fileName != null) {
                FileBubblePreview(
                    fileName = message.fileName,
                    fileType = message.fileType
                )
                if (message.text.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (message.text.isNotBlank()) {
                Text(
                    text = message.text,
                    modifier = if (message.imageUri != null) {
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(UserBubbleBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    } else {
                        Modifier
                    },
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp,
                    fontWeight = if (message.isLoading) FontWeight.Medium else FontWeight.Normal,
                    color = if (message.isUser) Color.White else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(26.dp)
        )
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AiChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onAttachClick: () -> Unit,
    isSending: Boolean,
    selectedAttachment: PendingAiAttachment?,
    onRemoveSelectedAttachment: () -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(AiScreenBg)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
        ) {
            selectedAttachment?.let { attachment ->
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 8.dp)
                        .size(width = if (attachment.mimeType.startsWith("image/")) 104.dp else 210.dp, height = 82.dp)
                ) {
                    if (attachment.mimeType.startsWith("image/")) {
                        AsyncImage(
                            model = attachment.uri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        FileBubblePreview(
                            fileName = attachment.name,
                            fileType = attachment.mimeType,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(5.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable(onClick = onRemoveSelectedAttachment),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove image",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAttachClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add file",
                        tint = TextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 28.dp)
                        .padding(horizontal = 6.dp),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 17.sp
                    ),
                    cursorBrush = SolidColor(natureGreen),
                    maxLines = 4,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (input.isBlank()) {
                                Text(
                                    text = if (selectedAttachment == null) "Ask MediSync AI" else "Ask about this file",
                                    color = Color(0xFF98A2B3),
                                    fontSize = 17.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                val canSend = (input.isNotBlank() || selectedAttachment != null) && !isSending
                IconButton(
                    onClick = onSendClick,
                    enabled = canSend
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (canSend) natureGreen else Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (canSend) Color.White else Color(0xFF9CA3AF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentSheetContent(
    onChooseImage: () -> Unit,
    onChoosePdf: () -> Unit,
    onUseSavedRecord: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Share with AI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Text(
            text = "Choose an image and MediSync AI will analyze it with your message.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(Modifier.height(18.dp))

        AttachmentOption(
            icon = Icons.Default.Image,
            title = "Choose image",
            subtitle = "Analyze image from gallery",
            onClick = onChooseImage
        )
        AttachmentOption(
            icon = Icons.Default.PictureAsPdf,
            title = "Upload PDF",
            subtitle = "Send lab report or document",
            onClick = onChoosePdf
        )
        AttachmentOption(
            icon = Icons.Default.Article,
            title = "Use saved record",
            subtitle = "Pick from medical records",
            onClick = onUseSavedRecord
        )

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SavedRecordSheetContent(
    records: List<PatientRecordDto>,
    error: String?,
    onRecordClick: (PatientRecordDto) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved records",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        if (error != null) {
            Text(error, color = Color(0xFFDC2626), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
        }

        if (records.isEmpty() && error == null) {
            Text("No saved records found", color = TextSecondary, fontSize = 14.sp)
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    AttachmentOption(
                        icon = if (record.fileType?.startsWith("image/") == true) Icons.Default.Image else Icons.Default.PictureAsPdf,
                        title = record.fileName,
                        subtitle = "Shared by ${record.uploadedByName}",
                        onClick = { onRecordClick(record) }
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun FileBubblePreview(
    fileName: String,
    fileType: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(natureGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (fileType == "application/pdf") Icons.Default.PictureAsPdf else Icons.Default.Description,
                contentDescription = null,
                tint = natureGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                color = TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Text(
                text = if (fileType == "application/pdf") "PDF document" else "Medical file",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(natureGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = natureGreen)
        }
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PatientAiChatPreview() {
    PatientAiChatScreen(navController = rememberNavController())
}
