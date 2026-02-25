package com.example.motes.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motes.ui.home.HomeScreen

@Composable
fun MotesNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen()
        }

        composable(AppRoute.Archive.route) {
            PlaceholderRoute(title = "archive", onBack = navController::navigateUp)
        }

        composable(
            route = AppRoute.NoteEditor.ROUTE,
            arguments = AppRoute.NoteEditor.arguments
        ) {
            PlaceholderRoute(title = "note_editor/{noteId}", onBack = navController::navigateUp)
        }

        composable(
            route = AppRoute.ChecklistEditor.ROUTE,
            arguments = AppRoute.ChecklistEditor.arguments
        ) {
            PlaceholderRoute(title = "checklist_editor/{checklistId}", onBack = navController::navigateUp)
        }

        composable(
            route = AppRoute.DrawingEditor.ROUTE,
            arguments = AppRoute.DrawingEditor.arguments
        ) {
            PlaceholderRoute(title = "drawing_editor/{drawingId}", onBack = navController::navigateUp)
        }
    }
}

@Composable
private fun PlaceholderRoute(
    title: String,
    onBack: () -> Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title)
        Button(onClick = { onBack() }) {
            Text("Back")
        }
    }
}
