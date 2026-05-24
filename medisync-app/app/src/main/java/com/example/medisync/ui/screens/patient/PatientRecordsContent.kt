package com.example.medisync.ui.screens.patient

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medisync.MediSyncApplication
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.PatientRecordDto
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems
import com.example.medisync.viewmodels.PatientRecordsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ScreenBg = Color(0xFFE7F0F4)
private val Accent = Color(0xFF2A9DF4)
private val TextDark = Color(0xFF111B21)

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
            repository = app.patientRecordsRepository,
            token = token!!
        )
    )

    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }

    val filtered = state.records.filter { record ->
        val matchesSearch = record.fileName.contains(search, ignoreCase = true) ||
                record.uploadedByName.contains(search, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Images" -> record.fileType?.startsWith("image/") == true
            "PDFs" -> record.fileType == "application/pdf"
            else -> true
        }

        matchesSearch && matchesFilter
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
                .padding(16.dp)
        ) {
            Text(
                text = "Medical Records",
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search records") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Images", "PDFs").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

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
                        RecordCard(record = record) {
                            viewModel.openRecord(record.fileKey) { url ->
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(url.toUri(), record.fileType ?: "*/*")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(Intent.createChooser(intent, "Open record"))
                            }
                        }
                    }
                }
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