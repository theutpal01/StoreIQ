package com.storiq.feature.cleanup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Android

@Composable
fun CleanScreen() {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Clean", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
                )
            }
        ) { paddingValues ->
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Potential recovery banner
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            color = Color.StorIQGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "3.4 GB",
                            style = Typography.displayLarge.copy(fontWeight = FontWeight.Bold, color = Color.StorIQGreen)
                        )
                        Text(
                            text = "Potentially Recoverable",
                            style = Typography.titleMedium,
                            color = Color.StorIQGreen
                        )
                    }
                }

                // Cleanup categories
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 100.dp)
                ) {
                    item {
                        CleanupCategoryCard(
                            title = "Duplicate Files",
                            description = "12 duplicate groups found",
                            size = "1.4 GB",
                            icon = Icons.Filled.Delete,
                            color = Color.StorIQBlue,
                            risk = "Low Risk",
                            onClick = {}
                        )
                    }
                    item {
                        CleanupCategoryCard(
                            title = "Old Downloads",
                            description = "11 files not modified in 180+ days",
                            size = "856 MB",
                            icon = Icons.Filedownload,
                            color = Color.StorIQOrange,
                            risk = "Low Risk",
                            onClick = {}
                        )
                    }
                    item {
                        CleanupCategoryCard(
                            title = "Screenshots",
                            description = "247 screenshots taking up space",
                            size = "1.2 GB",
                            icon = Icons.Filled.Image,
                            color = Color.StorIQTeal,
                            risk = "Low Risk",
                            onClick = {}
                        )
                    }
                    item {
                        CleanupCategoryCard(
                            title = "Empty Folders",
                            description = "43 empty folders safe to remove",
                            size = "0 B",
                            icon = Icons.Filled.Folder,
                            color = Color.StorIQPurple,
                            risk = "Very Low Risk",
                            onClick = {}
                        )
                    }
                    item {
                        CleanupCategoryCard(
                            title = "Old APK Installers",
                            description = "5 installer files no longer needed",
                            size = "312 MB",
                            icon = Icons.Filled.Android,
                            color = Color.StorIQRed,
                            risk = "Low Risk",
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CleanupCategoryCard(
    title: String,
    description: String,
    size: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    risk: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    text = description,
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
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color)
                )
                Text(
                    text = risk,
                    style = Typography.labelSmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}