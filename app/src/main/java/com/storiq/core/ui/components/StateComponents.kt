package com.storiq.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.storiq.core.ui.theme.Color
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.StorIQOrange
import com.storiq.core.ui.theme.StorIQRed
import com.storiq.core.ui.theme.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filedownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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
                        shape = androidx.compose.foundation.shape.CircleShape
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
            (actionText?.let { action ->
                Button(
                    onClick = onAction!!,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = StorIQGreen
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        actionIcon?.let { actionIcon ->
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = action,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            })
        }
    }
}

@Composable
fun ErrorState(
    title: String,
    message: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = StorIQRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = StorIQRed.copy(alpha = 0.15f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = StorIQRed,
                    modifier = Modifier.size(24.dp).align(Alignment.Center)
                )
            }
            Text(
                text = title,
                style = Typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = StorIQRed
            )
            Text(
                text = message,
                style = Typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                onRetry?.let {
                    Button(
                        onClick = it,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = StorIQRed
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retry",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
                onDismiss?.let {
                    Button(
                        onClick = it,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = StorIQRed
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Dismiss",
                            fontWeight = FontWeight.SemiBold,
                            color = StorIQRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionState(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Lock,
    onGrant: () -> Unit,
    onContinue: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = StorIQOrange.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = StorIQOrange.copy(alpha = 0.15f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StorIQOrange,
                    modifier = Modifier.size(24.dp).align(Alignment.Center)
                )
            }
            Text(
                text = title,
                style = Typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = StorIQOrange
            )
            Text(
                text = message,
                style = Typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGrant,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = StorIQOrange
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filedownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grant Access",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                onContinue?.let {
                    Button(
                        onClick = it,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = StorIQOrange
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "Continue with Limited Access",
                            fontWeight = FontWeight.SemiBold,
                            color = StorIQOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState(
    message: String = "Loading...",
    showProgress: Boolean = false,
    progress: Float = 0f,
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
            androidx.compose.material3.CircularProgressIndicator(
                color = StorIQGreen,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = Typography.bodyLarge,
                color = Color.Gray
            )
            if (showProgress) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = progress,
                        color = StorIQGreen,
                        trackColor = StorIQGreen.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = Typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ScanProgressState(
    steps: List<ScanStep>,
    currentStep: Int,
    stepProgress: Float,
    message: String = "Analyzing your device...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = Typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.LinearProgressIndicator(
                progress = (currentStep.toFloat() + stepProgress) / steps.size,
                color = StorIQGreen,
                trackColor = StorIQGreen.copy(alpha = 0.2f),
                modifier = Modifier.height(8.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                steps.forEachIndexed { index, step ->
                    val isCurrent = index == currentStep
                    val isCompleted = index < currentStep
                    val progress = if (isCompleted) 1f else if (isCurrent) stepProgress else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (isCompleted) StorIQGreen else 
                                            if (isCurrent) StorIQGreen.copy(alpha = 0.5f) else Color.LightGray,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp).align(Alignment.Center)
                                )
                            } else if (isCurrent) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = StorIQGreen,
                                    modifier = Modifier.size(16.dp).align(Alignment.Center)
                                )
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = step.title,
                                style = Typography.bodyLarge,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isCurrent || isCompleted) StorIQGreen else Color.Gray
                            )
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = progress,
                                color = StorIQGreen,
                                trackColor = StorIQGreen.copy(alpha = 0.2f),
                                modifier = Modifier.height(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ScanStep(
    val title: String
)