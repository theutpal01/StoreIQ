package com.storiq.feature.history.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.storiq.core.model.StorageSnapshot
import com.storiq.core.ui.theme.Color
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.StorIQBlue
import com.storiq.core.ui.theme.StorIQOrange
import com.storiq.core.ui.theme.StorIQPurple
import com.storiq.core.ui.theme.StorIQRed
import com.storiq.core.ui.theme.StorIQTeal
import com.storiq.core.ui.theme.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ExpandMore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val snapshots by viewModel.snapshots.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val chartData by remember { mutableStateOf(viewModel.chartData) }
    val growth by remember { mutableStateOf(viewModel.storageGrowth) }
    val growthPercent by remember { mutableStateOf(viewModel.storageGrowthPercent) }
    val dailyGrowth by remember { mutableStateOf(viewModel.averageDailyGrowth) }
    val projectedDate by remember { mutableStateOf(viewModel.projectedFullDate) }
    var expandedMenu by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(viewModel.chartData) {
        chartData = viewModel.chartData
        growth = viewModel.storageGrowth
        growthPercent = viewModel.storageGrowthPercent
        dailyGrowth = viewModel.averageDailyGrowth
        projectedDate = viewModel.projectedFullDate
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = StorIQGreen)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text("Loading history...", style = Typography.bodyLarge)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Storage History", style = Typography.titleLarge, fontWeight = FontWeight.SemiBold) },
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
                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                HistoryViewModel.TimeRange.values().forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range.label) },
                                        onClick = {
                                            viewModel.setTimeRange(range)
                                            expandedMenu = false
                                        }
                                    )
                                }
                            }
                            IconButton(onClick = { expandedMenu = !expandedMenu }) {
                                Icon(
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = "Time range",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.StorIQPurple)
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Growth summary card
                    GrowthSummaryCard(
                        growth = growth,
                        growthPercent = growthPercent,
                        dailyGrowth = dailyGrowth,
                        projectedDate = projectedDate
                    )

                    // Time range selector
                    TimeRangeSelector(
                        currentRange = timeRange,
                        onRangeClick = { range ->
                            viewModel.setTimeRange(range)
                        }
                    )

                    // Chart
                    if (snapshots.isNotEmpty()) {
                        StorageChartCard(
                            chartData = chartData,
                            totalStorageGB = snapshots.first().totalBytes / (1024f * 1024 * 1024)
                        )
                    } else {
                        EmptyHistoryCard()
                    }

                    // Breakdown history
                    if (snapshots.size > 1) {
                        BreakdownHistoryCard(snapshots = snapshots)
                    }
                }
            }
        }
    }
}

@Composable
fun GrowthSummaryCard(
    growth: Long,
    growthPercent: Float,
    dailyGrowth: Long,
    projectedDate: String?
) {
    val isGrowthPositive = growth > 0
    val growthColor = if (isGrowthPositive) StorIQRed else StorIQGreen
    val growthIcon = if (isGrowthPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = growthColor.copy(alpha = 0.1f)
        )
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = growthIcon,
                        contentDescription = null,
                        tint = growthColor,
                        modifier = Modifier.size(28.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGrowthPositive) "Storage Growing" : "Storage Decreasing",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = growthColor
                    )
                }

                Text(
                    text = "${StorageBreakdown.formatBytes(growth.absoluteValue)} (${"%.1f".format(growthPercent.absoluteValue)}%)",
                    style = Typography.displayMedium.copy(fontWeight = FontWeight.Bold, color = growthColor)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GrowthStat(
                        label = "Daily Change",
                        value = StorageBreakdown.formatBytes(dailyGrowth.absoluteValue),
                        color = growthColor
                    )
                    GrowthStat(
                        label = "Projected Full",
                        value = projectedDate ?: "N/A",
                        color = growthColor
                    )
                }
            }
        }
    }
}

@Composable
fun GrowthStat(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color)
        )
        Text(
            text = label,
            style = Typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TimeRangeSelector(
    currentRange: HistoryViewModel.TimeRange,
    onRangeClick: (HistoryViewModel.TimeRange) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Time Range",
                style = Typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryViewModel.TimeRange.values().forEach { range ->
                    val isSelected = range == currentRange
                    androidx.compose.material3.Button(
                        onClick = { onRangeClick(range) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color.StorIQPurple else Color.Transparent,
                            contentColor = if (isSelected) Color.White else Color.StorIQPurple
                        ),
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = range.label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StorageChartCard(
    chartData: List<HistoryViewModel.ChartDataPoint>,
    totalStorageGB: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Storage Usage Trend",
                style = Typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                // Draw chart using Canvas
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawStorageChart(chartData, totalStorageGB)
                }
            }
            
            // Legend
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem("Used", StorIQRed)
                LegendItem("Apps", StorIQPurple)
                LegendItem("Media", StorIQBlue)
                LegendItem("Docs", StorIQOrange)
                LegendItem("Other", Color.Gray)
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = Typography.bodySmall)
    }
}

