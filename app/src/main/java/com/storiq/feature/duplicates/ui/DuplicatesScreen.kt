package com.storiq.feature.duplicates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.storiq.core.model.DuplicateGroup
import com.storiq.core.model.MediaRecord
import com.storiq.core.ui.theme.Color
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.StorIQRed
import com.storiq.core.ui.theme.StorIQBlue
import com.storiq.core.ui.theme.StorIQTeal
import com.storiq.core.ui.theme.StorIQPurple
import com.storiq.core.ui.theme.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filedownload
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun DuplicatesScreen(
    viewModel: DuplicatesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val duplicateGroups by viewModel.duplicateGroups.collectAsState()
    val selectedGroups by viewModel.selectedGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showReview by viewModel.showReview.collectAsState()

    val selectedCount = viewModel.getSelectedCount()
    val selectedSize = viewModel.getSelectedTotalSize()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = StorIQGreen)
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.Modifier.height(16.dp))
            Text("Finding duplicates...", style = Typography.bodyLarge)
        }
    } else if (showReview) {
        DuplicatesReviewScreen(
            viewModel = viewModel,
            onBack = { viewModel.hideReviewScreen() }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Duplicates", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            if (duplicateGroups.isNotEmpty()) {
                                androidx.compose.material3.IconButton(onClick = { viewModel.selectAllGroups() }) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Select All",
                                        tint = Color.White
                                    )
                                }
                                androidx.compose.material3.IconButton(onClick = { viewModel.deselectAllGroups() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Deselect All",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = StorIQPurple)
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Summary banner
                    if (duplicateGroups.isNotEmpty()) {
                        DuplicatesSummaryBanner(
                            totalGroups = duplicateGroups.size,
                            totalWasted = duplicateGroups.sumOf { it.totalWastedBytes },
                            selectedCount = selectedCount,
                            selectedSize = selectedSize,
                            onReviewClick = { viewModel.showReviewScreen() }
                        )
                    }

                    // Group list
                    if (duplicateGroups.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 100.dp)
                        ) {
                            items(duplicateGroups) { group ->
                                DuplicateGroupCard(
                                    group = group,
                                    isSelected = selectedGroups.contains(group.id),
                                    onSelectionChange = { selected ->
                                        viewModel.toggleGroupSelection(group.id)
                                    },
                                    onClick = { /* Open group detail */ }
                                )
                            }
                        }
                    } else {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.ui.components.EmptyState(
                                icon = Icons.Filedownload.Done,
                                title = "No Duplicates Found",
                                message = "Your scanned files don't appear to contain exact duplicates.",
                                actionText = "Run Scan",
                                actionIcon = androidx.compose.material.icons.Icons.Filedownload.Refresh,
                                onAction = { viewModel.refresh() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DuplicatesSummaryBanner(
    totalGroups: Int,
    totalWasted: Long,
    selectedCount: Int,
    selectedSize: Long,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = StorIQPurple.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$totalGroups duplicate groups",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = StorIQPurple
                    )
                    Text(
                        text = "${formatBytes(totalWasted)} wasted",
                        style = Typography.bodyLarge,
                        color = StorIQRed
                    )
                }
                if (selectedCount > 0) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "$selectedCount selected",
                            style = Typography.bodyMedium,
                            color = StorIQPurple
                        )
                        Text(
                            text = "${formatBytes(selectedSize)} recoverable",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StorIQGreen)
                        )
                    }
                }
            }

            if (selectedCount > 0) {
                Button(
                    onClick = onReviewClick,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = StorIQGreen
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = "Review & Delete ($selectedCount items, ${formatBytes(selectedSize)})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DuplicateGroupCard(
    group: DuplicateGroup,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isVideo = group.category == com.storiq.core.model.MediaCategory.VIDEOS
    val isImage = group.category == com.storiq.core.model.MediaCategory.IMAGES

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) StorIQRed.copy(alpha = 0.05f) else Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) StorIQRed.copy(alpha = 0.1f) else Color.White
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
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
                        modifier = Modifier.size(16.dp).align(Alignment.Center)
                    )
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))

            // Preview thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = Color.Gray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isVideo) Icons.Filled.Videocam else Icons.Filled.Image,
                    contentDescription = null,
                    tint = if (isVideo) StorIQBlue else StorIQTeal,
                    modifier = Modifier.size(32.dp).align(Alignment.Center)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))

            // Group info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${group.count} × ${formatBytes(group.sizeBytes)}",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Wasted: ${group.formattedWastedSize}",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StorIQRed)
                    )
                }
                Text(
                    text = "Category: ${group.category.name}",
                    style = Typography.bodySmall,
                    color = Color.Gray
                )
                if (group.recommendedKeepUri != null) {
                    Text(
                        text = "Recommended: Keep 1 copy",
                        style = Typography.bodySmall,
                        color = StorIQGreen
                    )
                }
            }
        }
    }
}

