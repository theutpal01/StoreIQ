package com.storiq.feature.swipeclean.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.storiq.core.model.MediaRecord
import com.storiq.core.model.SwipeDecision
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.StorIQRed
import com.storiq.core.ui.theme.Typography
import com.storiq.core.ui.theme.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.foundation.layout.Offset

@Composable
fun SwipeCleanReviewScreen(
    viewModel: SwipeCleanViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val deleteCandidates = viewModel.getDeleteCandidates()
    val keptItems = viewModel.getKeptItems()
    val session = viewModel.session.collectAsState().value

    val selectedCount = deleteCandidates.size
    val selectedSize = deleteCandidates.sumOf { it.sizeBytes }
    val photosCount = deleteCandidates.count { it.mimeType.startsWith("image/") }
    val videosCount = deleteCandidates.count { it.mimeType.startsWith("video/") }

    var selectAll by remember { mutableStateOf(false) }
    var individualSelection by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

    // Initialize selection
    androidx.compose.runtime.LaunchedEffect(deleteCandidates) {
        individualSelection.clear()
        deleteCandidates.forEach { individualSelection[it.uri] = true }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Review Deletions",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$selectedCount items • ${StorageBreakdown.formatBytes(selectedSize)}",
                            style = Typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    Box(modifier = Modifier.width(48.dp)) // Balance
                }
            }

            // Stats row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Photos", photosCount, Icons.Filled.Image, Color.StorIQTeal)
                    StatItem("Videos", videosCount, Icons.Filled.Videocam, Color.StorIQBlue)
                    StatItem("Potential Recovery", StorageBreakdown.formatBytes(selectedSize), Icons.Filled.Delete, StorIQGreen)
                }
            }

            // Selection controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectAll) {
                                viewModel.deselectAll()
                                selectAll = false
                                deleteCandidates.forEach { individualSelection[it.uri] = false }
                            } else {
                                viewModel.selectAllForDeletion()
                                selectAll = true
                                deleteCandidates.forEach { individualSelection[it.uri] = true }
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (selectAll) StorIQRed else Color.StorIQPurple
                        ),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = if (selectAll) "Deselect All" else "Select All",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            )

            // Grid preview
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .weight(1f),
                cells = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
            ) {
                items(deleteCandidates) { media ->
                    ReviewGridItem(
                        media = media,
                        isSelected = individualSelection[media.uri] ?: true,
                        onSelectionChange = { selected ->
                            individualSelection[media.uri] = selected
                            if (selected) {
                                // Keep in delete candidates
                            } else {
                                // Remove from delete candidates - would need viewModel method
                            }
                        },
                        onClick = {
                            // Open fullscreen preview
                        }
                    )
                }
            }

            // Bottom action bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White)
            ) {
                Button(
                    onClick = { viewModel.confirmDeletion() },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = StorIQRed
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Remove $selectedCount items (${StorageBreakdown.formatBytes(selectedSize)})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: Any,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp).align(Alignment.Center)
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toString(),
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = Typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun ReviewGridItem(
    media: MediaRecord,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isVideo = media.mimeType.startsWith("video/")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Media content
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    androidx.compose.material.Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp).align(Alignment.Center)
                    )
                    media.durationMs?.let { duration ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.BottomEnd)
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = formatDuration(duration),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                ) {
                    androidx.compose.material.Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp).align(Alignment.Center)
                    )
                }
            }

            // Selection overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (isSelected) StorIQRed.copy(alpha = 0.3f) else Color.Transparent
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (isSelected) StorIQRed else Color.White,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) StorIQRed else Color.LightGray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp).align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            // File size badge
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = media.formattedSize,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs / 60000
    val seconds = (durationMs % 60000) / 1000
    return "%02d:%02d".format(minutes, seconds)
}