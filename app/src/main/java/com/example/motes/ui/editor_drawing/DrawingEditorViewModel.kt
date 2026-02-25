package com.example.motes.ui.editor_drawing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.motes.navigation.AppRoute

class DrawingEditorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val drawingId: String? = AppRoute.DrawingEditor.from(savedStateHandle)?.drawingId
}
