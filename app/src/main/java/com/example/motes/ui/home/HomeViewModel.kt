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
    val type: HomeNoteType,
    val checklistProgress: Float? = null,
    val checklistCompletionLabel: String? = null
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

    private val checklists: StateFlow<List<ChecklistEntity>> =
        checklistRepository.observeActive().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val drawings: StateFlow<List<DrawingEntity>> =
        drawingRepository.observeActive().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiMode = MutableStateFlow<UiMode>(UiMode.Normal)
    val uiMode: StateFlow<UiMode> = _uiMode.asStateFlow()

    private val _filter = MutableStateFlow(HomeFilter.ALL)
    val filter: StateFlow<HomeFilter> = _filter.asStateFlow()

    val items: StateFlow<List<HomeListItem>> = combine(
        notes.map { list ->
            list.map { note ->
                HomeListItem(
                    id = note.id.toString(),
                    title = note.title.ifBlank { "Untitled note" },
                    subtitle = notePreview(note.content),
                    isPinned = note.isPinned,
                    type = HomeNoteType.NOTE
                )
            }
        },
        checklists.map { list ->
            list.map { checklist ->
                val checkedCount = checklist.items.count { it.isChecked }
                val totalCount = checklist.items.size
                val progress = if (totalCount == 0) 0f else checkedCount.toFloat() / totalCount.toFloat()
                HomeListItem(
                    id = checklist.id,
                    title = checklist.title.ifBlank { "Untitled checklist" },
                    subtitle = checklist.items.joinToString(" ") { it.text }.take(100).ifBlank { "(empty checklist)" },
                    isPinned = checklist.isPinned,
                    type = HomeNoteType.CHECKLIST,
                    checklistProgress = progress,
                    checklistCompletionLabel = "$checkedCount checked • ${totalCount - checkedCount} left"
                )
            }
        },
        drawings.map { list ->
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

    fun setFilter(filter: HomeFilter) {
        _filter.update { current ->
            if (current == filter) HomeFilter.ALL else filter
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

    fun archiveSelected() {
        val selected = (uiMode.value as? UiMode.Selection)?.selectedIds.orEmpty()
        if (selected.isEmpty()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            selected.forEach { id ->
                notes.value.firstOrNull { it.id.toString() == id }?.let { note ->
                    noteRepository.update(note.copy(isArchived = true, updatedAt = now))
                }
                checklists.value.firstOrNull { it.id == id }?.let { checklist ->
                    checklistRepository.upsert(checklist.copy(isArchived = true, updatedAt = now))
                }
                drawings.value.firstOrNull { it.id == id }?.let { drawing ->
                    drawingRepository.upsert(drawing.copy(isArchived = true, updatedAt = now))
                }
            }
            clearSelection()
        }
    }

    fun deleteSelected() {
        val selected = (uiMode.value as? UiMode.Selection)?.selectedIds.orEmpty()
        if (selected.isEmpty()) return

        viewModelScope.launch {
            selected.forEach { id ->
                id.toLongOrNull()?.let { noteRepository.deletePermanently(it) }
                checklistRepository.deletePermanently(id)
                drawingRepository.deletePermanently(id)
            }
            clearSelection()
        }
    }

    fun buildShareTextForSelection(): String {
        val selected = (uiMode.value as? UiMode.Selection)?.selectedIds.orEmpty()
        if (selected.isEmpty()) return ""

        val selectedItems = items.value.filter { it.id in selected }
        return selectedItems.joinToString(separator = "\n\n") { item ->
            val type = when (item.type) {
                HomeNoteType.NOTE -> "Note"
                HomeNoteType.CHECKLIST -> "Checklist"
                HomeNoteType.DRAWING -> "Drawing"
            }
            "[$type] ${item.title}\n${item.subtitle}"
        }
    }

    fun createNewNote(onCreated: (Long) -> Unit) {
        onCreated(-1L)
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

    private fun notePreview(content: String): String {
        return content
            .lineSequence()
            .filterNot { line ->
                val trimmed = line.trim()
                trimmed.startsWith("[[image:") && trimmed.endsWith("]]")
            }
            .joinToString("\n")
            .trim()
            .take(100)
            .ifBlank { "(empty note)" }
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
