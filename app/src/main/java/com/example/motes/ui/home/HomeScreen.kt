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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.example.motes.ui.theme.MotesTheme

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
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = inSelectionMode) {
        viewModel.clearSelection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (inSelectionMode) "${selectedKeys.size} selected" else "Motes",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (inSelectionMode) {
                        TextButton(onClick = viewModel::clearSelection) { Text("Cancel") }
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
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                columns = GridCells.Adaptive(180.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
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
                                adjustViewBounds = true
                            }
                        },
                        update = { imageView ->
                            imageView.setImageURI(Uri.parse(item.previewImageUri))
                        }
                    )
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (item.type) {
                            HomeNoteType.NOTE -> "Note"
                            HomeNoteType.CHECKLIST -> "Checklist"
                            HomeNoteType.DRAWING -> "Drawing"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.type == HomeNoteType.CHECKLIST) {
                        LinearProgressIndicator(
                            progress = { item.checklistProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = item.checklistCompletionLabel ?: "0 checked • 0 left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.isPinned) {
                        Text(
                            text = "Pinned",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

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

@Preview(showBackground = true, backgroundColor = 0xFF25343F)
@Composable
private fun HomeScreenPreview() {
    MotesTheme {
        HomeScreen(navController = androidx.navigation.compose.rememberNavController())
    }
}
