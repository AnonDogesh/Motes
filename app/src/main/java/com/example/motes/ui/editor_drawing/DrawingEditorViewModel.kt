package com.example.motes.ui.editor_drawing

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

enum class DrawingTool {
    Brush,
    Eraser
}

data class DrawPoint(
    val x: Float,
    val y: Float
)

data class DrawingStrokeUi(
    val points: List<DrawPoint>,
    val color: Long,
    val width: Float,
    val isEraser: Boolean
)

data class DrawingEditorUiState(
    val drawingId: String?,
    val strokes: List<DrawingStrokeUi> = emptyList(),
    val selectedColor: Long = 0xFFEAEFEFL,
    val strokeWidth: Float = 8f,
    val selectedTool: DrawingTool = DrawingTool.Brush,
    val lastEditedLabel: String = "Not saved yet"
)

class DrawingEditorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val editorId = AppRoute.DrawingEditor.from(savedStateHandle)?.drawingId

    private val _uiState = MutableStateFlow(
        DrawingEditorUiState(
            drawingId = editorId
        )
    )
    val uiState: StateFlow<DrawingEditorUiState> = _uiState.asStateFlow()

    private val draftUpdates = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        observeAutoSave()
    }

    fun setColor(color: Long) {
        _uiState.update { it.copy(selectedColor = color, selectedTool = DrawingTool.Brush) }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(strokeWidth = width.coerceIn(2f, 28f)) }
    }

    fun setTool(tool: DrawingTool) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun addStroke(points: List<DrawPoint>) {
        if (points.size < 2) return

        _uiState.update { state ->
            state.copy(
                strokes = state.strokes + DrawingStrokeUi(
                    points = points,
                    color = state.selectedColor,
                    width = state.strokeWidth,
                    isEraser = state.selectedTool == DrawingTool.Eraser
                )
            )
        }
        queueAutoSave()
    }

    fun undo() {
        _uiState.update { state ->
            if (state.strokes.isEmpty()) state else state.copy(strokes = state.strokes.dropLast(1))
        }
        queueAutoSave()
    }

    fun clear() {
        _uiState.update { it.copy(strokes = emptyList()) }
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
}
