package com.storiq.feature.swipeclean.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Offset
import androidx.compose.foundation.layout.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import com.storiq.core.ui.theme.Color

@Composable
fun SwipeCleanGalleryScreen(
    viewModel: SwipeCleanViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val session by viewModel.session.collectAsState()
    val decisions by viewModel.decisions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showReview by viewModel.showReview.collectAsState()

    var dragOffset by remember { mutableStateOf(0f) }
    var dragRotation by remember { mutableStateOf(0f) }

    val currentItem = if (currentIndex < mediaItems.size) mediaItems[currentIndex] else null

    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 2000f)
    )
    val animatedRotation by animateFloatAsState(
        targetValue = dragRotation,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.9f, stiffness = 1500f)
    )

    // Auto-advance when decision made
    LaunchedEffect(currentIndex, mediaItems.size) {
        if (currentIndex >= mediaItems.size && mediaItems.isNotEmpty()) {
            // Session complete, review screen will show
        }
    }

    if (showReview) {
        SwipeCleanReviewScreen(
            viewModel = viewModel,
            onBack = { viewModel.cancelSession() }
        )
    } else if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = StorIQGreen)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text("Loading media...", style = Typography.bodyLarge)
        }
    } else if (currentItem != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background dim when swiping
            if (dragOffset != 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (Math.abs(dragOffset) / 500f).coerceIn(0f, 0.5f)))
                )
            }

            // Main card stack
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${currentIndex + 1} / ${mediaItems.size}",
                            style = Typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = session?.let { "${it.deleteCandidates} for deletion (${StorageBreakdown.formatBytes(it.totalCandidateBytes)})" } ?: "",
                            style = Typography.bodyMedium,
                            color = StorIQRed
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = (currentIndex + 1).toFloat() / mediaItems.size,
                        color = StorIQGreen,
                        trackColor = StorIQGreen.copy(alpha = 0.3f)
                    )
                }

                // Media card
                MediaCard(
                    media = currentItem,
                    dragOffset = animatedOffset,
                    dragRotation = animatedRotation,
                    onDrag = { offset, rotation ->
                        dragOffset = offset
                        dragRotation = rotation
                    },
                    onDragEnd = { offset ->
                        dragOffset = 0f
                        dragRotation = 0f
                        handleDragDecision(offset, currentItem)
                    }
                )

                // Action buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Delete button
                        Button(
                            onClick = { viewModel.makeDecision(SwipeDecision.DELETE) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = StorIQRed
                            ),
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Keep button
                        Button(
                            onClick = { viewModel.makeDecision(SwipeDecision.KEEP) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = StorIQGreen
                            ),
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Keep",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keep",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Undo button
                    if (currentIndex > 0 && decisions.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.undoLastDecision() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Undo,
                                contentDescription = "Undo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Undo last decision",
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Swipe feedback overlays
            if (dragOffset < -50) {
                SwipeFeedbackOverlay(
                    text = "DELETE",
                    color = StorIQRed,
                    icon = Icons.Filled.Delete,
                    progress = (-dragOffset / 300f).coerceIn(0f, 1f)
                )
            } else if (dragOffset > 50) {
                SwipeFeedbackOverlay(
                    text = "KEEP",
                    color = StorIQGreen,
                    icon = Icons.Filled.Favorite,
                    progress = (dragOffset / 300f).coerceIn(0f, 1f)
                )
            }
        }
    }

    private fun handleDragDecision(offset: Float, media: MediaRecord) {
        if (offset < -100) {
            viewModel.makeDecision(SwipeDecision.DELETE)
        } else if (offset > 100) {
            viewModel.makeDecision(SwipeDecision.KEEP)
        }
    }
}

@Composable
fun MediaCard(
    media: MediaRecord,
    dragOffset: Float,
    dragRotation: Float,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: (Float) -> Unit
) {
    val isVideo = media.mimeType.startsWith("video/")

    Box(
        modifier = Modifier
            .width(340.dp)
            .height(500.dp)
            .graphicsLayer {
                translationX = dragOffset
                rotationZ = dragRotation
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = dragOffset + dragAmount.x
                        val newRotation = (newOffset / 300f) * 15f // Max 15 degrees
                        onDrag(newOffset, newRotation.coerceIn(-15f, 15f))
                    },
                    onDragEnd = {
                        onDragEnd(dragOffset)
                    },
                    onDragCancel = {
                        onDrag(0f, 0f)
                    }
                )
            }
            .background(Color.Black)
    ) {
        if (isVideo) {
            // Video thumbnail with play button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Placeholder for video thumbnail
                androidx.compose.ui.res.PainterResources(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Videocam,
                    contentDescription = null
                )?.let { painter ->
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                    )
                }
                // Play button overlay
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp).align(Alignment.Center)
                    )
                }
                // Duration badge
                media.durationMs?.let { duration ->
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formatDuration(duration),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // Image
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = android.R.drawable.ic_menu_gallery // Placeholder
                ),
                contentDescription = media.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // File size badge
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = media.formattedSize,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SwipeFeedbackOverlay(
    text: String,
    color: Color,
    icon: ImageVector,
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(progress)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = color.copy(alpha = 0.9f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp).align(Alignment.Center)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs / 60000
    val seconds = (durationMs % 60000) / 1000
    return "%02d:%02d".format(minutes, seconds)
}