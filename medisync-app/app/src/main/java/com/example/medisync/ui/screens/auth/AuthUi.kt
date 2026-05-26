package com.example.medisync.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.ui.theme.natureGreen

val AuthBg = Color.White
val AuthTextPrimary = Color(0xFF111827)
val AuthTextSecondary = Color(0xFF64748B)
val AuthBorder = Color(0xFFE2E8F0)
val AuthFieldBg = Color(0xFFF8FAFC)

@Composable
fun AuthScreenFrame(
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            natureGreen.copy(alpha = 0.30f),
                            natureGreen.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        if (showBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 16.dp, top = 42.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AuthTextPrimary
                )
            }
        }

        content()
    }
}

@Composable
fun AuthLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 74.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(24.dp))
            .background(natureGreen),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_medisync_mark),
            contentDescription = "MediSync",
            modifier = Modifier.size(size * 0.70f)
        )
    }
}

@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showLogo: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showLogo) {
            AuthLogo()
            Spacer(Modifier.height(20.dp))
        }
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AuthTextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = AuthTextSecondary
        )
    }
}

@Composable
fun authTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = natureGreen,
        unfocusedBorderColor = AuthBorder,
        cursorColor = natureGreen,
        focusedTextColor = AuthTextPrimary,
        unfocusedTextColor = AuthTextPrimary,
        focusedContainerColor = AuthFieldBg,
        unfocusedContainerColor = AuthFieldBg
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = natureGreen,
            disabledContainerColor = natureGreen.copy(alpha = 0.35f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            if (trailing != null && !loading) {
                Spacer(Modifier.size(8.dp))
                trailing()
            }
        }
    }
}
