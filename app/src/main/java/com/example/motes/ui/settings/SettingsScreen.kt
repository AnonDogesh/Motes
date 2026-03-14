package com.example.motes.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Popup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motes.navigation.AppRoute
import com.example.motes.ui.theme.AppThemeState
import com.example.motes.ui.theme.ThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var selectedTheme by remember { mutableStateOf(AppThemeState.mode) }
    var compactLayout by remember { mutableStateOf(false) }
    var accountName by remember { mutableStateOf("Unknown") }
    var draftAccountName by remember { mutableStateOf(accountName) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCustomThemesMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        NotificationSettingsState.onPermissionResult(context, granted)
    }

    LaunchedEffect(Unit) {
        NotificationSettingsState.initialize(context)
        NotificationSettingsState.refreshFromSystem(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                NotificationSettingsState.refreshFromSystem(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val colorScheme = MaterialTheme.colorScheme
    val screenBackground = colorScheme.background
    val accent = colorScheme.primary
    val mutedText = colorScheme.onSurfaceVariant
    val mainText = colorScheme.onSurface
    val iconChip = if (screenBackground.luminance() > 0.6f) Color(0xFFDCC9A5) else Color(0xFFE2C89A)


    Scaffold(
        containerColor = screenBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = accent) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = mainText)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showRenameDialog) {
            RenameAccountDialog(
                value = draftAccountName,
                onValueChange = { draftAccountName = it },
                onDismiss = {
                    draftAccountName = accountName
                    showRenameDialog = false
                },
                onSave = {
                    val trimmedName = draftAccountName.trim()
                    accountName = if (trimmedName.isEmpty()) accountName else trimmedName
                    draftAccountName = accountName
                    showRenameDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("ACCOUNT")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            draftAccountName = accountName
                            showRenameDialog = true
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(iconChip, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF846B45))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(accountName, style = MaterialTheme.typography.titleMedium, color = mainText)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SectionTitle("APPEARANCE")
            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = mutedText)
                        Spacer(Modifier.width(8.dp))
                        Text("Theme", color = mainText, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Box {
                            OutlinedButton(
                                onClick = { showCustomThemesMenu = !showCustomThemesMenu },
                                border = BorderStroke(1.5.dp, accent),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Custom",
                                    color = accent,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            if (showCustomThemesMenu) {
                                Popup(alignment = Alignment.TopEnd, onDismissRequest = { showCustomThemesMenu = false }) {
                                    Card(
                                        modifier = Modifier
                                            .padding(top = 44.dp)
                                            .width(220.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text("Custom themes", color = mainText, style = MaterialTheme.typography.labelLarge)

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                CustomThemeOption(
                                                    name = "Peach",
                                                    swatch = Color(0xFFFD7979),
                                                    selected = selectedTheme == ThemeMode.PEACH,
                                                    onSelect = {
                                                        selectedTheme = ThemeMode.PEACH
                                                        AppThemeState.mode = ThemeMode.PEACH
                                                        showCustomThemesMenu = false
                                                    },
                                                    accent = accent,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                CustomThemeOption(
                                                    name = "Sea",
                                                    swatch = Color(0xFF016B61),
                                                    selected = selectedTheme == ThemeMode.SEA,
                                                    onSelect = {
                                                        selectedTheme = ThemeMode.SEA
                                                        AppThemeState.mode = ThemeMode.SEA
                                                        showCustomThemesMenu = false
                                                    },
                                                    accent = accent,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ThemeOption(
                            label = "Light",
                            selected = selectedTheme == ThemeMode.LIGHT,
                            modifier = Modifier.weight(1f),
                            mainText = mainText,
                            accent = accent,
                        ) { selectedTheme = ThemeMode.LIGHT; AppThemeState.mode = ThemeMode.LIGHT }
                        ThemeOption(
                            label = "Dark",
                            selected = selectedTheme == ThemeMode.DARK,
                            modifier = Modifier.weight(1f),
                            mainText = mainText,
                            accent = accent,
                        ) { selectedTheme = ThemeMode.DARK; AppThemeState.mode = ThemeMode.DARK }
                        ThemeOption(
                            label = "System",
                            selected = selectedTheme == ThemeMode.SYSTEM,
                            modifier = Modifier.weight(1f),
                            mainText = mainText,
                            accent = accent,
                        ) { selectedTheme = ThemeMode.SYSTEM; AppThemeState.mode = ThemeMode.SYSTEM }
                    }

                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.45f))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ViewModule, contentDescription = null, tint = mutedText)
                        Spacer(Modifier.width(8.dp))
                        Text("Note Layout", color = mainText, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        LayoutPill(isCompact = compactLayout, onToggle = { compactLayout = !compactLayout })
                    }
                }
            }

            SectionTitle("ARCHIVE")
            SettingsCard {
                SimpleActionRow(
                    icon = Icons.Default.Archive,
                    title = "Archived Notes",
                    onClick = { navController.navigate(AppRoute.Archive.route) }
                )
            }

            SectionTitle("NOTIFICATIONS")
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = mutedText)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Push Notifications", color = mainText, style = MaterialTheme.typography.titleMedium)
                        Text("Daily summaries and reminders", color = mutedText, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = NotificationSettingsState.enabled, onCheckedChange = { desired ->
                        NotificationSettingsState.setEnabled(
                            context = context,
                            desired = desired,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    })
                }
            }

            SectionTitle("SYNC & DATA")
            SettingsCard {
                SimpleActionRow(icon = Icons.Default.Download, title = "Export Notes", onClick = { })
            }

            Spacer(Modifier.height(8.dp))
            Text("Motes v2.4.0 (Build 392)", color = mutedText, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Made with ❤️ in India", color = mutedText, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) { content() }
}

@Composable
private fun SimpleActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    mainText: Color,
    accent: Color,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (selected) BorderStroke(2.dp, accent) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(if (selected) accent else Color(0xFFA9B7C6), CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = if (selected) accent else mainText,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}




@Composable
private fun CustomThemeOption(
    name: String,
    swatch: Color,
    selected: Boolean,
    onSelect: () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = if (selected) BorderStroke(1.5.dp, accent) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(swatch, CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun RenameAccountDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename account") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                label = { Text("Account name") }
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LayoutPill(isCompact: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (!isCompact) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                .clickable { if (isCompact) onToggle() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ViewModule, contentDescription = null, tint = if (!isCompact) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant) }

        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (isCompact) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                .clickable { if (!isCompact) onToggle() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ViewModule, contentDescription = null, tint = if (isCompact) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
