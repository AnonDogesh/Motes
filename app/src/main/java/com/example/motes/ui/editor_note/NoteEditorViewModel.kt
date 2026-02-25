package com.example.motes.ui.editor_note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motes.navigation.AppRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val AUTO_SAVE_DEBOUNCE_MS = 700L

data class NoteEditorUiState(
    val noteId: String?,
    val sectionLabel: String = "NOTE",
    val lastEditedLabel: String = "Not saved yet",
    val initialTitle: String = "",
    val initialBody: String = ""
)

class NoteEditorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val editorId = AppRoute.NoteEditor.from(savedStateHandle)?.noteId

    private val _uiState = MutableStateFlow(
        NoteEditorUiState(
            noteId = editorId,
            initialTitle = "",
            initialBody = ""
        )
    )
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private val draftUpdates = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)

    init {
        observeAutoSave()
    }

    fun onDraftChanged(title: String, body: String) {
        draftUpdates.tryEmit(title to body)
    }

    private fun observeAutoSave() {
        viewModelScope.launch {
            draftUpdates
                .debounce(AUTO_SAVE_DEBOUNCE_MS)
                .collect { (title, body) ->
                    saveDraft(title, body)
                }
        }
    }

    private fun saveDraft(title: String, body: String) {
        // Placeholder save pipeline until repository integration is wired.
        _uiState.update {
            it.copy(
                lastEditedLabel = "Last edited just now"
            )
        }
    }
}
