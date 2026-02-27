package com.example.motes.ui.editor_note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motes.data.entity.NoteEntity
import com.example.motes.data.repository.NoteRepository
import com.example.motes.navigation.AppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val AUTO_SAVE_DEBOUNCE_MS = 500L

data class NoteEditorUiState(
    val noteId: String?,
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
    private val editorId = AppRoute.NoteEditor.from(savedStateHandle)?.noteId

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = editorId))
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        observeNote()
        observeAutoSave()
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onBodyChanged(value: String) {
        _uiState.update { it.copy(body = value) }
    }

    private fun observeNote() {
        val id = editorId ?: return
        viewModelScope.launch {
            noteRepository.observeById(id).collectLatest { note ->
                if (note != null) {
                    _uiState.update {
                        it.copy(
                            title = note.title,
                            body = note.content,
                            createdAt = note.createdAt
                        )
                    }
                }
            }
        }
    }

    private fun observeAutoSave() {
        viewModelScope.launch {
            uiState
                .filter { !it.noteId.isNullOrBlank() }
                .debounce(AUTO_SAVE_DEBOUNCE_MS)
                .collectLatest { state ->
                    saveDraft(state)
                }
        }
    }

    private suspend fun saveDraft(state: NoteEditorUiState) {
        val id = state.noteId ?: return
        if (state.title.isBlank() && state.body.isBlank()) return

        noteRepository.upsert(
            NoteEntity(
                id = id,
                title = state.title,
                content = state.body,
                createdAt = state.createdAt,
                updatedAt = System.currentTimeMillis(),
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
