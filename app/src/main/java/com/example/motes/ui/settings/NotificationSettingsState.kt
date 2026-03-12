package com.example.motes.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationSettingsState {
    private const val PREFS_NAME = "motes_prefs"
    private const val KEY_USER_ENABLED = "notifications_user_enabled"

    private var initialized = false
    private var userEnabled: Boolean = true

    var enabled by mutableStateOf(false)
        private set

    fun initialize(context: Context) {
        if (initialized) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userEnabled = prefs.getBoolean(KEY_USER_ENABLED, true)
        initialized = true
        refreshFromSystem(context)
    }

    fun refreshFromSystem(context: Context) {
        initialize(context)
        enabled = userEnabled && hasSystemNotificationPermission(context)
    }

    fun setEnabled(
        context: Context,
        desired: Boolean,
        onRequestPermission: () -> Unit
    ) {
        initialize(context)
        if (!desired) {
            userEnabled = false
            persist(context)
            refreshFromSystem(context)
            return
        }

        if (hasSystemNotificationPermission(context)) {
            userEnabled = true
            persist(context)
            refreshFromSystem(context)
        } else {
            onRequestPermission()
        }
    }

    fun onPermissionResult(context: Context, granted: Boolean) {
        initialize(context)
        if (granted) {
            userEnabled = true
            persist(context)
        }
        refreshFromSystem(context)
    }

    private fun hasSystemNotificationPermission(context: Context): Boolean {
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return notificationsEnabled && runtimePermissionGranted
    }

    private fun persist(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_USER_ENABLED, userEnabled)
            .apply()
    }
}
