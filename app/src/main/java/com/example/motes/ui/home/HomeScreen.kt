@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.motes.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.motes.data.AppContainer
import com.example.motes.navigation.AppRoute
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
    val selectedIds = selectionMode?.selectedIds.orEmpty()
    val inSelectionMode = selectionMode != null

    BackHandler(enabled = inSelectionMode) {
        viewModel.clearSelection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (inSelectionMode) "${selectedIds.size} selected" else "Motes",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (inSelectionMode) {
                        TextButton(onClick = viewModel::clearSelection) { Text("Cancel") }
                    } else {
                        TextButton(onClick = viewModel::cycleFilter) {
                            Text(
                                text = when (filter) {
                                    HomeFilter.ALL -> "Filter: All"
                                    HomeFilter.NOTE -> "Filter: Notes"
                                    HomeFilter.CHECKLIST -> "Filter: Checklists"
                                    HomeFilter.DRAWING -> "Filter: Drawings"
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            SpeedDialFab(
                onNewNote = { viewModel.createNewNote { id -> navController.navigate(AppRoute.NoteEditor(id).route) } },
                onNewChecklist = { viewModel.createNewChecklist { id -> navController.navigate(AppRoute.ChecklistEditor(id).route) } },
                onNewDrawing = { viewModel.createNewDrawing { id -> navController.navigate(AppRoute.DrawingEditor(id).route) } }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = inSelectionMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                SelectionActionBar(
                    onArchive = viewModel::onArchiveSelected,
                    onDelete = viewModel::onDeleteSelected,
                    onPin = viewModel::onPinSelected,
                    onShare = viewModel::onShareSelected
                )
            }
        }
    ) { innerPadding ->
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
                    isSelected = selectedIds.contains(item.id),
                    onClick = {
                        if (inSelectionMode) {
                            viewModel.onNoteClick(item.id)
                        } else {
                            val route = when (item.type) {
                                HomeNoteType.NOTE -> AppRoute.NoteEditor(item.id.toLongOrNull() ?: -1L).route
                                HomeNoteType.CHECKLIST -> AppRoute.ChecklistEditor(item.id).route
                                HomeNoteType.DRAWING -> AppRoute.DrawingEditor(item.id).route
                            }
                            navController.navigate(route)
                        }
                    },
                    onLongClick = { viewModel.onNoteLongPress(item.id) }
                )
            }
        }
    }
}

@Composable
private fun SelectionActionBar(
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onShare: () -> Unit
) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onArchive) { Text("Archive") }
            TextButton(onClick = onDelete) { Text("Delete") }
            TextButton(onClick = onPin) { Text("Pin") }
            TextButton(onClick = onShare) { Text("Share") }
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            if (item.isPinned) {
                Text(
                    text = "Pinned",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
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
