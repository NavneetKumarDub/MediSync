package com.example.medisync.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R

data class Speciality(
    val name: String,
    val imageRes: Int,
    val bgColor: Color
)

val specialityList = listOf(
    Speciality("General Physician", R.drawable.general_physician, Color(0xFFEDF7F1)),
    Speciality("Skin & Hair",       R.drawable.skin,              Color(0xFFFFF0F5)),
    Speciality("Women's Health",    R.drawable.women_health,      Color(0xFFFFE4EE)),
    Speciality("Dental Care",       R.drawable.dental,            Color(0xFFE8F4FF)),
    Speciality("Child Specialist",  R.drawable.child,             Color(0xFFFFF8E1)),
    Speciality("Ear Nose Throat",   R.drawable.ear,               Color(0xFFEDF7F1)),
    Speciality("Mental Wellness",   R.drawable.mental,            Color(0xFFEDE9FE)),
    Speciality("More",              R.drawable.more,              Color(0xFFF1F1F1)),
)

@Composable
fun FindDoctorSection(
    onSpecialityClick: (Speciality) -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Find a Doctor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = "for your health problem",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        // Grid using Column + Row instead of LazyVerticalGrid
        val rows = specialityList.chunked(4)
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { speciality ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (speciality.name == "More") {
                                MoreItem(
                                    speciality = speciality,
                                    onClick = onMoreClick
                                )
                            } else {
                                SpecialityItem(
                                    speciality = speciality,
                                    onClick = { onSpecialityClick(speciality) }
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
fun SpecialityItem(
    speciality: Speciality,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = speciality.bgColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = speciality.imageRes),
                    contentDescription = speciality.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = speciality.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A2E),
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun MoreItem(
    speciality: Speciality,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F1F1)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "20+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D6E40)
                    )
                    Text(
                        text = "more",
                        fontSize = 10.sp,
                        color = Color(0xFF0D6E40)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "More",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A2E),
            textAlign = TextAlign.Center
        )
    }
}