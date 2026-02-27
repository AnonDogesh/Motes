package com.example.motes.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motes.data.entity.NoteEntity
import com.example.motes.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArchiveViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            noteRepository.observeArchived().collect { notes ->
                _uiState.update { it.copy(items = notes.map { note -> note.toArchiveItemUi() }) }
            }
        }
    }

    fun restore(id: String) {
        viewModelScope.launch {
            noteRepository.restore(id)
        }
    }

    fun deletePermanently(id: String) {
        viewModelScope.launch {
            noteRepository.deletePermanently(id)
        }
    }
}

data class ArchiveUiState(
    val items: List<ArchiveItemUi> = emptyList()
)

data class ArchiveItemUi(
    val id: String,
    val title: String,
    val preview: String,
    val updatedAt: Long,
    val isPinned: Boolean
)

private fun NoteEntity.toArchiveItemUi(): ArchiveItemUi = ArchiveItemUi(
    id = id,
    title = title.ifBlank { "Untitled" },
    preview = content,
    updatedAt = updatedAt,
    isPinned = isPinned
)

class ArchiveViewModelFactory(
    private val noteRepository: NoteRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ArchiveViewModel(noteRepository) as T
    }
}
