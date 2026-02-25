package com.example.motes.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.motes.ui.theme.MotesTheme

private data class HomeNoteUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val isPinned: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNoteClick: (String) -> Unit = {},
    onNoteLongPress: (String) -> Unit = {}
) {
    val previewItems = remember { fakeHomeNotes }
    val pinnedItems = remember(previewItems) { previewItems.filter { it.isPinned } }
    val allItems = remember(previewItems) { previewItems }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(minSize = 180.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pinnedItems.isNotEmpty()) {
            stickyHeader(key = "pinned_header", span = fullLineSpan()) {
                SectionHeader(title = "Pinned")
            }
            items(items = pinnedItems, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { onNoteLongPress(note.id) }
                )
            }
        }

        stickyHeader(key = "all_header", span = fullLineSpan()) {
            SectionHeader(title = "All Notes")
        }
        items(items = allItems, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note.id) },
                onLongClick = { onNoteLongPress(note.id) }
            )
        }
    }
}

private fun fullLineSpan(): (LazyGridItemSpanScope) -> GridItemSpan = {
    GridItemSpan(maxLineSpan)
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: HomeNoteUiModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val animatedElevation = animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        label = "note_card_elevation"
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
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation.value)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = note.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (note.isPinned) {
                Text(
                    text = "Pinned",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private val fakeHomeNotes = listOf(
    HomeNoteUiModel("n-1", "Project Outline", "Draft structure for quarterly plan.", true),
    HomeNoteUiModel("n-2", "Shopping", "Milk, coffee, dish soap, paper towels.", true),
    HomeNoteUiModel("n-3", "Meeting Notes", "Client sync highlights and next steps.", false),
    HomeNoteUiModel("n-4", "Ideas", "A list of quick experiments to try.", false),
    HomeNoteUiModel("n-5", "Journal", "Thoughts from today.", false),
    HomeNoteUiModel("n-6", "Travel", "Packing checklist and itinerary notes.", false)
)

@Preview(showBackground = true, backgroundColor = 0xFF25343F)
@Composable
private fun HomeScreenPreview() {
    MotesTheme {
        HomeScreen()
    }
}
