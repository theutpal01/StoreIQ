package com.storiq.feature.analyzer.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storiq.core.ui.theme.Color
import com.storiq.core.ui.theme.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filedownload
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Android

@Composable
fun AnalyzerScreen() {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Analyze", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
            ) {
                item {
                    CategoryCard(
                        title = "Media Analysis",
                        subtitle = "Photos, videos, audio files",
                        icon = Icons.Filled.PieChart,
                        color = Color.StorIQPurple,
                        onClick = {}
                    )
                }
                item {
                    CategoryCard(
                        title = "Images",
                        subtitle = "Photos, screenshots, screen recordings",
                        icon = Icons.Filled.Image,
                        color = Color.StorIQTeal,
                        onClick = {}
                    )
                }
                item {
                    CategoryCard(
                        title = "Videos",
                        subtitle = "Movies, screen recordings, clips",
                        icon = Icons.Filled.Videocam,
                        color = Color.StorIQBlue,
                        onClick = {}
                    )
                }
                item {
                    CategoryCard(
                        title = "Audio",
                        subtitle = "Music, recordings, voice memos",
                        icon = Icons.Filled.MusicNote,
                        color = Color.StorIQOrange,
                        onClick = {}
                    )
                }
                item {
                    CategoryCard(
                        title = "Documents",
                        subtitle = "PDFs, Office files, text documents",
                        icon = Icons.Filled.Description,
                        color = Color.StorIQGreen,
                        onClick = {}
                    )
                }
                item {
                    CategoryCard(
                        title = "Downloads",
                        subtitle = "Downloaded files from browser, apps",
                        icon = Icons.Filedownload,
                        color = Color.StorIQRed,
                        onClick = {}
                    )
                }
                item {
                    CategoryCard(
                        title = "Archives",
                        subtitle = "ZIP, RAR, 7z, compressed files",
                        icon = Icons.Filled.Archive,
                        color = Color.StorIQPurple,
                        onClick = {}
                    )
                }
                item {
                    CategoryCard(
                        title = "APKs",
                        subtitle = "Installer files, app bundles",
                        icon = Icons.Filled.Android,
                        color = Color.StorIQTeal,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(80.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
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
                    text = title,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
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