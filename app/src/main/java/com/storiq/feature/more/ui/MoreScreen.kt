package com.storiq.feature.more.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storiq.core.ui.theme.Color
import com.storiq.core.ui.theme.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filedownload
import androidx.compose.material.icons.filled.Swipe

@Composable
fun MoreScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSwipeClean: () -> Unit = {},
    onNavigateToFileExplorer: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToDeviceInfo: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("More", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
            ) {
                item {
                    SectionHeader(title = "Signature Features")
                }
                items(listOf(
                    MoreItem("Swipe Clean", "Rapidly review photos & videos", Icons.Filled.Swipe, Color.StorIQGreen),
                    MoreItem("File Explorer", "Browse and manage files", Icons.Filled.Folder, Color.StorIQBlue),
                )) { item ->
                    MoreItemCard(item = item, onClick = {
                        when (item.title) {
                            "Swipe Clean" -> onNavigateToSwipeClean()
                            "File Explorer" -> onNavigateToFileExplorer()
                            else -> Unit
                        }
                    })
                }

                item {
                    SectionHeader(title = "Analysis & History")
                }
                items(listOf(
                    MoreItem("Storage History", "Track storage usage over time", Icons.Filled.History, Color.StorIQPurple),
                    MoreItem("Downloads Analyzer", "Analyze downloaded files", Icons.Filedownload, Color.StorIQOrange),
                    MoreItem("Device Info", "Hardware & system information", Icons.Filled.Info, Color.StorIQTeal),
                )) { item ->
                    MoreItemCard(item = item, onClick = {
                        when (item.title) {
                            "Storage History" -> onNavigateToHistory()
                            else -> Unit
                        }
                    })
                }

                item {
                    SectionHeader(title = "Privacy & Settings")
                }
                items(listOf(
                    MoreItem("Privacy Dashboard", "Permission insights & controls", Icons.Filled.PrivacyTip, Color.StorIQRed),
                    MoreItem("Settings", "App preferences & configuration", Icons.Filled.Settings, Color.Gray),
                )) { item ->
                    MoreItemCard(item = item, onClick = {
                        when (item.title) {
                            "Privacy Dashboard" -> onNavigateToPrivacy()
                            "Settings" -> onNavigateToSettings()
                            else -> Unit
                        }
                    })
                }
            }
        }
    }
}

data class MoreItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = Typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun MoreItemCard(
    item: MoreItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = item.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.description,
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}