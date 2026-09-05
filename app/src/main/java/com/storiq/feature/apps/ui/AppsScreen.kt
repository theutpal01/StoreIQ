package com.storiq.feature.apps.ui

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
import androidx.compose.material.icons.filedownload
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info

@Composable
fun AppsScreen() {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Apps", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    actions = {
                        androidx.compose.material3.IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Filled.Sort,
                                contentDescription = "Sort",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }
                        androidx.compose.material3.IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
                // Summary card
                item {
                    AppSummaryCard(
                        totalApps = 127,
                        userApps = 89,
                        totalSize = "24.2 GB",
                        unusedApps = 7
                    )
                }

                // Unused apps section
                item {
                    SectionHeader(title = "Unused Apps (90+ days)", actionText = "View All", onActionClick = {})
                }
                items(listOf(
                    UnusedAppItem("Game XYZ", "com.example.game", "3.8 GB", "143 days ago", Icons.Filled.Android),
                    UnusedAppItem("Old Photo Editor", "com.example.photo", "1.2 GB", "210 days ago", Icons.Filled.Image),
                    UnusedAppItem("Unused Utility", "com.example.utility", "542 MB", "95 days ago", Icons.Filedownload)
                )) { app ->
                    AppItem(
                        name = app.name,
                        packageName = app.packageName,
                        size = app.size,
                        lastUsed = app.lastUsed,
                        icon = app.icon,
                        onClick = {}
                    )
                }

                // Large apps section
                item {
                    SectionHeader(title = "Largest Apps", actionText = "View All", onActionClick = {})
                }
                items(listOf(
                    LargeAppItem("WhatsApp", "com.whatsapp", "5.9 GB", "Today", Icons.Filedownload),
                    LargeAppItem("Instagram", "com.instagram.android", "3.4 GB", "2 hours ago", Icons.Filled.Image),
                    LargeAppItem("Spotify", "com.spotify.music", "2.8 GB", "Yesterday", Icons.Filled.MusicNote)
                )) { app ->
                    AppItem(
                        name = app.name,
                        packageName = app.packageName,
                        size = app.size,
                        lastUsed = app.lastUsed,
                        icon = app.icon,
                        onClick = {}
                    )
                }
            }
        }
    }
}

data class UnusedAppItem(
    val name: String,
    val packageName: String,
    val size: String,
    val lastUsed: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class LargeAppItem(
    val name: String,
    val packageName: String,
    val size: String,
    val lastUsed: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AppSummaryCard(
    totalApps: Int,
    userApps: Int,
    totalSize: String,
    unusedApps: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "App Storage Overview",
                style = Typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryStat("Total Apps", "$totalApps", Icons.Filled.Android, Color.StorIQPurple)
                SummaryStat("User Apps", "$userApps", Icons.Filedownload, Color.StorIQBlue)
                SummaryStat("Total Size", totalSize, Icons.Filled.Storage, Color.StorIQGreen)
                SummaryStat("Unused", "$unusedApps", Icons.Filled.AccessTime, Color.StorIQOrange)
            }
        }
    }
}

@Composable
fun SummaryStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp).align(Alignment.Center)
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = Typography.bodySmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = Typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = actionText,
            style = Typography.labelLarge,
            color = Color.StorIQPurple
        ).let { text ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color.Transparent)
                    .clickable { onActionClick() }
                    .padding(vertical = 4.dp)
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    text
                }
            }
        }
    }
}

@Composable
fun AppItem(
    name: String,
    packageName: String,
    size: String,
    lastUsed: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
                    text = name,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = packageName,
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = size,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Last used: $lastUsed",
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}