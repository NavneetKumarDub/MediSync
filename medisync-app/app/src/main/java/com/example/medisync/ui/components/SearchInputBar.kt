package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.theme.natureGreen


private val IconTint      = Color(0xFFFFFFFF)
private val IconBg        = Color(0x33FFFFFF)
private val SearchFieldBg = Color(0xFFFFFFFF)
private val HintColor     = Color(0xFF9CA3AF)
private val TextColor     = Color(0xFF111827)

@Composable
fun SearchInputBar(
    query         : String           = "",
    onQueryChange : (String) -> Unit = {},
    onBackClick   : () -> Unit,
    focusRequester: FocusRequester,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shape = RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd   = 24.dp
                )
                clip = true
            }
            .background(natureGreen)
            .statusBarsPadding()
            .padding(
                start  = 16.dp,
                end    = 16.dp,
                top    = 16.dp,
                bottom = 24.dp
            )
    ) {
        
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(IconBg),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick  = onBackClick,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = IconTint,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text       = "Find a Doctor",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = IconTint
            )
        }

        Spacer(Modifier.height(16.dp))

        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SearchFieldBg)
                .height(48.dp)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = null,
                tint               = HintColor,
                modifier           = Modifier.size(20.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
            ) {
                
                if (query.isEmpty()) {
                    Text(
                        text     = "Search doctors, specialities...",
                        fontSize = 14.sp,
                        color    = HintColor
                    )
                }
                
                BasicTextField(
                    value         = query,
                    onValueChange = onQueryChange,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle     = TextStyle(
                        fontSize = 14.sp,
                        color    = TextColor
                    ),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    )
                )
            }

            
            if (query.isNotEmpty()) {
                IconButton(
                    onClick  = { onQueryChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint               = HintColor,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}