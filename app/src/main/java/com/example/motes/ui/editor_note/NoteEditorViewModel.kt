package com.example.motes.ui.editor_note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.motes.navigation.AppRoute

class NoteEditorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val noteId: String? = AppRoute.NoteEditor.from(savedStateHandle)?.noteId
}
