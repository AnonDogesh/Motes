@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.motes.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.motes.data.AppContainer
import com.example.motes.navigation.AppRoute
import com.example.motes.ui.components.FilterDialFab
import com.example.motes.ui.components.SpeedDialFab
import com.example.motes.ui.theme.Accent
import com.example.motes.ui.theme.DarkBlueBase
import com.example.motes.ui.theme.MotesTheme
import com.example.motes.ui.theme.cardColorForDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val noteRepository = remember(context) { AppContainer.noteRepository(context) }
    val checklistRepository = remember(context) { AppContainer.checklistRepository(context) }
    val drawingRepository = remember(context) { AppContainer.drawingRepository(context) }
    val factory = remember(noteRepository, checklistRepository, drawingRepository) {
        HomeViewModelFactory(noteRepository, checklistRepository, drawingRepository)
    }
    val viewModel: HomeViewModel = viewModel(factory = factory)

    val items by viewModel.items.collectAsState()
    val uiMode by viewModel.uiMode.collectAsState()
    val filter by viewModel.filter.collectAsState()

    val selectionMode = uiMode as? UiMode.Selection
    val selectedKeys = selectionMode?.selectedKeys.orEmpty()
    val inSelectionMode = selectionMode != null
    val defaultTitle = when (filter) {
        HomeFilter.ALL -> "Motes"
        HomeFilter.NOTE -> "Notes"
        HomeFilter.CHECKLIST -> "Checklists"
        HomeFilter.DRAWING -> "Drawings"
    }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = inSelectionMode) {
        viewModel.clearSelection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (inSelectionMode) "${selectedKeys.size} selected" else defaultTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (inSelectionMode) MaterialTheme.colorScheme.onSurface else Accent
                    )
                },
                actions = {
                    if (inSelectionMode) {
                        TextButton(onClick = viewModel::clearSelection) { Text("Cancel") }
                    } else {
                        IconButton(onClick = { navController.navigate(AppRoute.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = inSelectionMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                SelectionActionBar(
                    onArchive = {
                        viewModel.archiveSelected()
                        navController.navigate(AppRoute.Archive.route)
                    },
                    onDelete = { showDeleteConfirmation = true },
                    onShare = {
                        val shareText = viewModel.buildShareTextForSelection()
                        if (shareText.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share notes"))
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                columns = StaggeredGridCells.Adaptive(180.dp),
                contentPadding = PaddingValues(16.dp),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = items, key = { it.id }) { item ->
                    HomeCard(
                        item = item,
                        isSelected = selectedKeys.contains(item.selectionKey),
                        onClick = {
                            if (inSelectionMode) {
                                viewModel.onNoteClick(item.selectionKey)
                            } else {
                                val route = when (item.type) {
                                    HomeNoteType.NOTE -> AppRoute.NoteEditor(item.id.toLongOrNull() ?: -1L).route
                                    HomeNoteType.CHECKLIST -> AppRoute.ChecklistEditor(item.id).route
                                    HomeNoteType.DRAWING -> AppRoute.DrawingEditor(item.id).route
                                }
                                navController.navigate(route)
                            }
                        },
                        onLongClick = { viewModel.onNoteLongPress(item.selectionKey) }
                    )
                }
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Delete selected notes?") },
                    text = { Text("This action permanently deletes the selected items.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteSelected()
                            showDeleteConfirmation = false
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (!inSelectionMode) {
                FilterDialFab(
                    activeFilter = filter,
                    onNotes = { viewModel.setFilter(HomeFilter.NOTE) },
                    onChecklists = { viewModel.setFilter(HomeFilter.CHECKLIST) },
                    onDrawings = { viewModel.setFilter(HomeFilter.DRAWING) },
                    onArchive = { navController.navigate(AppRoute.Archive.route) }
                )
                SpeedDialFab(
                    onNewNote = { viewModel.createNewNote { id -> navController.navigate(AppRoute.NoteEditor(id).route) } },
                    onNewChecklist = { viewModel.createNewChecklist { id -> navController.navigate(AppRoute.ChecklistEditor(id).route) } },
                    onNewDrawing = { viewModel.createNewDrawing { id -> navController.navigate(AppRoute.DrawingEditor(id).route) } }
                )
            }
        }
    }
}

@Composable
private fun SelectionActionBar(
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)))
            TextButton(onClick = onArchive) {
                Icon(Icons.Default.Archive, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Archive")
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)))
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun HomeCard(
    item: HomeListItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val animatedElevation = animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (isSelected) 10.dp else 8.dp,
        label = "home_card_elevation"
    )
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.6f
    val cardTitleColor = if (isLightTheme) DarkBlueBase else Color.White
    val cardSubColor = if (isLightTheme) DarkBlueBase.copy(alpha = 0.82f) else Color(0xFFEAEAEA)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                spotColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.surfaceVariant
                item.cardColor != null -> Color(cardColorForDisplay(item.cardColor, isLightTheme))
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation.value),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.type == HomeNoteType.NOTE && !item.previewImageUri.isNullOrBlank()) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        },
                        update = { imageView ->
                            imageView.setImageURI(Uri.parse(item.previewImageUri))
                        }
                    )
                }

                if (item.type == HomeNoteType.DRAWING && item.previewDrawingPaths.isNotEmpty()) {
                    DrawingPreview(
                        strokePaths = item.previewDrawingPaths,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    )
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = cardTitleColor
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cardSubColor
                    )
                    Text(
                        text = when (item.type) {
                            HomeNoteType.NOTE -> "Note"
                            HomeNoteType.CHECKLIST -> "Checklist"
                            HomeNoteType.DRAWING -> "Drawing"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = cardTitleColor
                    )
                    if (item.type == HomeNoteType.CHECKLIST) {
                        LinearProgressIndicator(
                            progress = { item.checklistProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = if ((item.checklistProgress ?: 0f) >= 0.999f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )
                        Text(
                            text = item.checklistCompletionLabel ?: "0 checked • 0 left",
                            style = MaterialTheme.typography.bodySmall,
                            color = cardSubColor
                        )
                    }
                    if (item.isPinned) {
                        Text(
                            text = "Pinned",
                            style = MaterialTheme.typography.labelLarge,
                            color = cardTitleColor
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Text(
                text = formatCardDate(item.lastSavedAt),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = cardSubColor
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


private data class PreviewStroke(
    val points: List<Offset>,
    val color: Color,
    val width: Float,
    val isEraser: Boolean
)

@Composable
private fun DrawingPreview(
    strokePaths: List<String>,
    modifier: Modifier = Modifier
) {
    val strokes = remember(strokePaths) { decodePreviewStrokes(strokePaths) }
    Canvas(modifier = modifier) {
        if (strokes.isEmpty()) return@Canvas

        val allPoints = strokes.flatMap { it.points }
        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }
        val sourceWidth = (maxX - minX).coerceAtLeast(1f)
        val sourceHeight = (maxY - minY).coerceAtLeast(1f)
        val scale = kotlin.math.min(size.width / sourceWidth, size.height / sourceHeight) * 0.9f
        val dx = (size.width - sourceWidth * scale) / 2f
        val dy = (size.height - sourceHeight * scale) / 2f

        strokes.forEach { stroke ->
            stroke.points.zipWithNext { start, end ->
                val mappedStart = Offset((start.x - minX) * scale + dx, (start.y - minY) * scale + dy)
                val mappedEnd = Offset((end.x - minX) * scale + dx, (end.y - minY) * scale + dy)
                drawLine(
                    color = if (stroke.isEraser) Color.Transparent else stroke.color,
                    start = mappedStart,
                    end = mappedEnd,
                    strokeWidth = (stroke.width * scale).coerceIn(1.2f, 10f),
                    cap = StrokeCap.Round
                )
            }
        }

        drawRect(
            color = Color.White.copy(alpha = 0.08f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun decodePreviewStrokes(paths: List<String>): List<PreviewStroke> =
    paths.mapNotNull { encoded ->
        val segments = encoded.split("|", limit = 4)
        if (segments.size < 4) return@mapNotNull null
        val color = segments[0].toLongOrNull()?.let { Color(it) } ?: return@mapNotNull null
        val width = segments[1].toFloatOrNull() ?: return@mapNotNull null
        val isEraser = segments[2].toBooleanStrictOrNull() ?: false
        val points = segments[3]
            .split(';')
            .mapNotNull { pair ->
                val xy = pair.split(',', limit = 2)
                if (xy.size != 2) return@mapNotNull null
                val x = xy[0].toFloatOrNull() ?: return@mapNotNull null
                val y = xy[1].toFloatOrNull() ?: return@mapNotNull null
                Offset(x, y)
            }
        if (points.size < 2) null else PreviewStroke(points = points, color = color, width = width, isEraser = isEraser)
    }

private fun formatCardDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true, backgroundColor = 0xFF25343F)
@Composable
private fun HomeScreenPreview() {
    MotesTheme {
        HomeScreen(navController = androidx.navigation.compose.rememberNavController())
    }
}
