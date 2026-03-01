package com.example.motes.ui.editor_checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motes.data.entity.ChecklistEntity
import com.example.motes.data.entity.ChecklistItem
import com.example.motes.data.repository.ChecklistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val AUTO_SAVE_DEBOUNCE_MS = 500L

data class ChecklistEditorItemUi(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)

data class ChecklistEditorUiState(
    val checklistId: String,
    val title: String = "",
    val lastEditedLabel: String = "Not saved yet",
    val items: List<ChecklistEditorItemUi> = emptyList(),
    val checkedCount: Int = 0,
    val progress: Float = 0f,
    val completionLabel: String = "0 / 0 completed",
    val createdAt: Long = System.currentTimeMillis()
)

class ChecklistEditorViewModel(
    private val checklistRepository: ChecklistRepository,
    private val editorId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        calculateDerived(ChecklistEditorUiState(checklistId = editorId))
    )
    val uiState: StateFlow<ChecklistEditorUiState> = _uiState.asStateFlow()

    init {
        observeChecklist()
        observeAutoSave()
    }

    fun onTitleChanged(value: String) {
        _uiState.update { calculateDerived(it.copy(title = value)) }
    }

    fun onItemTextChanged(itemId: String, value: String) {
        _uiState.update { state ->
            calculateDerived(state.copy(items = state.items.map { if (it.id == itemId) it.copy(text = value) else it }))
        }
    }

    fun onItemCheckedChanged(itemId: String, checked: Boolean) {
        _uiState.update { state ->
            calculateDerived(state.copy(items = state.items.map { if (it.id == itemId) it.copy(isChecked = checked) else it }))
        }
    }

    fun addItem(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        _uiState.update { state -> calculateDerived(state.copy(items = state.items + ChecklistEditorItemUi(text = trimmed))) }
    }

    fun deleteItem(itemId: String) {
        _uiState.update { state -> calculateDerived(state.copy(items = state.items.filterNot { it.id == itemId })) }
    }

    private fun observeChecklist() {
        val id = editorId
        viewModelScope.launch {
            checklistRepository.observeById(id).collectLatest { checklist ->
                if (checklist != null) {
                    _uiState.update {
                        calculateDerived(
                            it.copy(
                                title = checklist.title,
                                items = checklist.items.map { item -> ChecklistEditorItemUi(text = item.text, isChecked = item.isChecked) },
                                createdAt = checklist.createdAt
                            )
                        )
                    }
                }
            }
        }
    }

    private fun observeAutoSave() {
        viewModelScope.launch {
            uiState
                .filter { it.checklistId.isNotBlank() }
                .debounce(AUTO_SAVE_DEBOUNCE_MS)
                .collectLatest { state ->
                    if (state.title.isBlank() && state.items.all { it.text.isBlank() }) return@collectLatest
                    val id = state.checklistId
                    checklistRepository.upsert(
                        ChecklistEntity(
                            id = id,
                            title = state.title,
                            items = state.items.map { ChecklistItem(text = it.text, isChecked = it.isChecked) },
                            createdAt = state.createdAt,
                            updatedAt = System.currentTimeMillis(),
                            isPinned = false,
                            isArchived = false
                        )
                    )
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
