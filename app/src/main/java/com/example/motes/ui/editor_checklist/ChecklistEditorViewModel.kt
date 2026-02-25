package com.example.motes.ui.editor_checklist

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
import java.util.UUID

private const val AUTO_SAVE_DEBOUNCE_MS = 700L

data class ChecklistEditorItemUi(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)

data class ChecklistEditorUiState(
    val checklistId: String?,
    val title: String = "",
    val lastEditedLabel: String = "Not saved yet",
    val items: List<ChecklistEditorItemUi> = emptyList(),
    val checkedCount: Int = 0,
    val progress: Float = 0f,
    val completionLabel: String = "0 / 0 completed"
)

class ChecklistEditorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val editorId = AppRoute.ChecklistEditor.from(savedStateHandle)?.checklistId

    private val _uiState = MutableStateFlow(
        calculateDerived(
            ChecklistEditorUiState(
                checklistId = editorId,
                title = "Checklist",
                items = listOf(
                    ChecklistEditorItemUi(text = "First checklist item"),
                    ChecklistEditorItemUi(text = "Second checklist item")
                )
            )
        )
    )
    val uiState: StateFlow<ChecklistEditorUiState> = _uiState.asStateFlow()

    private val draftUpdates = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        observeAutoSave()
    }

    fun onTitleChanged(value: String) {
        _uiState.update { calculateDerived(it.copy(title = value)) }
        queueAutoSave()
    }

    fun onItemTextChanged(itemId: String, value: String) {
        _uiState.update { state ->
            calculateDerived(state.copy(items = state.items.map { if (it.id == itemId) it.copy(text = value) else it }))
        }
        queueAutoSave()
    }

    fun onItemCheckedChanged(itemId: String, checked: Boolean) {
        _uiState.update { state ->
            calculateDerived(state.copy(items = state.items.map { if (it.id == itemId) it.copy(isChecked = checked) else it }))
        }
        queueAutoSave()
    }

    fun addItem(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        _uiState.update { state ->
            calculateDerived(state.copy(items = state.items + ChecklistEditorItemUi(text = trimmed)))
        }
        queueAutoSave()
    }

    fun deleteItem(itemId: String) {
        _uiState.update { state ->
            calculateDerived(state.copy(items = state.items.filterNot { it.id == itemId }))
        }
        queueAutoSave()
    }

    private fun queueAutoSave() {
        draftUpdates.tryEmit(Unit)
    }

    private fun observeAutoSave() {
        viewModelScope.launch {
            draftUpdates
                .debounce(AUTO_SAVE_DEBOUNCE_MS)
                .collect {
                    _uiState.update { it.copy(lastEditedLabel = "Last edited just now") }
                }
        }
    }

    private fun calculateDerived(state: ChecklistEditorUiState): ChecklistEditorUiState {
        val checkedCount = state.items.count { it.isChecked }
        val progress = if (state.items.isEmpty()) 0f else checkedCount.toFloat() / state.items.size.toFloat()
        return state.copy(
            checkedCount = checkedCount,
            progress = progress,
            completionLabel = "$checkedCount / ${state.items.size} completed"
        )
    }
}
