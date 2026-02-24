package com.example.motes.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object EditorNote : Routes("editor_note")
    data object EditorChecklist : Routes("editor_checklist")
    data object EditorDrawing : Routes("editor_drawing")
}
