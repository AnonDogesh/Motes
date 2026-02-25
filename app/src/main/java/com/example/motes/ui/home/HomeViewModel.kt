package com.example.motes.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class UiMode {
    data object Normal : UiMode()
    data class Selection(val selectedIds: Set<String>) : UiMode()
}

data class HomeItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val isPinned: Boolean
)

data class HomeUiState(
    val items: List<HomeItemUiModel> = emptyList(),
    val uiMode: UiMode = UiMode.Normal
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            items = sortItems(fakeHomeNotes)
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onNoteClick(id: String) {
        _uiState.update { state ->
            when (val mode = state.uiMode) {
                UiMode.Normal -> state
                is UiMode.Selection -> {
                    val updated = mode.selectedIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    state.copy(
                        uiMode = if (updated.isEmpty()) UiMode.Normal else UiMode.Selection(updated)
                    )
                }
            }
        }
    }

    fun onNoteLongPress(id: String) {
        _uiState.update { state ->
            when (val mode = state.uiMode) {
                UiMode.Normal -> state.copy(uiMode = UiMode.Selection(setOf(id)))
                is UiMode.Selection -> {
                    val updated = mode.selectedIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    state.copy(
                        uiMode = if (updated.isEmpty()) UiMode.Normal else UiMode.Selection(updated)
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(uiMode = UiMode.Normal) }
    }
}

private val fakeHomeNotes = listOf(
    HomeItemUiModel("n-1", "Project Outline", "Draft structure for quarterly plan.", true),
    HomeItemUiModel("n-2", "Shopping", "Milk, coffee, dish soap, paper towels.", true),
    HomeItemUiModel("n-3", "Meeting Notes", "Client sync highlights and next steps.", false),
    HomeItemUiModel("n-4", "Ideas", "A list of quick experiments to try.", false),
    HomeItemUiModel("n-5", "Journal", "Thoughts from today.", false),
    HomeItemUiModel("n-6", "Travel", "Packing checklist and itinerary notes.", false)
)


private fun sortItems(items: List<HomeItemUiModel>): List<HomeItemUiModel> =
    items.sortedWith(compareByDescending<HomeItemUiModel> { it.isPinned }.thenBy { it.title.lowercase() })
