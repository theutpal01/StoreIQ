package com.storiq.feature.largefiles.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storiq.core.model.MediaRecord
import com.storiq.core.ui.theme.StorIQBlue
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.StorIQOrange
import com.storiq.core.ui.theme.StorIQPurple
import com.storiq.core.ui.theme.StorIQRed
import com.storiq.core.ui.theme.StorIQTeal
import com.storiq.core.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color.Gray.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp).align(Alignment.Center)
                )
            }
            Text(
                text = title,
                style = Typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Text(
                text = message,
                style = Typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            if (actionText != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StorIQGreen
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        actionIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = actionText,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeFilesScreen(
    viewModel: LargeFilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = LargeFilesViewModelFactory(
            repository = com.storiq.AppEntryPoint.get(
                androidx.compose.ui.platform.LocalContext.current.applicationContext
            ).storageRepository()
        )
    ),
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = StorIQGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading large files...", style = Typography.bodyLarge)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Large Files", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
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

                    if (filteredFiles.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
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
                        val emptyTitle = if (selectedGroup != null) "No files in this size range" else "No large files found"
                        val emptyMessage = if (selectedGroup != null) "Try a different size filter" else "Run a storage scan to find large files"
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                icon = Icons.Filled.Storage,
                                title = emptyTitle,
                                message = emptyMessage,
                                actionText = "Run Scan",
                                actionIcon = Icons.Filled.Refresh,
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

    Row(
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

            FilterChip(
                selected = isSelected,
                onClick = {
                    if (group == null) onClearClick() else onGroupClick(group)
                },
                label = { Text("$label ($count)") },
                leadingIcon = if (isSelected) {
                    { Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
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
        else -> Icons.Filled.InsertDriveFile to Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = file.displayName,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = file.relativePath ?: "Unknown location",
                    style = Typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.formattedSize,
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = color)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatDate(file.modifiedDate),
                        style = Typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Row(
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
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
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
