package com.example.motes.navigation

sealed interface AppRoute {
    val route: String

    data object Home : AppRoute {
        override val route: String = "home"
    }

    data object Archive : AppRoute {
        override val route: String = "archive"
    }

    data object NoteEditor : AppRoute {
        private const val BASE = "note_editor"
        const val ARG_ID = "id"
        override val route: String = "$BASE/{$ARG_ID}"

        fun create(id: String? = null): String = "$BASE/${id.orEmpty()}"
    }

    data object ChecklistEditor : AppRoute {
        private const val BASE = "checklist_editor"
        const val ARG_ID = "id"
        override val route: String = "$BASE/{$ARG_ID}"

        fun create(id: String? = null): String = "$BASE/${id.orEmpty()}"
    }

    data object DrawingEditor : AppRoute {
        private const val BASE = "drawing_editor"
        const val ARG_ID = "id"
        override val route: String = "$BASE/{$ARG_ID}"

        fun create(id: String? = null): String = "$BASE/${id.orEmpty()}"
    }
}