@Composable
fun DuplicatesReviewScreen(
    viewModel: DuplicatesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val selectedGroups = viewModel.getSelectedGroups()
    val selectedCount = viewModel.getSelectedCount()
    val selectedSize = viewModel.getSelectedTotalSize()

    val photosCount = selectedGroups.sumOf { it.category == com.storiq.core.model.MediaCategory.IMAGES }.toInt()
    val videosCount = selectedGroups.sumOf { it.category == com.storiq.core.model.MediaCategory.VIDEOS }.toInt()

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
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Close,
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
                            text = "$selectedCount items • ${formatBytes(selectedSize)}",
                            style = Typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    Box(modifier = Modifier.width(48.dp))
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
                    StatItem("Photos", photosCount, Icons.Filled.Image, StorIQTeal)
                    StatItem("Videos", videosCount, Icons.Filled.Videocam, StorIQBlue)
                    StatItem("Potential Recovery", formatBytes(selectedSize), Icons.Filled.Delete, StorIQGreen)
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
                        onClick = { viewModel.deselectAllGroups() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = StorIQRed
                        ),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = "Deselect All",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = { viewModel.selectAllGroups() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = StorIQPurple
                        ),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = "Select All",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Group preview with individual file selection
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedGroups) { group ->
                    DuplicateReviewCard(
                        group = group,
                        isSelected = true,
                        onSelectionChange = { _ ->
                            viewModel.toggleGroupSelection(group.id)
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
                            text = "Remove $selectedCount items (${formatBytes(selectedSize)})",
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
fun DuplicateReviewCard(
    group: DuplicateGroup,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    val isVideo = group.category == com.storiq.core.model.MediaCategory.VIDEOS
    val isImage = group.category == com.storiq.core.model.MediaCategory.IMAGES
    val filesToDelete = group.files.minusElement(group.recommendedKeepUri)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
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
                                modifier = Modifier.size(16.dp).align(Alignment.Center)
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${group.count} ${group.category.name.toLowerCase()}s",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Wasted: ${group.formattedWastedSize} • ${filesToDelete.size} to delete",
                            style = Typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // File grid
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filesToDelete.forEachIndexed { index, uri ->
                    // In real app, would fetch MediaRecord from URI
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { /* Open preview */ }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray)
                        ) {
                            Icon(
                                imageVector = if (isVideo) Icons.Filled.Videocam else Icons.Filled.Image,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp).align(Alignment.Center)
                            )
                            if (uri == group.recommendedKeepUri) {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .align(Alignment.TopEnd)
                                        .background(
                                            color = StorIQGreen,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "KEEP",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recommended keep note
            if (group.recommendedKeepUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            color = StorIQGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StorIQGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recommended: Keep highest quality/original copy",
                            style = Typography.bodySmall,
                            color = StorIQGreen
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
            color = color.copy(alpha = 0.7f)
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024L * 1024 * 1024 * 1024 -> "%.2f TB".format(bytes / (1024.0 * 1024 * 1024 * 1024))
        bytes >= 1024L * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        bytes >= 1024L * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024))
        bytes >= 1024L -> "%.2f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}