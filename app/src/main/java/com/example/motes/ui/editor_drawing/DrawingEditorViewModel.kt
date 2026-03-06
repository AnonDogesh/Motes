package com.example.motes.ui.editor_drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motes.data.entity.DrawingEntity
import com.example.motes.data.repository.DrawingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val AUTO_SAVE_DEBOUNCE_MS = 500L

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
    val drawingId: String,
    val title: String = "",
    val strokes: List<DrawingStrokeUi> = emptyList(),
    val selectedColor: Long = 0xFFEAEFEFL,
    val strokeWidth: Float = 8f,
    val selectedTool: DrawingTool = DrawingTool.Brush,
    val lastEditedLabel: String = "Not saved yet",
    val createdAt: Long = System.currentTimeMillis(),
    val cardColor: Long? = null
)

class DrawingEditorViewModel(
    private val drawingRepository: DrawingRepository,
    private val editorId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrawingEditorUiState(drawingId = editorId))
    val uiState: StateFlow<DrawingEditorUiState> = _uiState.asStateFlow()

    init {
        observeDrawing()
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

    fun setTitle(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun setCardColor(color: Long?) {
        _uiState.update { it.copy(cardColor = color) }
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
    }

    fun undo() {
        _uiState.update { state ->
            if (state.strokes.isEmpty()) state else state.copy(strokes = state.strokes.dropLast(1))
        }
    }

    fun clear() {
        _uiState.update { it.copy(strokes = emptyList()) }
    }

    private fun observeDrawing() {
        val id = editorId
        viewModelScope.launch {
            drawingRepository.observeById(id).collectLatest { drawing ->
                if (drawing != null) {
                    _uiState.update {
                        it.copy(
                            title = drawing.title,
                            strokes = decodeStrokes(drawing.strokePaths),
                            createdAt = drawing.createdAt,
                            cardColor = drawing.cardColor
                        )
                    }
                }
            }
        }
    }

    private fun observeAutoSave() {
        viewModelScope.launch {
            uiState
                .filter { it.drawingId.isNotBlank() }
                .debounce(AUTO_SAVE_DEBOUNCE_MS)
                .collectLatest { state ->
                    if (state.title.isBlank() && state.strokes.isEmpty()) return@collectLatest
                    val id = state.drawingId
                    drawingRepository.upsert(
                        DrawingEntity(
                            id = id,
                            title = state.title,
                            strokePaths = encodeStrokes(state.strokes),
                            createdAt = state.createdAt,
                            updatedAt = System.currentTimeMillis(),
                            isPinned = false,
                            isArchived = false,
                            cardColor = state.cardColor
                        )
                    )
                    _uiState.update { it.copy(lastEditedLabel = "Last edited just now") }
                }
        }
    }

    private fun encodeStrokes(strokes: List<DrawingStrokeUi>): List<String> =
        strokes.map { stroke ->
            val pointPart = stroke.points.joinToString(";") { "${it.x},${it.y}" }
            "${stroke.color}|${stroke.width}|${stroke.isEraser}|$pointPart"
        }

    private fun decodeStrokes(paths: List<String>): List<DrawingStrokeUi> =
        paths.mapNotNull { encoded ->
            val segments = encoded.split("|", limit = 4)
            if (segments.size < 4) return@mapNotNull null
            val color = segments[0].toLongOrNull() ?: return@mapNotNull null
            val width = segments[1].toFloatOrNull() ?: return@mapNotNull null
            val isEraser = segments[2].toBooleanStrictOrNull() ?: false
            val points = segments[3]
                .split(';')
                .mapNotNull { pair ->
                    val xy = pair.split(',', limit = 2)
                    if (xy.size != 2) return@mapNotNull null
                    val x = xy[0].toFloatOrNull() ?: return@mapNotNull null
                    val y = xy[1].toFloatOrNull() ?: return@mapNotNull null
                    DrawPoint(x, y)
                }
            if (points.size < 2) null else DrawingStrokeUi(points, color, width, isEraser)
        }
}
