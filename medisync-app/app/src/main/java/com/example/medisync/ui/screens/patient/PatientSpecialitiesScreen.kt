package com.example.medisync.ui.screens.patient

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.medisync.ui.components.SearchBar
import com.example.medisync.ui.components.Speciality
import com.example.medisync.ui.navigation.safePopBackStack

private val SpecialityScreenBg = Color.White
private val SpecialityText = Color(0xFF2F333A)
private val SpecialityDivider = Color(0xFFECEFF3)

val allPatientSpecialities = listOf(
    Speciality("COVID", "newIcons/1_COVID.svg", Color(0xFFE0F2FE)),
    Speciality("Skin & Hair", "newIcons/2_Skin_Hair.svg", Color(0xFFE0F2FE)),
    Speciality("Women's Health", "newIcons/3_Womens_Health.svg", Color(0xFFE0F2FE)),
    Speciality("General Physician", "newIcons/4_General_Physician.svg", Color(0xFFE0F2FE)),
    Speciality("Dental Care", "newIcons/5_Dental_Care.svg", Color(0xFFE0F2FE)),
    Speciality("Bones & Joints", "newIcons/6_Bones_Joints.svg", Color(0xFFE0F2FE)),
    Speciality("Mental Wellness", "newIcons/7_Mental_Wellness.svg", Color(0xFFE0F2FE)),
    Speciality("Ear Nose Throat", "newIcons/8_ENT.svg", Color(0xFFE0F2FE)),
    Speciality("Sexual Health", "newIcons/9_Sexual_Health.svg", Color(0xFFE0F2FE)),
    Speciality("Child Specialist", "newIcons/10_Child_Specialist.svg", Color(0xFFE0F2FE)),
    Speciality("Homeopathy", "newIcons/11_Homeopathy.svg", Color(0xFFE0F2FE)),
    Speciality("Digestive Issues", "newIcons/12_Digestive_Issues.svg", Color(0xFFE0F2FE)),
    Speciality("Eye Specialist", "newIcons/13_Eye_Specialist.svg", Color(0xFFE0F2FE)),
    Speciality("Heart", "newIcons/14_Heart.svg", Color(0xFFE0F2FE)),
    Speciality("Physiotherapy", "newIcons/15_Physiotherapy.svg", Color(0xFFE0F2FE)),
    Speciality("Brain & Nerves", "newIcons/16_Brain_Nerves.svg", Color(0xFFE0F2FE)),
    Speciality("Lungs", "newIcons/17_Lungs.svg", Color(0xFFE0F2FE)),
    Speciality("Kidney Issues", "newIcons/18_Kidney_Issues.svg", Color(0xFFE0F2FE)),
    Speciality("General Surgery", "newIcons/19_General_Surgery.svg", Color(0xFFE0F2FE)),
    Speciality("Diabetes", "newIcons/20_Diabetes.svg", Color(0xFFE0F2FE)),
    Speciality("Ayurveda", "newIcons/21_Ayurveda.svg", Color(0xFFE0F2FE)),
    Speciality("Cancer", "newIcons/22_Cancer.svg", Color(0xFFE0F2FE)),
    Speciality("Urinary Issues", "newIcons/23_Urinary_Issues.svg", Color(0xFFE0F2FE)),
    Speciality("Veterinary", "newIcons/24_Veterinary.svg", Color(0xFFE0F2FE)),
    Speciality("Diet & Nutrition", "newIcons/25_Diet_Nutrition.svg", Color(0xFFE0F2FE))
)

@Composable
fun PatientSpecialitiesScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    val filteredSpecialities = remember(query) {
        if (query.isBlank()) {
            allPatientSpecialities
        } else {
            allPatientSpecialities.filter {
                it.name.contains(query.trim(), ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpecialityScreenBg)
            .statusBarsPadding()
    ) {
        Spacer(Modifier.height(20.dp))

        IconButton(
            onClick = { navController.safePopBackStack() },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Choose from top specialities",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SpecialityText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )

        SearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search Symptoms / Specialities",
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp)
        ) {
            items(filteredSpecialities, key = { it.name }) { speciality ->
                SpecialityListRow(
                    speciality = speciality,
                    onClick = {
                        navController.navigate("search?speciality=${Uri.encode(speciality.name)}")
                    }
                )
            }
        }
    }
}

@Composable
private fun SpecialityListRow(
    speciality: Speciality,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val svgImageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(speciality.bgColor),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "file:///android_asset/${speciality.iconAsset}",
                    contentDescription = speciality.name,
                    imageLoader = svgImageLoader,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = speciality.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = SpecialityText
            )
        }

        HorizontalDivider(
            color = SpecialityDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(start = 62.dp)
        )
    }
}
