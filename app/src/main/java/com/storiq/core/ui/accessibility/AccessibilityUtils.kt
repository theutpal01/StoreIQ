package com.storiq.core.ui.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.RangeInfo
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionSelectionMode
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AccessibilityUtils {
    
    fun Modifier.accessibleButton(
        description: String,
        stateDescription: String? = null
    ): Modifier {
        return this
            .semantics {
                role = Role.Button
                contentDescription = description
                stateDescription?.let { this.stateDescription = it }
            }
    }

    fun Modifier.accessibleImage(
        description: String
    ): Modifier {
        return this
            .semantics {
                contentDescription = description
            }
    }

    fun Modifier.accessibleText(
        text: String
    ): Modifier {
        return this
            .semantics {
                contentDescription = text
            }
    }

    fun Modifier.accessibleProgress(
        progress: Float,
        max: Float = 1f,
        min: Float = 0f,
        description: String
    ): Modifier {
        return this
            .semantics {
                this.progressBarRangeInfo = ProgressBarRangeInfo(min, max, progress, true)
                contentDescription = description
            }
    }

    fun Modifier.accessibleSlider(
        value: Float,
        min: Float,
        max: Float,
        description: String
    ): Modifier {
        return this
            .semantics {
                this.rangeInfo = RangeInfo(min, max, value)
                contentDescription = description
            }
    }

    fun Modifier.accessibleTab(
        selected: Boolean,
        description: String
    ): Modifier {
        return this
            .semantics {
                role = Role.Tab
                contentDescription = description
                selected = selected
            }
    }

    fun Modifier.accessibleHeading(
        level: Int = 1,
        text: String
    ): Modifier {
        return this
            .semantics {
                role = Role.Heading
                contentDescription = text
                headingLevel = level
            }
    }

    fun Modifier.accessibleListItem(
        index: Int,
        count: Int,
        description: String
    ): Modifier {
        return this
            .semantics {
                role = Role.ListItem
                contentDescription = description
                collectionInfo = CollectionInfo(
                    rowCount = count,
                    columnCount = 1,
                    selectionMode = CollectionSelectionMode.None
                )
                collectionItemInfo = CollectionItemInfo(
                    rowIndex = index,
                    rowSpan = 1,
                    columnIndex = 0,
                    columnSpan = 1,
                    selected = false
                )
            }
    }

    fun Modifier.accessibleSwitch(
        checked: Boolean,
        onText: String = "On",
        offText: String = "Off",
        description: String
    ): Modifier {
        return this
            .semantics {
                role = Role.Switch
                contentDescription = description
                stateDescription = if (checked) onText else offText
            }
    }

    fun Modifier.accessibleCard(
        description: String,
        selected: Boolean = false
    ): Modifier {
        return this
            .semantics {
                role = Role.Button
                contentDescription = description
                selected = selected
            }
    }

    fun Modifier.accessibleSwipeAction(
        actionDescription: String,
        valueDescription: String
    ): Modifier {
        return this
            .semantics {
                role = Role.Button
                contentDescription = actionDescription
                stateDescription = valueDescription
            }
    }
}

@Composable
fun AccessibleCard(
    modifier: Modifier = Modifier,
    description: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .accessibleCard(description, selected)
            .clickable { onClick?.invoke() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}