package com.example.motes.ui.editor_checklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.motes.navigation.AppRoute

class ChecklistEditorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val checklistId: String? = AppRoute.ChecklistEditor.from(savedStateHandle)?.checklistId
}
