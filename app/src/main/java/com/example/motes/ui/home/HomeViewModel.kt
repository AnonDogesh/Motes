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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
