package com.example.motes.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motes.ui.archive.ArchiveScreen
import com.example.motes.ui.editor_checklist.ChecklistEditorScreen
import com.example.motes.ui.editor_drawing.DrawingEditorScreen
import com.example.motes.ui.editor_note.NoteEditorScreen
import com.example.motes.ui.home.HomeScreen
import com.example.motes.ui.settings.SettingsScreen
import com.example.motes.ui.theme.MotesTheme

@Composable
fun MotesApp() {
    val navController = rememberNavController()

    MotesTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = AppRoute.Home.route
            ) {
                composable(AppRoute.Home.route) {
                    HomeScreen(navController = navController)
                }

                composable(AppRoute.Archive.route) {
                    ArchiveScreen(navController = navController)
                }

                composable(AppRoute.Settings.route) {
                    SettingsScreen(navController = navController)
                }

                composable(
                    route = AppRoute.NoteEditor.ROUTE,
                    arguments = AppRoute.NoteEditor.arguments
                ) { backStackEntry ->
                    NoteEditorScreen(
                        navController = navController,
                        backStackEntry = backStackEntry
                    )
                }

                composable(
                    route = AppRoute.ChecklistEditor.ROUTE,
                    arguments = AppRoute.ChecklistEditor.arguments
                ) { backStackEntry ->
                    ChecklistEditorScreen(
                        navController = navController,
                        backStackEntry = backStackEntry
                    )
                }

                composable(
                    route = AppRoute.DrawingEditor.ROUTE,
                    arguments = AppRoute.DrawingEditor.arguments
                ) { backStackEntry ->
                    DrawingEditorScreen(
                        navController = navController,
                        backStackEntry = backStackEntry
                    )
                }
            }
        }
    }
}