@Composable
fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No History Yet",
                    style = Typography.titleLarge,
                    color = Color.Gray
                )
                Text(
                    text = "Run a storage scan to start tracking your storage history",
                    style = Typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Navigate to dashboard */ },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = StorIQGreen
                    )
                ) {
                    Text(
                        text = "Run Scan",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BreakdownHistoryCard(snapshots: List<StorageSnapshot>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Category Breakdown",
                style = Typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryBreakdownRow(
                    label = "Apps",
                    current = snapshots.first().appBytes,
                    previous = snapshots.last().appBytes,
                    color = StorIQPurple
                )
                CategoryBreakdownRow(
                    label = "Media",
                    current = snapshots.first().mediaBytes,
                    previous = snapshots.last().mediaBytes,
                    color = StorIQBlue
                )
                CategoryBreakdownRow(
                    label = "Documents",
                    current = snapshots.first().documentsBytes,
                    previous = snapshots.last().documentsBytes,
                    color = StorIQOrange
                )
                CategoryBreakdownRow(
                    label = "Other",
                    current = snapshots.first().otherBytes,
                    previous = snapshots.last().otherBytes,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownRow(
    label: String,
    current: Long,
    previous: Long,
    color: Color
) {
    val change = current - previous
    val isPositive = change > 0
    
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
                    .size(12.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = Typography.bodyLarge
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = StorageBreakdown.formatBytes(current),
                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "${if (isPositive) "+" else ""}${StorageBreakdown.formatBytes(change.absoluteValue)}",
                style = Typography.bodySmall,
                color = if (isPositive) StorIQRed else StorIQGreen
            )
        }
    }
}

private fun drawStorageChart(
    chartData: List<HistoryViewModel.ChartDataPoint>,
    totalStorageGB: Float
) {
    if (chartData.size < 2) return
    
    val width = size.width
    val height = size.height
    val padding = 40f
    val chartWidth = width - 2 * padding
    val chartHeight = height - 2 * padding
    
    val maxY = totalStorageGB
    val minY = 0f
    
    // Draw axes
    drawLine(
        color = Color.LightGray,
        start = androidx.compose.ui.geometry.Offset(padding, padding),
        end = androidx.compose.ui.geometry.Offset(padding, height - padding),
        strokeWidth = 1f
    )
    drawLine(
        color = Color.LightGray,
        start = androidx.compose.ui.geometry.Offset(padding, height - padding),
        end = androidx.compose.ui.geometry.Offset(width - padding, height - padding),
        strokeWidth = 1f
    )
    
    // Draw used storage line
    val usedPath = Path()
    val appsPath = Path()
    val mediaPath = Path()
    val docsPath = Path()
    val otherPath = Path()
    
    var first = true
    for ((index, point) in chartData.withIndex()) {
        val x = padding + (index / (chartData.size - 1).toFloat()) * chartWidth
        val usedY = height - padding - (point.usedGB / maxY) * chartHeight
        val appsY = height - padding - (point.appsGB / maxY) * chartHeight
        val mediaY = height - padding - (point.mediaGB / maxY) * chartHeight
        val docsY = height - padding - (point.docsGB / maxY) * chartHeight
        val otherY = height - padding - (point.otherGB / maxY) * chartHeight
        
        if (first) {
            usedPath.moveTo(x, usedY)
            appsPath.moveTo(x, appsY)
            mediaPath.moveTo(x, mediaY)
            docsPath.moveTo(x, docsY)
            otherPath.moveTo(x, otherY)
            first = false
        } else {
            usedPath.lineTo(x, usedY)
            appsPath.lineTo(x, appsY)
            mediaPath.lineTo(x, mediaY)
            docsPath.lineTo(x, docsY)
            otherPath.lineTo(x, otherY)
        }
    }
    
    drawPath(usedPath, color = StorIQRed, style = androidx.compose.ui.graphics.Stroke(width = 2f, cap = StrokeCap.Round))
    drawPath(appsPath, color = StorIQPurple, style = androidx.compose.ui.graphics.Stroke(width = 1.5f, cap = StrokeCap.Round))
    drawPath(mediaPath, color = StorIQBlue, style = androidx.compose.ui.graphics.Stroke(width = 1.5f, cap = StrokeCap.Round))
    drawPath(docsPath, color = StorIQOrange, style = androidx.compose.ui.graphics.Stroke(width = 1.5f, cap = StrokeCap.Round))
    drawPath(otherPath, color = Color.Gray, style = androidx.compose.ui.graphics.Stroke(width = 1.5f, cap = StrokeCap.Round))
}