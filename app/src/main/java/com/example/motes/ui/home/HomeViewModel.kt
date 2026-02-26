package com.example.motes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motes.data.entity.ChecklistEntity
import com.example.motes.data.entity.DrawingEntity
import com.example.motes.data.entity.NoteEntity
import com.example.motes.data.repository.ChecklistRepository
import com.example.motes.data.repository.DrawingRepository
import com.example.motes.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeNoteType {
    NOTE,
    CHECKLIST,
    DRAWING
}

sealed class UiMode {
    data object Normal : UiMode()
    data class Selection(val selectedIds: Set<String>) : UiMode()
}

data class HomeItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val isPinned: Boolean,
    val type: HomeNoteType
)

data class HomeUiState(
    val items: List<HomeItemUiModel> = emptyList(),
    val uiMode: UiMode = UiMode.Normal
)

class HomeViewModel(
    private val noteRepository: NoteRepository? = null,
    private val checklistRepository: ChecklistRepository? = null,
    private val drawingRepository: DrawingRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            items = sortItems(fakeHomeNotes)
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onNoteClick(id: String) {
        _uiState.update { state ->
            when (val mode = state.uiMode) {
                UiMode.Normal -> state
                is UiMode.Selection -> {
                    val updated = mode.selectedIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    state.copy(
                        uiMode = if (updated.isEmpty()) UiMode.Normal else UiMode.Selection(updated)
                    )
                }
            }
        }
    }

    fun onNoteLongPress(id: String) {
        _uiState.update { state ->
            when (val mode = state.uiMode) {
                UiMode.Normal -> state.copy(uiMode = UiMode.Selection(setOf(id)))
                is UiMode.Selection -> {
                    val updated = mode.selectedIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    state.copy(
                        uiMode = if (updated.isEmpty()) UiMode.Normal else UiMode.Selection(updated)
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(uiMode = UiMode.Normal) }
    }

    fun onArchiveSelected() {
        // Stub for future multi-select archive behavior.
    }

    fun onDeleteSelected() {
        // Stub for future multi-select delete behavior.
    }

    fun onPinSelected() {
        // Stub for future multi-select pin behavior.
    }

    fun onShareSelected() {
        // Stub for future multi-select share behavior.
    }

    fun createNewNote(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = NoteEntity(
                title = "",
                content = "",
                createdAt = now,
                updatedAt = now,
                isPinned = false,
                isArchived = false
            )
            noteRepository?.upsert(note)
            onCreated(note.id)
        }
    }

    fun createNewChecklist(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val checklist = ChecklistEntity(
                title = "",
                items = emptyList(),
                createdAt = now,
                updatedAt = now,
                isPinned = false,
                isArchived = false
            )
            checklistRepository?.upsert(checklist)
            onCreated(checklist.id)
        }
    }

    fun createNewDrawing(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val drawing = DrawingEntity(
                title = "",
                strokePaths = emptyList(),
                createdAt = now,
                updatedAt = now,
                isPinned = false,
                isArchived = false
            )
            drawingRepository?.upsert(drawing)
            onCreated(drawing.id)
        }
    }
}

class HomeViewModelFactory(
    private val noteRepository: NoteRepository,
    private val checklistRepository: ChecklistRepository,
    private val drawingRepository: DrawingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(noteRepository, checklistRepository, drawingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private val fakeHomeNotes = listOf(
    HomeItemUiModel("n-1", "Project Outline", "Draft structure for quarterly plan.", true, HomeNoteType.NOTE),
    HomeItemUiModel("n-2", "Shopping", "Milk, coffee, dish soap, paper towels.", true, HomeNoteType.CHECKLIST),
    HomeItemUiModel("n-3", "Meeting Notes", "Client sync highlights and next steps.", false, HomeNoteType.NOTE),
    HomeItemUiModel("n-4", "Ideas", "A list of quick experiments to try.", false, HomeNoteType.DRAWING),
    HomeItemUiModel("n-5", "Journal", "Thoughts from today.", false, HomeNoteType.NOTE),
    HomeItemUiModel("n-6", "Travel", "Packing checklist and itinerary notes.", false, HomeNoteType.CHECKLIST)
)

private fun sortItems(items: List<HomeItemUiModel>): List<HomeItemUiModel> =
    items.sortedWith(compareByDescending<HomeItemUiModel> { it.isPinned }.thenBy { it.title.lowercase() })
