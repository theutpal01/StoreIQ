package com.storiq.feature.largefiles.ui

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.storiq.core.model.MediaRecord
import com.storiq.core.ui.theme.Color
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.StorIQBlue
import com.storiq.core.ui.theme.StorIQOrange
import com.storiq.core.ui.theme.StorIQPurple
import com.storiq.core.ui.theme.StorIQRed
import com.storiq.core.ui.theme.StorIQTeal
import com.storiq.core.ui.theme.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filedownload
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive

@Composable
fun LargeFilesScreen(
    viewModel: LargeFilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val filteredFiles by viewModel.filteredFiles.collectAsState()
    val sizeGroups by viewModel.sizeGroups.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var expandedSort by remember { mutableStateOf(false) }
    var expandedGroup by remember { mutableStateOf(false) }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = StorIQGreen)
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.Modifier.height(16.dp))
            Text("Loading large files...", style = Typography.bodyLarge)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Large Files", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
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
                            // Sort dropdown
                            DropdownMenu(
                                expanded = expandedSort,
                                onDismissRequest = { expandedSort = false }
                            ) {
                                LargeFilesViewModel.SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        onClick = {
                                            viewModel.setSort(option)
                                            expandedSort = false
                                        }
                                    )
                                }
                            }
                            IconButton(onClick = { expandedSort = !expandedSort }) {
                                Icon(
                                    imageVector = Icons.Filled.Sort,
                                    contentDescription = "Sort",
                                    tint = Color.White
                                )
                            }

                            // Group filter dropdown
                            DropdownMenu(
                                expanded = expandedGroup,
                                onDismissRequest = { expandedGroup = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Sizes") },
                                    onClick = {
                                        viewModel.setGroupFilter(null)
                                        expandedGroup = false
                                    }
                                )
                                sizeGroups.forEach { group ->
                                    DropdownMenuItem(
                                        text = { Text("${group.filter.label} (${group.count})") },
                                        onClick = {
                                            viewModel.setGroupFilter(group.filter)
                                            expandedGroup = false
                                        }
                                    )
                                }
                            }
                            IconButton(onClick = { expandedGroup = !expandedGroup }) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filter by size",
                                    tint = Color.White
                                )
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
                    // Size group chips
                    if (sizeGroups.isNotEmpty()) {
                        SizeGroupChips(
                            groups = sizeGroups,
                            selectedGroup = selectedGroup,
                            onGroupClick = { group ->
                                viewModel.setGroupFilter(group)
                            },
                            onClearClick = { viewModel.setGroupFilter(null) }
                        )
                    }

                    // Results count
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(Color.White)
                    ) {
                        Text(
                            text = "${filteredFiles.size} files • ${formatTotalSize(filteredFiles)}",
                            style = Typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    // File list
                    if (filteredFiles.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 100.dp)
                        ) {
                            items(filteredFiles) { file ->
                                LargeFileItem(
                                    file = file,
                                    onClick = { /* Open preview */ },
                                    onShare = { /* Share file */ },
                                    onDelete = { /* Delete file */ }
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
                                icon = Icons.Filled.Storage,
                                title = selectedGroup != null ? "No files in this size range" : "No large files found",
                                message = selectedGroup != null ? "Try a different size filter" : "Run a storage scan to find large files",
                                actionText = "Run Scan",
                                actionIcon = androidx.compose.material.icons.Icons.Filedownload.Refresh,
                                onAction = { viewModel.loadFiles() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SizeGroupChips(
    groups: List<LargeFilesViewModel.SizeGroup>,
    selectedGroup: LargeFilesViewModel.SizeGroupFilter?,
    onGroupClick: (LargeFilesViewModel.SizeGroupFilter) -> Unit,
    onClearClick: () -> Unit
) {
    val chips = listOf<LargeFilesViewModel.SizeGroupFilter?>(null) + groups.map { it.filter }

    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { group ->
            val isSelected = group == selectedGroup
            val label = group?.label ?: "All"
            val count = group?.let { g -> groups.find { it.filter == g }?.count } ?: groups.sumOf { it.count }

            androidx.compose.material3.FilterChip(
                selected = isSelected,
                onClick = {
                    if (group == null) onClearClick() else onGroupClick(group)
                },
                label = { Text("$label ($count)") },
                leadingIcon = if (isSelected) {
                    { Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White) }
                } else null,
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StorIQPurple,
                    unselectedContainerColor = Color.White,
                    selectedLabelColor = Color.White,
                    unselectedLabelColor = Color.Black
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .height(32.dp)
            )
        }
    }
}

@Composable
fun LargeFileItem(
    file: MediaRecord,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isVideo = file.mimeType.startsWith("video/")
    val isAudio = file.mimeType.startsWith("audio/")
    val isImage = file.mimeType.startsWith("image/")
    val isApk = file.mimeType.contains("android.package")
    val isArchive = file.mimeType.contains("zip") || file.mimeType.contains("rar") || file.mimeType.contains("7z")
    val isDocument = file.mimeType.startsWith("application/pdf") || file.mimeType.startsWith("application/vnd.openxmlformats")

    val (icon, color) = when {
        isVideo -> Icons.Filled.Videocam to StorIQBlue
        isAudio -> Icons.Filled.MusicNote to StorIQOrange
        isImage -> Icons.Filled.Image to StorIQTeal
        isApk -> Icons.Filled.Android to StorIQGreen
        isArchive -> Icons.Filled.Archive to StorIQPurple
        isDocument -> Icons.Filled.Description to StorIQRed
        else -> Icons.Filedownload to Color.Gray
    }

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
            // File type icon
            Box(
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

            // File info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = file.displayName,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                )
                Text(
                    text = file.relativePath ?: "Unknown location",
                    style = Typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.formattedSize,
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = color)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatDate(file.modifiedDate),
                        style = Typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Actions
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = StorIQRed
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}

private fun formatTotalSize(files: List<MediaRecord>): String {
    val total = files.sumOf { it.sizeBytes }
    return when {
        total >= 1024L * 1024 * 1024 * 1024 -> "%.2f TB".format(total / (1024.0 * 1024 * 1024 * 1024))
        total >= 1024L * 1024 * 1024 -> "%.2f GB".format(total / (1024.0 * 1024 * 1024))
        total >= 1024L * 1024 -> "%.2f MB".format(total / (1024.0 * 1024))
        total >= 1024L -> "%.2f KB".format(total / 1024.0)
        else -> "$total B"
    }
}