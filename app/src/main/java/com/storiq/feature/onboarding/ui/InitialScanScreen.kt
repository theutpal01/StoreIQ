package com.storiq.feature.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.storiq.core.model.ScanType
import com.storiq.core.storage.StorageRepository
import com.storiq.core.storage.StorageRepositoryImpl
import com.storiq.core.ui.theme.StorIQGreen
import com.storiq.core.ui.theme.StorIQRed

@Composable
fun InitialScanScreen(
    repository: StorageRepository,
    onScanComplete: (StorageRepositoryImpl.FullScanResult) -> Unit
) {
    var scanResult by remember { mutableStateOf<StorageRepositoryImpl.FullScanResult?>(null) }
    var currentStep by remember { mutableStateOf(0) }
    var stepProgress by remember { mutableStateOf(0f) }

    val steps = remember {
        listOf(
            ScanStep("Finding media", ScanType.MEDIA_ONLY),
            ScanStep("Analyzing large files", ScanType.FULL),
            ScanStep("Finding duplicates", ScanType.FULL),
            ScanStep("Building recommendations", ScanType.FULL)
        )
    }

    LaunchedEffect(Unit) {
        val result = repository.performFullScan()
        scanResult = result
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (scanResult == null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Analyzing your device...",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LinearProgressIndicator(
                        progress = (currentStep.toFloat() + stepProgress) / steps.size,
                        color = StorIQGreen,
                        trackColor = StorIQGreen.copy(alpha = 0.2f),
                        modifier = Modifier.height(8.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                                            shape = CircleShape
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
                                        CircularProgressIndicator(
                                            color = StorIQGreen,
                                            modifier = Modifier.size(16.dp).align(Alignment.Center)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isCurrent || isCompleted) StorIQGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LinearProgressIndicator(
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
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (scanResult!!.success) {
                    Box(modifier = Modifier.size(80.dp)) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StorIQGreen,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Text(
                        text = "Analysis Complete!",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Found ${scanResult!!.mediaScanned} media files, ${scanResult!!.filesScanned} documents, ${scanResult!!.appsScanned} apps",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Completed in ${scanResult!!.durationMs / 1000}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Box(modifier = Modifier.size(80.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = StorIQRed,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Text(
                        text = "Scan Failed",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = StorIQRed
                    )
                    Text(
                        text = scanResult!!.errorMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onScanComplete(scanResult!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = StorIQGreen),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

data class ScanStep(
    val title: String,
    val type: ScanType
)
