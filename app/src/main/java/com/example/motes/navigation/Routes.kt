package com.example.motes.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Sealed route model for type-safe navigation.
 *
 * Use typed route classes for navigation (e.g. [AppRoute.NoteEditor]) and
 * companion route specs when declaring NavHost destinations.
 */
sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Archive : AppRoute("archive")

    data class NoteEditor(val noteId: String) : AppRoute("note_editor/$noteId") {
        companion object Spec {
            const val ARG_NOTE_ID = "noteId"
            const val ROUTE = "note_editor/{$ARG_NOTE_ID}"
            val arguments = listOf(navArgument(ARG_NOTE_ID) { type = NavType.StringType })

            fun from(savedStateHandle: SavedStateHandle): NoteEditor? {
                val noteId = savedStateHandle.get<String>(ARG_NOTE_ID).orEmpty()
                return noteId.takeIf(String::isNotBlank)?.let(::NoteEditor)
            }
        }
    }

    data class ChecklistEditor(val checklistId: String) : AppRoute("checklist_editor/$checklistId") {
        companion object Spec {
            const val ARG_CHECKLIST_ID = "checklistId"
            const val ROUTE = "checklist_editor/{$ARG_CHECKLIST_ID}"
            val arguments = listOf(navArgument(ARG_CHECKLIST_ID) { type = NavType.StringType })

            fun from(savedStateHandle: SavedStateHandle): ChecklistEditor? {
                val checklistId = savedStateHandle.get<String>(ARG_CHECKLIST_ID).orEmpty()
                return checklistId.takeIf(String::isNotBlank)?.let(::ChecklistEditor)
            }
        }
    }

    data class DrawingEditor(val drawingId: String) : AppRoute("drawing_editor/$drawingId") {
        companion object Spec {
            const val ARG_DRAWING_ID = "drawingId"
            const val ROUTE = "drawing_editor/{$ARG_DRAWING_ID}"
            val arguments = listOf(navArgument(ARG_DRAWING_ID) { type = NavType.StringType })

            fun from(savedStateHandle: SavedStateHandle): DrawingEditor? {
                val drawingId = savedStateHandle.get<String>(ARG_DRAWING_ID).orEmpty()
                return drawingId.takeIf(String::isNotBlank)?.let(::DrawingEditor)
            }
        }
    }
}
