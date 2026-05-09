package com.example.medisync.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medisync.data.TokenManager
import com.example.medisync.networks.DoctorSearchResult
import com.example.medisync.networks.RetrofitInstance
import com.example.medisync.networks.SearchFilters
import com.example.medisync.ui.components.SearchInputBar
import kotlinx.coroutines.delay

// ── Colors ────────────────────────────────────
private val ScreenBackground  = Color(0xFFF6F7F9)
private val CardBackground    = Color(0xFFFFFFFF)
private val DividerCol        = Color(0xFFE4E7EC)
private val TextPrimCol       = Color(0xFF111827)
private val TextSecondCol     = Color(0xFF6B7280)
private val TextHintCol       = Color(0xFF9CA3AF)
private val GreenCol          = Color(0xFF27AE7A)
private val GreenLightCol     = Color(0xFFE6F7F0)
private val GreenTextCol      = Color(0xFF1A8C61)
private val ChipBg            = Color(0xFFEEF0F3)
private val OnlineChipBg      = Color(0xFFE6F7F0)
private val OfflineChipBg     = Color(0xFFFFF3E0)
private val ErrorBannerBg     = Color(0xFFFFF3E0)
private val ErrorBannerText   = Color(0xFFE65100)
private val FilterActiveColor = Color(0xFF27AE7A)
private val FilterIdleColor   = Color(0xFF6B7280)
private val AvatarBg          = Color(0xFFE6F7F0)
private val AvatarText        = Color(0xFF1A8C61)
private val OfflineText       = Color(0xFFB45309)

