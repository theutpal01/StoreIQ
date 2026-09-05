package com.storiq.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.Typography
import com.storiq.core.ui.theme.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Swipe

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToAnalyze: () -> Unit = {},
    onNavigateToClean: () -> Unit = {},
    onNavigateToApps: () -> Unit = {},
    onNavigateToSwipeClean: () -> Unit = {},
    onNavigateToLargeFiles: () -> Unit = {},
    onNavigateToDuplicates: () -> Unit = {}
) {
    val storageBreakdown by viewModel.storageBreakdown.collectAsState()
    val totalStorage by viewModel.totalStorage.collectAsState()
    val usedStorage by viewModel.usedStorage.collectAsState()
    val freeStorage by viewModel.freeStorage.collectAsState()
    val usagePercent by viewModel.usagePercent.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                color = Color.StorIQGreen
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Storage overview card
                item {
                    StorageOverviewCard(
                        totalStorage = totalStorage,
                        usedStorage = usedStorage,
                        freeStorage = freeStorage,
                        usagePercent = usagePercent
                    )
                }

                // Storage breakdown
                item {
                    if (storageBreakdown.isNotEmpty()) {
                        StorageBreakdownCard(
                            breakdown = storageBreakdown.map { b ->
                                BreakdownItem(b.category.name, b.formattedSize, getCategoryColor(b.category))
                            }
                        )
                    } else {
                        StorageBreakdownCard(
                            breakdown = listOf(
                                BreakdownItem("No data", "Scan to analyze", Color.Gray)
                            )
                        )
                    }
                }

                // Quick actions
                item {
                    QuickActionsCard(
                        onAnalyzeClick = onNavigateToAnalyze,
                        onSwipeCleanClick = onNavigateToSwipeClean,
                        onLargeFilesClick = onNavigateToLargeFiles,
                        onDuplicatesClick = onNavigateToDuplicates
                    )
                }

                // Recommendations
                item {
                    if (recommendations.isNotEmpty()) {
                        RecommendationsCard(
                            recommendations = recommendations.map { r ->
                                RecommendationItem(
                                    r.description,
                                    r.actionLabel,
                                    getPriorityColor(r.priority)
                                )
                            },
                            onCleanupClick = onNavigateToClean
                        )
                    } else {
                        RecommendationsCard(
                            recommendations = listOf(
                                RecommendationItem(
                                    "Run a scan to get recommendations",
                                    "Start Scan",
                                    Color.StorIQGreen
                                )
                            ),
                            onCleanupClick = onNavigateToClean
                        )
                    }
                }
            }
        }
    }

    private fun getCategoryColor(category: MediaCategory): Color {
        return when (category) {
            MediaCategory.APPS -> Color.StorIQPurple
            MediaCategory.VIDEOS -> Color.StorIQBlue
            MediaCategory.IMAGES -> Color.StorIQTeal
            MediaCategory.DOCUMENTS -> Color.StorIQOrange
            MediaCategory.DOWNLOADS -> Color.StorIQRed
            MediaCategory.ARCHIVES -> Color.StorIQPurple
            MediaCategory.APKS -> Color.StorIQTeal
            MediaCategory.AUDIO -> Color.StorIQGreen
            MediaCategory.SCREENSHOTS -> Color.StorIQOrange
            MediaCategory.SCREEN_RECORDINGS -> Color.StorIQBlue
            MediaCategory.OTHER -> Color.Gray
        }
    }

    private fun getPriorityColor(priority: com.storiq.core.model.RecommendationPriority): Color {
        return when (priority) {
            com.storiq.core.model.RecommendationPriority.URGENT -> Color.StorIQRed
            com.storiq.core.model.RecommendationPriority.HIGH -> Color.StorIQOrange
            com.storiq.core.model.RecommendationPriority.MEDIUM -> Color.StorIQBlue
            com.storiq.core.model.RecommendationPriority.LOW -> Color.StorIQTeal
            com.storiq.core.model.RecommendationPriority.INFO -> Color.StorIQGreen
        }
    }
}

@Composable
fun StorageOverviewCard(
    totalStorage: String,
    usedStorage: String,
    freeStorage: String,
    usagePercent: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Storage",
                    style = Typography.titleMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$usedStorage / $totalStorage",
                    style = Typography.displayMedium.copy(fontWeight = FontWeight.Bold)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$freeStorage free",
                    style = Typography.bodyLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .width(usagePercent / 100f)
                            .fillMaxHeight()
                            .background(
                                color = StorIQGreen,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${String.format("%.0f", usagePercent)}% used",
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class BreakdownItem(
    val name: String,
    val size: String,
    val color: Color
)

@Composable
fun StorageBreakdownCard(breakdown: List<BreakdownItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Storage Breakdown",
                style = Typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                breakdown.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = item.color,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.name,
                                style = Typography.bodyLarge
                            )
                        }
                        Text(
                            text = item.size,
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class RecommendationItem(
    val description: String,
    val actionText: String,
    val color: Color
)

@Composable
fun RecommendationsCard(
    recommendations: List<RecommendationItem>,
    onCleanupClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommendations",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                recommendations.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = item.color,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.description,
                                style = Typography.bodyMedium
                            )
                        }
                        Text(
                            text = item.actionText,
                            style = Typography.labelLarge,
                            color = item.color
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCleanupClick,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = StorIQGreen
                )
            ) {
                Text(
                    text = "Review Cleanup",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

data class QuickAction(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val action: () -> Unit
)

@Composable
fun QuickActionsCard(
    onAnalyzeClick: () -> Unit,
    onSwipeCleanClick: () -> Unit,
    onLargeFilesClick: () -> Unit,
    onDuplicatesClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Actions",
                style = Typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = "Analyze",
                    icon = Icons.Filled.Analytics,
                    color = Color.StorIQBlue,
                    action = onAnalyzeClick
                )
                QuickActionButton(
                    title = "Swipe Clean",
                    icon = Icons.Filled.Swipe,
                    color = StorIQGreen,
                    action = onSwipeCleanClick
                )
                QuickActionButton(
                    title = "Large Files",
                    icon = Icons.Filled.Storage,
                    color = Color.StorIQOrange,
                    action = onLargeFilesClick
                )
                QuickActionButton(
                    title = "Duplicates",
                    icon = Icons.Filled.Delete,
                    color = Color.StorIQRed,
                    action = onDuplicatesClick
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    action: () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = action,
        modifier = Modifier
            .weight(1f)
            .height(100.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = Typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}