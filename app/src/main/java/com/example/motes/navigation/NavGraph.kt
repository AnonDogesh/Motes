package com.example.motes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.motes.ui.archive.ArchiveScreen
import com.example.motes.ui.editor_checklist.ChecklistEditorScreen
import com.example.motes.ui.editor_drawing.DrawingEditorScreen
import com.example.motes.ui.editor_note.NoteEditorScreen
import com.example.motes.ui.home.HomeScreen

@Composable
fun MotesNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier
    ) {
        composable(route = AppRoute.Home.route) {
            HomeScreen(
                onOpenArchive = { navController.navigate(AppRoute.Archive.route) },
                onOpenNoteEditor = { id -> navController.navigate(AppRoute.NoteEditor.create(id)) },
                onOpenChecklistEditor = { id -> navController.navigate(AppRoute.ChecklistEditor.create(id)) },
                onOpenDrawingEditor = { id -> navController.navigate(AppRoute.DrawingEditor.create(id)) }
            )
        }

        composable(route = AppRoute.Archive.route) {
            ArchiveScreen(onBack = navController::navigateUp)
        }

        composable(
            route = AppRoute.NoteEditor.route,
            arguments = listOf(navArgument(AppRoute.NoteEditor.ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            })
        ) {
            NoteEditorScreen(onBack = navController::navigateUp)
        }

        composable(
            route = AppRoute.ChecklistEditor.route,
            arguments = listOf(navArgument(AppRoute.ChecklistEditor.ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            })
        ) {
            ChecklistEditorScreen(onBack = navController::navigateUp)
        }

        composable(
            route = AppRoute.DrawingEditor.route,
            arguments = listOf(navArgument(AppRoute.DrawingEditor.ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            })
        ) {
            DrawingEditorScreen(onBack = navController::navigateUp)
        }
    }
}
