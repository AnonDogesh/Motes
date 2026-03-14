package com.example.motes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    title: String,
    bodyPreview: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCardContainer(
        selected = selected,
        onClick = onClick,
        onLongPress = onLongPress,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = bodyPreview,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistCard(
    title: String,
    checkedCount: Int,
    totalCount: Int,
    selected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeTotal = totalCount.coerceAtLeast(0)
    val safeChecked = checkedCount.coerceIn(0, safeTotal)
    val progress = if (safeTotal == 0) 0f else safeChecked.toFloat() / safeTotal.toFloat()
    val completionLabel = if (safeTotal == 0) "0%" else "${(progress * 100).toInt()}%"

    PremiumCardContainer(
        selected = selected,
        onClick = onClick,
        onLongPress = onLongPress,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$safeChecked / $safeTotal done",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = completionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawingCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCardContainer(
        selected = selected,
        onClick = onClick,
        onLongPress = onLongPress,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                val strokeColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                drawLine(strokeColor, start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.65f), end = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.35f), strokeWidth = 5f)
                drawLine(strokeColor, start = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.35f), end = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.58f), strokeWidth = 5f)
                drawCircle(
                    color = strokeColor,
                    radius = size.minDimension * 0.18f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.84f, size.height * 0.28f),
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PremiumCardContainer(
    selected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        label = "card_border_color"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 4.dp else if (selected) 12.dp else 8.dp,
        label = "card_elevation"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else if (selected) 8.dp else 5.dp,
        label = "card_shadow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}
