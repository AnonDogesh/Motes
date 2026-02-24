package com.example.motes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motes.ui.editor_checklist.EditorChecklistScreen
import com.example.motes.ui.editor_drawing.EditorDrawingScreen
import com.example.motes.ui.editor_note.EditorNoteScreen
import com.example.motes.ui.home.HomeScreen

@Composable
fun MotesNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen()
        }
        composable(Routes.EditorNote.route) {
            EditorNoteScreen()
        }
        composable(Routes.EditorChecklist.route) {
            EditorChecklistScreen()
        }
        composable(Routes.EditorDrawing.route) {
            EditorDrawingScreen()
        }
    }
}
