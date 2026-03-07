package com.example.motes.ui.archive

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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ArchiveItemType { NOTE, CHECKLIST, DRAWING }

data class ArchiveItemUi(
    val key: String,
    val id: String,
    val type: ArchiveItemType,
    val title: String,
    val preview: String,
    val updatedAt: Long
)

data class ArchiveUiState(
    val items: List<ArchiveItemUi> = emptyList(),
    val selectedKeys: Set<String> = emptySet(),
    val query: String = ""
) {
    val filteredItems: List<ArchiveItemUi>
        get() {
            val normalized = query.trim().lowercase()
            if (normalized.isBlank()) return items
            return items.filter {
                it.title.lowercase().contains(normalized) || it.preview.lowercase().contains(normalized)
            }
        }
}

class ArchiveViewModel(
    private val noteRepository: NoteRepository,
    private val checklistRepository: ChecklistRepository,
    private val drawingRepository: DrawingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                noteRepository.observeArchived(),
                checklistRepository.observeArchived(),
                drawingRepository.observeArchived()
            ) { notes, checklists, drawings ->
                (notes.map { it.toArchiveItemUi() } + checklists.map { it.toArchiveItemUi() } + drawings.map { it.toArchiveItemUi() })
                    .sortedByDescending { it.updatedAt }
            }.collect { merged ->
                _uiState.update { state ->
                    state.copy(
                        items = merged,
                        selectedKeys = state.selectedKeys.intersect(merged.map { it.key }.toSet())
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun toggleSelection(key: String) {
        _uiState.update { state ->
            val updated = state.selectedKeys.toMutableSet().apply { if (!add(key)) remove(key) }
            state.copy(selectedKeys = updated)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedKeys = emptySet()) }
    }

    fun restoreSelected() {
        val selectedItems = uiState.value.items.filter { it.key in uiState.value.selectedKeys }
        viewModelScope.launch {
            selectedItems.forEach { item ->
                when (item.type) {
                    ArchiveItemType.NOTE -> noteRepository.restore(item.id.toLong())
                    ArchiveItemType.CHECKLIST -> checklistRepository.restore(item.id)
                    ArchiveItemType.DRAWING -> drawingRepository.restore(item.id)
                }
            }
            clearSelection()
        }
    }

    fun deleteSelectedPermanently() {
        val selectedItems = uiState.value.items.filter { it.key in uiState.value.selectedKeys }
        viewModelScope.launch {
            selectedItems.forEach { item ->
                when (item.type) {
                    ArchiveItemType.NOTE -> noteRepository.deletePermanently(item.id.toLong())
                    ArchiveItemType.CHECKLIST -> checklistRepository.deletePermanently(item.id)
                    ArchiveItemType.DRAWING -> drawingRepository.deletePermanently(item.id)
                }
            }
            clearSelection()
        }
    }
}

private fun NoteEntity.toArchiveItemUi(): ArchiveItemUi = ArchiveItemUi(
    key = "note:$id",
    id = id.toString(),
    type = ArchiveItemType.NOTE,
    title = title.ifBlank { "Untitled note" },
    preview = content.ifBlank { "(empty note)" },
    updatedAt = updatedAt
)

private fun ChecklistEntity.toArchiveItemUi(): ArchiveItemUi = ArchiveItemUi(
    key = "checklist:$id",
    id = id,
    type = ArchiveItemType.CHECKLIST,
    title = title.ifBlank { "Untitled checklist" },
    preview = items.joinToString(" • ") { it.text }.take(120).ifBlank { "(empty checklist)" },
    updatedAt = updatedAt
)

private fun DrawingEntity.toArchiveItemUi(): ArchiveItemUi = ArchiveItemUi(
    key = "drawing:$id",
    id = id,
    type = ArchiveItemType.DRAWING,
    title = title.ifBlank { "Untitled drawing" },
    preview = "${strokePaths.size} stroke(s)",
    updatedAt = updatedAt
)

class ArchiveViewModelFactory(
    private val noteRepository: NoteRepository,
    private val checklistRepository: ChecklistRepository,
    private val drawingRepository: DrawingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ArchiveViewModel(noteRepository, checklistRepository, drawingRepository) as T
    }
}
