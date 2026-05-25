package com.example.medisync.ui.screens.patient

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.PatientRecordDto
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.components.SearchBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.ui.theme.natureGreen
import com.example.medisync.viewmodels.PatientRecordsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ScreenBg = Color.White
private val Accent = Color(0xFF2A9DF4)
private val TextDark = Color(0xFF111B21)
private val TextMuted = Color(0xFF6B7280)
private val ChipActiveBg = natureGreen.copy(alpha = 0.1f)
private val ChipActiveBorder = natureGreen.copy(alpha = 0.3f)
private val ChipIdleBg = Color(0xFFF3F4F6)
private val RecordFilters = listOf("All", "Images", "PDFs")

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PatientRecordsContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MediSyncApplication
    var token by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        token = TokenManager.getToken(context)
    }

    if (token == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    val viewModel: PatientRecordsViewModel = viewModel(
        factory = PatientRecordsViewModel.Factory(
            context = context,
            repository = app.patientRecordsRepository,
            token = token!!
        )
    )

    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(selectedTab) {
        if (selectedTab == 3) {
            viewModel.loadRecords()
        }
    }

    LaunchedEffect(state.records) {
        if (state.records.isNotEmpty()) {
            viewModel.cacheRecords(context)
        }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    var imagePreviewUrl by remember { mutableStateOf<String?>(null) }

    val filtered = remember(state.records, selectedFilter, search) {
        state.records.filter { record ->
            val matchesFilter = when (selectedFilter) {
                "Images" -> record.fileType?.startsWith("image/") == true
                "PDFs" -> record.fileType == "application/pdf"
                else -> true
            }

            matchesFilter && record.matchesRecordSearch(search)
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            BottomNavBar(
                navItems = NavItems.patient,
                selectedIndex = selectedTab,
                onItemSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Medical Records",
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                fontWeight = FontWeight.Bold,
                color = natureGreen
            )

            Spacer(Modifier.height(14.dp))

            SearchBar(
                value = search,
                onValueChange = { search = it },
                placeholder = "Search records",
                modifier = Modifier
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordFilterRow(
                    active = selectedFilter,
                    onSelect = { selectedFilter = it },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { viewModel.refreshRecords() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Accent
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Accent)
                }

                state.error != null -> Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )

                filtered.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No records found", color = Color.Gray)
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { record ->
                        val cachedUri = state.cachedRecordUris[record.fileKey]
                        RecordCard(record = record) {
                            fun openRecordUri(uriString: String) {
                                if (record.fileType?.startsWith("image/") == true) {
                                    imagePreviewUrl = uriString
                                } else {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(android.net.Uri.parse(uriString), record.fileType ?: "*/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open record"))
                                }
                            }

                            if (cachedUri != null) {
                                openRecordUri(cachedUri)
                            } else {
                                viewModel.openRecordCached(context, record) { uri ->
                                    openRecordUri(uri.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    imagePreviewUrl?.let { url ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            coil.compose.AsyncImage(
                model = url,
                contentDescription = "Record image",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun PatientRecordDto.matchesRecordSearch(query: String): Boolean {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) return true

    val fileKind = when {
        fileType?.startsWith("image/") == true -> "image images photo picture"
        fileType == "application/pdf" -> "pdf document"
        else -> "file record"
    }

    val searchableText = listOfNotNull(
        fileName,
        uploadedByName,
        fileType,
        fileKind,
        createdAt,
        formatRecordDate(createdAt),
        fileSize?.toString(),
        id.toString()
    ).joinToString(" ")

    return searchableText.contains(cleanQuery, ignoreCase = true)
}

@Composable
private fun RecordFilterRow(
    active: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RecordFilters.forEach { filter ->
            val isActive = active == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isActive) ChipActiveBg else ChipIdleBg)
                    .then(
                        if (isActive) {
                            Modifier.border(
                                width = 1.dp,
                                color = ChipActiveBorder,
                                shape = RoundedCornerShape(24.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) natureGreen else TextMuted
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RecordCard(record: PatientRecordDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(Accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        record.fileType?.startsWith("image/") == true -> Icons.Default.Image
                        record.fileType == "application/pdf" -> Icons.Default.PictureAsPdf
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = Accent
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(record.fileName, fontWeight = FontWeight.SemiBold, color = TextDark, maxLines = 1)
                Text("Uploaded by ${record.uploadedByName}", color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Text(formatRecordDate(record.createdAt), color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatRecordDate(value: String): String {
    return try {
        val instant = Instant.parse(value)
        DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    } catch (e: Exception) {
        value
    }
}
