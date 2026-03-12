package com.example.motes.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motes.ui.archive.ArchiveScreen
import com.example.motes.ui.editor_checklist.ChecklistEditorScreen
import com.example.motes.ui.editor_drawing.DrawingEditorScreen
import com.example.motes.ui.editor_note.NoteEditorScreen
import com.example.motes.ui.home.HomeScreen
import com.example.motes.ui.settings.NotificationSettingsState
import com.example.motes.ui.settings.SettingsScreen
import com.example.motes.ui.theme.MotesTheme

private fun hasNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun ReminderPermissionFlash(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Reminders need notification permission", style = MaterialTheme.typography.titleLarge)
            Text(
                "Motes only uses notifications for your checklist reminders.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRequestPermission) {
                Text("Allow notifications")
            }
        }
    }
}


@Composable
fun MotesApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var showPermissionFlash by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        NotificationSettingsState.onPermissionResult(context, granted)
        showPermissionFlash = false
    }

    LaunchedEffect(Unit) {
        NotificationSettingsState.initialize(context)
        NotificationSettingsState.refreshFromSystem(context)
        val prefs = context.getSharedPreferences("motes_prefs", Context.MODE_PRIVATE)
        val asked = prefs.getBoolean("notification_permission_asked", false)
        if (!asked && !hasNotificationPermission(context)) {
            showPermissionFlash = true
            prefs.edit().putBoolean("notification_permission_asked", true).apply()
        }
    }

    MotesTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
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

                if (showPermissionFlash) {
                    ReminderPermissionFlash(
                        onRequestPermission = {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }
        }
    }
}
