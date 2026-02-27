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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeNoteType {
    NOTE,
    CHECKLIST,
    DRAWING
}

enum class HomeFilter {
    ALL,
    NOTE,
    CHECKLIST,
    DRAWING
}

data class HomeListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val isPinned: Boolean,
    val type: HomeNoteType
)

sealed class UiMode {
    data object Normal : UiMode()
    data class Selection(val selectedIds: Set<String>) : UiMode()
}

class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val checklistRepository: ChecklistRepository,
    private val drawingRepository: DrawingRepository
) : ViewModel() {

    val notes: StateFlow<List<NoteEntity>> =
        noteRepository.observeActive().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiMode = MutableStateFlow<UiMode>(UiMode.Normal)
    val uiMode: StateFlow<UiMode> = _uiMode.asStateFlow()

    private val _filter = MutableStateFlow(HomeFilter.ALL)
    val filter: StateFlow<HomeFilter> = _filter.asStateFlow()

    val items: StateFlow<List<HomeListItem>> = combine(
        noteRepository.observeActive().map { list ->
            list.map { note ->
                HomeListItem(
                    id = note.id,
                    title = note.title.ifBlank { "Untitled note" },
                    subtitle = note.content.ifBlank { "(empty note)" },
                    isPinned = note.isPinned,
                    type = HomeNoteType.NOTE
                )
            }
        },
        checklistRepository.observeActive().map { list ->
            list.map { checklist ->
                HomeListItem(
                    id = checklist.id,
                    title = checklist.title.ifBlank { "Untitled checklist" },
                    subtitle = summarizeChecklist(checklist),
                    isPinned = checklist.isPinned,
                    type = HomeNoteType.CHECKLIST
                )
            }
        },
        drawingRepository.observeActive().map { list ->
            list.map { drawing ->
                HomeListItem(
                    id = drawing.id,
                    title = drawing.title.ifBlank { "Untitled drawing" },
                    subtitle = "${drawing.strokePaths.size} stroke(s)",
                    isPinned = drawing.isPinned,
                    type = HomeNoteType.DRAWING
                )
            }
        },
        filter
    ) { notesItems, checklistItems, drawingItems, activeFilter ->
        val merged = (notesItems + checklistItems + drawingItems)
            .sortedWith(compareByDescending<HomeListItem> { it.isPinned }.thenBy { it.title.lowercase() })
        when (activeFilter) {
            HomeFilter.ALL -> merged
            HomeFilter.NOTE -> merged.filter { it.type == HomeNoteType.NOTE }
            HomeFilter.CHECKLIST -> merged.filter { it.type == HomeNoteType.CHECKLIST }
            HomeFilter.DRAWING -> merged.filter { it.type == HomeNoteType.DRAWING }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun cycleFilter() {
        _filter.update {
            when (it) {
                HomeFilter.ALL -> HomeFilter.NOTE
                HomeFilter.NOTE -> HomeFilter.CHECKLIST
                HomeFilter.CHECKLIST -> HomeFilter.DRAWING
                HomeFilter.DRAWING -> HomeFilter.ALL
            }
        }
    }

    fun onNoteClick(id: String) {
        _uiMode.update { mode ->
            when (mode) {
                UiMode.Normal -> mode
                is UiMode.Selection -> {
                    val updated = mode.selectedIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    if (updated.isEmpty()) UiMode.Normal else UiMode.Selection(updated)
                }
            }
        }
    }

    fun onNoteLongPress(id: String) {
        _uiMode.update { mode ->
            when (mode) {
                UiMode.Normal -> UiMode.Selection(setOf(id))
                is UiMode.Selection -> {
                    val updated = mode.selectedIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    if (updated.isEmpty()) UiMode.Normal else UiMode.Selection(updated)
                }
            }
        }
    }

    fun clearSelection() {
        _uiMode.value = UiMode.Normal
    }

    fun onArchiveSelected() {}
    fun onDeleteSelected() {}
    fun onPinSelected() {}
    fun onShareSelected() {}

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
            noteRepository.upsert(note)
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
            checklistRepository.upsert(checklist)
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
            drawingRepository.upsert(drawing)
            onCreated(drawing.id)
        }
    }

    private fun summarizeChecklist(checklist: ChecklistEntity): String {
        val done = checklist.items.count { it.isChecked }
        return if (checklist.items.isEmpty()) "No checklist items" else "$done/${checklist.items.size} done"
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
