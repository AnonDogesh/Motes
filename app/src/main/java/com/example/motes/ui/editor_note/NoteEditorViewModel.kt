package com.example.motes.ui.editor_note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motes.data.entity.NoteEntity
import com.example.motes.data.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val AUTO_SAVE_DEBOUNCE_MS = 2500L
private const val IMAGE_LINE_PREFIX = "[[image:"
private const val IMAGE_LINE_SUFFIX = "]]"
private const val FONT_LINE_PREFIX = "[[font:"
private const val FONT_LINE_SUFFIX = "]]"

data class NoteEditorUiState(
    val noteId: Long = -1L,
    val sectionLabel: String = "NOTE",
    val lastEditedLabel: String = "Not saved yet",
    val title: String = "",
    val body: String = "",
    val imageUris: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isBoldEnabled: Boolean = false,
    val isItalicEnabled: Boolean = false,
    val isUnderlineEnabled: Boolean = false,
    val cardColor: Long? = null,
    val selectedFontFamily: String = "sans-serif"
)

class NoteEditorViewModel(
    private val noteRepository: NoteRepository,
    private val initialNoteId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = initialNoteId))
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        if (initialNoteId != -1L) {
            loadInitialNote(initialNoteId)
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value) }
        scheduleSave()
    }

    fun onBodyChanged(value: String) {
        _uiState.update { it.copy(body = value) }
        scheduleSave()
    }

    fun addImage(uri: String) {
        if (uri.isBlank()) return
        _uiState.update { state ->
            if (state.imageUris.contains(uri)) state else state.copy(imageUris = state.imageUris + uri)
        }
        scheduleSave()
    }

    fun removeImage(uri: String) {
        _uiState.update { state -> state.copy(imageUris = state.imageUris.filterNot { it == uri }) }
        scheduleSave()
    }

    fun toggleBold() {
        _uiState.update { it.copy(isBoldEnabled = !it.isBoldEnabled) }
    }

    fun toggleItalic() {
        _uiState.update { it.copy(isItalicEnabled = !it.isItalicEnabled) }
    }

    fun toggleUnderline() {
        _uiState.update { it.copy(isUnderlineEnabled = !it.isUnderlineEnabled) }
    }

    fun setCardColor(color: Long?) {
        _uiState.update { it.copy(cardColor = color) }
        scheduleSave()
    }

    fun setFontFamily(fontFamily: String) {
        if (fontFamily.isBlank()) return
        _uiState.update { it.copy(selectedFontFamily = fontFamily) }
        scheduleSave()
    }

    private fun loadInitialNote(noteId: Long) {
        viewModelScope.launch {
            val note = noteRepository.observeById(noteId).first() ?: return@launch
            val (plainBody, images, fontFamily) = decodeNoteContent(note.content)
            _uiState.update {
                it.copy(
                    noteId = note.id,
                    title = note.title,
                    body = plainBody,
                    imageUris = images,
                    createdAt = note.createdAt,
                    lastEditedLabel = "Last edited just now",
                    cardColor = note.cardColor,
                    selectedFontFamily = fontFamily
                )
            }
        }
    }


    suspend fun saveNow() {
        saveJob?.cancel()
        saveDraft()
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DEBOUNCE_MS)
            saveDraft()
        }
    }

    private suspend fun saveDraft() {
        val state = uiState.value
        if (state.title.isBlank() && state.body.isBlank() && state.imageUris.isEmpty()) return

        val now = System.currentTimeMillis()
        val encodedContent = encodeNoteContent(state.body, state.imageUris, state.selectedFontFamily)

        if (state.noteId == -1L) {
            val insertedId = noteRepository.insert(
                NoteEntity(
                    title = state.title,
                    content = encodedContent,
                    createdAt = state.createdAt,
                    updatedAt = now,
                    isPinned = false,
                    isArchived = false,
                    cardColor = state.cardColor
                )
            )
            _uiState.update {
                it.copy(noteId = insertedId, lastEditedLabel = "Last edited just now")
            }
            return
        }

        noteRepository.update(
            NoteEntity(
                id = state.noteId,
                title = state.title,
                content = encodedContent,
                createdAt = state.createdAt,
                updatedAt = now,
                isPinned = false,
                isArchived = false,
                cardColor = state.cardColor
            )
        )
        _uiState.update { it.copy(lastEditedLabel = "Last edited just now") }
    }

    private fun encodeNoteContent(body: String, imageUris: List<String>, fontFamily: String): String {
        val imageLines = imageUris.joinToString(separator = "\n") { uri -> "$IMAGE_LINE_PREFIX$uri$IMAGE_LINE_SUFFIX" }
        val fontLine = if (fontFamily.isBlank() || fontFamily == "sans-serif") "" else "$FONT_LINE_PREFIX$fontFamily$FONT_LINE_SUFFIX"

        val parts = listOf(body, imageLines, fontLine).filter { it.isNotBlank() }
        return parts.joinToString(separator = "\n")
    }

    private fun decodeNoteContent(content: String): Triple<String, List<String>, String> {
        if (content.isBlank()) return Triple("", emptyList(), "sans-serif")
        val bodyLines = mutableListOf<String>()
        val imageUris = mutableListOf<String>()
        var fontFamily = "sans-serif"

        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith(IMAGE_LINE_PREFIX) && trimmed.endsWith(IMAGE_LINE_SUFFIX)) {
                val uri = trimmed.removePrefix(IMAGE_LINE_PREFIX).removeSuffix(IMAGE_LINE_SUFFIX)
                if (uri.isNotBlank()) imageUris.add(uri)
            } else if (trimmed.startsWith(FONT_LINE_PREFIX) && trimmed.endsWith(FONT_LINE_SUFFIX)) {
                val parsedFont = trimmed.removePrefix(FONT_LINE_PREFIX).removeSuffix(FONT_LINE_SUFFIX)
                if (parsedFont.isNotBlank()) fontFamily = parsedFont
            } else {
                bodyLines.add(line)
            }
        }

        return Triple(bodyLines.joinToString("\n"), imageUris, fontFamily)
    }
}
