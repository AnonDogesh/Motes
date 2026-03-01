package com.example.motes.ui.editor_note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motes.data.entity.NoteEntity
import com.example.motes.data.repository.NoteRepository
import com.example.motes.navigation.AppRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val AUTO_SAVE_DEBOUNCE_MS = 500L

data class NoteEditorUiState(
    val noteId: Long = -1L,
    val sectionLabel: String = "NOTE",
    val lastEditedLabel: String = "Not saved yet",
    val title: String = "",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class NoteEditorViewModel(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val initialNoteId: Long = savedStateHandle[AppRoute.NoteEditor.ARG_NOTE_ID] ?: -1L

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = initialNoteId))
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        if (initialNoteId != -1L) {
            observeNote(initialNoteId)
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value) }
        scheduleSave()
    }

    fun onBodyChanged(value: String) {
        _uiState.update { it.copy(body = value) }
        scheduleSave()
    }

    private fun observeNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.observeById(noteId).collectLatest { note ->
                if (note != null) {
                    _uiState.update {
                        it.copy(
                            noteId = note.id,
                            title = note.title,
                            body = note.content,
                            createdAt = note.createdAt,
                            lastEditedLabel = "Last edited just now"
                        )
                    }
                }
            }
        }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DEBOUNCE_MS)
            saveDraft()
        }
    }

    private suspend fun saveDraft() {
        val state = uiState.value
        if (state.title.isBlank() && state.body.isBlank()) return

        val now = System.currentTimeMillis()
        if (state.noteId == -1L) {
            val insertedId = noteRepository.insert(
                NoteEntity(
                    title = state.title,
                    content = state.body,
                    createdAt = state.createdAt,
                    updatedAt = now,
                    isPinned = false,
                    isArchived = false
                )
            )
            _uiState.update {
                it.copy(noteId = insertedId, lastEditedLabel = "Last edited just now")
            }
            return
        }

        noteRepository.update(
            NoteEntity(
                id = state.noteId,
                title = state.title,
                content = state.body,
                createdAt = state.createdAt,
                updatedAt = now,
                isPinned = false,
                isArchived = false
            )
        )
        _uiState.update { it.copy(lastEditedLabel = "Last edited just now") }
    }
}

class NoteEditorViewModelFactory(
    private val noteRepository: NoteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditorViewModel::class.java)) {
            return NoteEditorViewModel(noteRepository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
