package com.example.motes.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Archive : AppRoute("archive")

    data class NoteEditor(val noteId: String) : AppRoute("editor_note/$noteId") {
        companion object Spec {
            const val ARG_NOTE_ID = "noteId"
            const val ROUTE = "editor_note/{$ARG_NOTE_ID}"
            val arguments = listOf(navArgument(ARG_NOTE_ID) { type = NavType.StringType })

            fun from(savedStateHandle: SavedStateHandle): NoteEditor? {
                val noteId = savedStateHandle.get<String>(ARG_NOTE_ID).orEmpty()
                return noteId.takeIf(String::isNotBlank)?.let(::NoteEditor)
            }
        }
    }

    data class ChecklistEditor(val noteId: String) : AppRoute("editor_checklist/$noteId") {
        companion object Spec {
            const val ARG_NOTE_ID = "noteId"
            const val ROUTE = "editor_checklist/{$ARG_NOTE_ID}"
            val arguments = listOf(navArgument(ARG_NOTE_ID) { type = NavType.StringType })

            fun from(savedStateHandle: SavedStateHandle): ChecklistEditor? {
                val noteId = savedStateHandle.get<String>(ARG_NOTE_ID).orEmpty()
                return noteId.takeIf(String::isNotBlank)?.let(::ChecklistEditor)
            }
        }
    }

    data class DrawingEditor(val noteId: String) : AppRoute("editor_drawing/$noteId") {
        companion object Spec {
            const val ARG_NOTE_ID = "noteId"
            const val ROUTE = "editor_drawing/{$ARG_NOTE_ID}"
            val arguments = listOf(navArgument(ARG_NOTE_ID) { type = NavType.StringType })

            fun from(savedStateHandle: SavedStateHandle): DrawingEditor? {
                val noteId = savedStateHandle.get<String>(ARG_NOTE_ID).orEmpty()
                return noteId.takeIf(String::isNotBlank)?.let(::DrawingEditor)
            }
        }
    }
}
