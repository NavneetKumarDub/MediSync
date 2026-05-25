package com.example.medisync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder

private val TileGlass = Color(0xCCF2FAFF)
private val TileBlue = Color(0xFF2F95F6)
private val MoreTileBg = Color(0xFFF6FAFE)

data class Speciality(
    val name: String,
    val iconAsset: String,
    val bgColor: Color
)

val specialityList = listOf(
    Speciality("General Physician", "newIcons/4_General_Physician.svg", Color(0xFFE0F2FE)),
    Speciality("Skin & Hair", "newIcons/2_Skin_Hair.svg", Color(0xFFE0F2FE)),
    Speciality("Child Specialist", "newIcons/10_Child_Specialist.svg", Color(0xFFE0F2FE)),
    Speciality("Dental Care", "newIcons/5_Dental_Care.svg", Color(0xFFE0F2FE)),
    Speciality("Women's Health", "newIcons/3_Womens_Health.svg", Color(0xFFE0F2FE)),
    Speciality("Mental Wellness", "newIcons/7_Mental_Wellness.svg", Color(0xFFE0F2FE)),
    Speciality("Ear Nose Throat", "newIcons/8_ENT.svg", Color(0xFFE0F2FE)),
    Speciality("More", "", MoreTileBg),
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
    val context = LocalContext.current
    val svgImageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0x1A03A9F4),
                    spotColor = Color(0x2203A9F4)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileGlass)
                    .padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "file:///android_asset/${speciality.iconAsset}",
                    contentDescription = speciality.name,
                    imageLoader = svgImageLoader,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(56.dp)
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
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0x1A03A9F4),
                    spotColor = Color(0x2203A9F4)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MoreTileBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MoreTileBg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "20+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TileBlue
                    )
                    Text(
                        text = "more",
                        fontSize = 10.sp,
                        color = TileBlue
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