// ── Sample fallback ────────────────────────────
val sampleDoctors = listOf(
    DoctorSearchResult(1,  "Dr. Ravi Sharma",   "General Physician",  8,  500.0,  "both",    "English, Hindi",          "Bangalore"),
    DoctorSearchResult(2,  "Dr. Anjali Sharma", "Cardiology",         12, 800.0,  "online",  "English, Hindi, Kannada", "Bangalore", "Experienced cardiologist"),
    DoctorSearchResult(3,  "Dr. Rakesh Gupta",  "Dermatology",        5,  600.0,  "both",    "English, Hindi",          "Mumbai"),
    DoctorSearchResult(4,  "Dr. Priya Mehta",   "Neurology",          10, 1000.0, "offline", "English",                 "Delhi"),
    DoctorSearchResult(5,  "Dr. Sunita Rao",    "Endocrinology",      7,  700.0,  "online",  "English, Kannada",        "Bangalore"),
    DoctorSearchResult(6,  "Dr. Arjun Menon",   "Gastroenterology",   6,  650.0,  "both",    "English, Malayalam",      "Chennai"),
    DoctorSearchResult(7,  "Dr. Kavitha Reddy", "Nutrition",          4,  400.0,  "online",  "English, Telugu",         "Hyderabad"),
    DoctorSearchResult(8,  "Dr. Vikram Nair",   "Ophthalmology",      9,  750.0,  "offline", "English, Malayalam",      "Kochi"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    var query          by remember { mutableStateOf("") }
    var filters        by remember { mutableStateOf(SearchFilters()) }
    var results        by remember { mutableStateOf(sampleDoctors) }
    var showFilters    by remember { mutableStateOf(false) }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val filtersActive  = filters != SearchFilters()

    LaunchedEffect(Unit) {
        try {
            delay(100)
            focusRequester.requestFocus()
        } catch (e: Exception) { }
    }

    LaunchedEffect(query, filters) {
        isLoading    = true
        errorMessage = null
        delay(500)
        try {
            val token = "Bearer ${TokenManager.getToken(context) ?: ""}"

            val response = RetrofitInstance.api.searchDoctors(
                token,
                query            = query,
                consultationType = filters.consultationType,
                minExperience    = filters.minExperience,
                minFee           = filters.minFee,
                maxFee           = filters.maxFee,
                languages        = filters.languages
            )
            results = response.doctors
        } catch (e: Exception) {
            errorMessage = "Could not reach server. Showing sample data."
            results = sampleDoctors.filter { doctor ->
                val matchesQuery = query.isEmpty() ||
                        doctor.doctorName.contains(query, ignoreCase = true) ||
                        doctor.speciality?.contains(query, ignoreCase = true) == true
                val matchesType  = filters.consultationType == null ||
                        doctor.consultationType == filters.consultationType ||
                        doctor.consultationType == "both"
                val matchesExp   = filters.minExperience == null ||
                        (doctor.experienceYears ?: 0) >= filters.minExperience!!
                val matchesMin   = filters.minFee == null ||
                        (doctor.consultationFee ?: 0.0) >= filters.minFee!!
                val matchesMax   = filters.maxFee == null ||
                        (doctor.consultationFee ?: 0.0) <= filters.maxFee!!
                val matchesLang  = filters.languages == null ||
                        doctor.languages?.contains(filters.languages!!, ignoreCase = true) == true
                matchesQuery && matchesType && matchesExp &&
                        matchesMin && matchesMax && matchesLang
            }
        }
        isLoading = false
    }

    // ── Filter bottom sheet — outside Scaffold ─
    if (showFilters) {
        FilterBottomSheet(
            currentFilters = filters,
            onApply        = { filters = it; showFilters = false },
            onDismiss      = { showFilters = false }
        )
    }

    // ── Scaffold with topBar ───────────────────
    // SearchInputBar in topBar works because
    // we use BasicTextField inside — not TextField
    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SearchInputBar(
                    query          = query,
                    onQueryChange  = { query = it },
                    onBackClick    = { navController.popBackStack() },
                    focusRequester = focusRequester
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScreenBackground)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text     = if (!isLoading && results.isNotEmpty())
                            "${results.size} doctors found" else "",
                        fontSize = 13.sp,
                        color    = TextSecondCol
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (filtersActive) GreenLightCol else ChipBg)
                            .clickable { showFilters = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint               = if (filtersActive) FilterActiveColor else FilterIdleColor,
                                modifier           = Modifier.size(16.dp)
                            )
                            Text(
                                text       = "Filters",
                                fontSize   = 13.sp,
                                color      = if (filtersActive) FilterActiveColor else FilterIdleColor,
                                fontWeight = if (filtersActive) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (filtersActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(GreenCol)
                                )
                            }
                        }
                    }
                }

                // Clear filters
                if (filtersActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 6.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text     = "Filters applied —",
                            fontSize = 12.sp,
                            color    = TextSecondCol
                        )
                        Text(
                            text       = "Clear all",
                            fontSize   = 12.sp,
                            color      = GreenCol,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.clickable { filters = SearchFilters() }
                        )
                    }
                }

                HorizontalDivider(color = DividerCol)
            }
        }
    ) { paddingValues ->
        // ── Content ───────────────────────────
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenCol)
                }
            }

            results.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ChipBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint               = TextHintCol,
                                modifier           = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text       = "No doctors found",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimCol
                        )
                        Text(
                            text     = "Try different search or adjust filters",
                            fontSize = 13.sp,
                            color    = TextHintCol
                        )
                    }
                }
            }

            else -> {
                // ── LazyColumn directly in Scaffold content ──
                // This gives LazyColumn proper bounded constraints ✅
                LazyColumn(
                    modifier       = Modifier.padding(paddingValues),
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 12.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    errorMessage?.let { msg ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ErrorBannerBg)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint               = ErrorBannerText,
                                    modifier           = Modifier.size(16.dp)
                                )
                                Text(
                                    text     = msg,
                                    fontSize = 12.sp,
                                    color    = ErrorBannerText
                                )
                            }
                        }
                    }

                    items(results, key = { it.doctorId }) { doctor ->
                        DoctorCard(
                            doctor  = doctor,
                            onClick = {
                                navController.navigate("doctorProfile/${doctor.doctorId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorCard(
    doctor : DoctorSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.Top
        ) {
            // ── Avatar ────────────────────────
            Box(
                modifier         = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = (doctor.doctorName ?: "?")
                        .split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                        .ifEmpty { "?" },
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AvatarText
                )
            }

            // ── Info ──────────────────────────
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name + city
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = doctor.doctorName ?: "Unknown Doctor",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimCol,
                        modifier   = Modifier.weight(1f)
                    )
                    if (!doctor.city.isNullOrEmpty()) {
                        Text(
                            text     = doctor.city,
                            fontSize = 11.sp,
                            color    = TextHintCol
                        )
                    }
                }

                // Speciality
                if (!doctor.speciality.isNullOrEmpty()) {
                    Text(
                        text     = doctor.speciality,
                        fontSize = 13.sp,
                        color    = TextSecondCol
                    )
                }

                // Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier              = Modifier.padding(top = 4.dp)
                ) {
                    doctor.experienceYears?.let {
                        InfoChip(text = "$it yrs exp")
                    }
                    if (!doctor.consultationType.isNullOrEmpty()) {
                        InfoChip(
                            text      = doctor.consultationType
                                .replaceFirstChar { it.uppercase() },
                            textColor = when (doctor.consultationType) {
                                "online"  -> GreenTextCol
                                "offline" -> OfflineText
                                else      -> TextSecondCol
                            },
                            bgColor   = when (doctor.consultationType) {
                                "online"  -> OnlineChipBg
                                "offline" -> OfflineChipBg
                                else      -> ChipBg
                            }
                        )
                    }
                }

                HorizontalDivider(
                    modifier  = Modifier.padding(top = 8.dp),
                    color     = DividerCol,
                    thickness = 0.8.dp
                )

                // Fee + Book
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    doctor.consultationFee?.let {
                        Text(
                            text       = "₹${it.toInt()} per consult",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = GreenCol
                        )
                    } ?: Text(
                        text     = "Fee not listed",
                        fontSize = 13.sp,
                        color    = TextHintCol
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenCol)
                            .clickable { onClick() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text       = "Book",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── Info Chip ──────────────────────────────────
@Composable
fun InfoChip(
    text     : String,
    bgColor  : Color = ChipBg,
    textColor: Color = TextSecondCol
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text       = text,
            fontSize   = 11.sp,
            color      = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Filter Bottom Sheet ────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilters: SearchFilters,
    onApply       : (SearchFilters) -> Unit,
    onDismiss     : () -> Unit
) {
    var consultationType by remember { mutableStateOf(currentFilters.consultationType) }
    var minExperience    by remember { mutableStateOf(currentFilters.minExperience?.toString() ?: "") }
    var minFee           by remember { mutableStateOf(currentFilters.minFee?.toString() ?: "") }
    var maxFee           by remember { mutableStateOf(currentFilters.maxFee?.toString() ?: "") }
    var languages        by remember { mutableStateOf(currentFilters.languages ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = CardBackground,
        scrimColor       = Color.Black.copy(alpha = 0.4f),
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Filters",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimCol
                )
                TextButton(onClick = {
                    consultationType = null
                    minExperience    = ""
                    minFee           = ""
                    maxFee           = ""
                    languages        = ""
                }) {
                    Text(text = "Reset all", color = GreenCol)
                }
            }

            HorizontalDivider(color = DividerCol)

            FilterSection(title = "Consultation Type") {
                listOf("online", "offline", "both").forEach { type ->
                    FilterChipItem(
                        label      = type.replaceFirstChar { it.uppercase() },
                        isSelected = consultationType == type,
                        onClick    = {
                            consultationType =
                                if (consultationType == type) null else type
                        }
                    )
                }
            }

            FilterSection(title = "Minimum Experience") {
                listOf("2", "5", "10", "15").forEach { exp ->
                    FilterChipItem(
                        label      = "$exp+ yrs",
                        isSelected = minExperience == exp,
                        onClick    = {
                            minExperience = if (minExperience == exp) "" else exp
                        }
                    )
                }
            }

            FilterSection(title = "Fee Range (₹)") {
                listOf(
                    "0"    to "500",
                    "500"  to "1000",
                    "1000" to "2000"
                ).forEach { (min, max) ->
                    FilterChipItem(
                        label      = "₹$min–$max",
                        isSelected = minFee == min && maxFee == max,
                        onClick    = {
                            if (minFee == min && maxFee == max) {
                                minFee = ""; maxFee = ""
                            } else {
                                minFee = min; maxFee = max
                            }
                        }
                    )
                }
            }

            FilterSection(title = "Language") {
                listOf("Hindi", "English", "Kannada", "Tamil").forEach { lang ->
                    FilterChipItem(
                        label      = lang,
                        isSelected = languages == lang,
                        onClick    = {
                            languages = if (languages == lang) "" else lang
                        }
                    )
                }
            }

            Button(
                onClick = {
                    onApply(
                        SearchFilters(
                            consultationType = consultationType,
                            minExperience    = minExperience.toIntOrNull(),
                            minFee           = minFee.toIntOrNull(),
                            maxFee           = maxFee.toIntOrNull(),
                            languages        = languages.ifEmpty { null }
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenCol)
            ) {
                Text(
                    text       = "Apply Filters",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Filter Section ─────────────────────────────
@Composable
fun FilterSection(
    title  : String,
    content: @Composable RowScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text       = title,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimCol
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content               = content
        )
    }
}

// ── Filter Chip ────────────────────────────────
@Composable
fun FilterChipItem(
    label     : String,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) GreenLightCol else ChipBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text       = label,
            fontSize   = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isSelected) GreenTextCol else TextSecondCol
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() {
    SearchScreen(navController = rememberNavController())
}