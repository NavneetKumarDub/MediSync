package com.example.medisync.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R

@Composable
fun ConsultationCard(
    onPhysicalClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        Card(
            modifier = Modifier
                .weight(1f)
                .height(160.dp)
                .clickable { onPhysicalClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 12.dp)
                        .fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = "Physical Appointment",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "At Hospital",
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }

                
                Image(
                    painter = painterResource(
                        id = R.drawable.clinic
                        
                    ),
                    contentDescription = "Physical Appointment",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .width(105.dp)
                        .height(125.dp)
                )
            }
        }

        
        Card(
            modifier = Modifier
                .weight(1f)
                .height(160.dp)
                .clickable { onVideoClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 12.dp)
                        .fillMaxWidth(0.6f)
                ) {
                    Text(
                        text = "Instant Video Consult",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Connect in 5 sec",
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }

                
                Image(
                    painter = painterResource(
                        id = R.drawable.onlinedoctor
                        
                    ),
                    contentDescription = "Video Consult",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .width(95.dp)
                        .height(125.dp)
                )
            }
        }
    }
}