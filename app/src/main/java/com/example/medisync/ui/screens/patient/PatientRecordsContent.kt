package com.example.medisync.ui.screens.patient


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.components.BottomNavBar
import com.example.medisync.ui.navigation.NavItems

// Make sure to import your BottomNavBar and NavItems here!

@Composable
fun RecordsContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                navItems = NavItems.patient,
                selectedIndex = selectedTab,
                onItemSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This is the Records Screen",
                fontSize = 20.sp
            )
        }
    }
}