package com.example.motes.ui.editor_checklist

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import com.example.motes.notifications.ChecklistReminderScheduler
import com.example.motes.ui.settings.NotificationSettingsState
import java.util.Calendar
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.motes.data.AppContainer
import com.example.motes.ui.theme.DarkCardPalette
import com.example.motes.ui.theme.LightCardPalette
import com.example.motes.ui.theme.cardColorForDisplay
import com.example.motes.ui.theme.usesLightCardPalette
import com.example.motes.ui.theme.cardColorForStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEditorScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    val context = LocalContext.current
    val repository = AppContainer.checklistRepository(context)
    val checklistId = backStackEntry.arguments?.getString("noteId").orEmpty()
    val viewModel: ChecklistEditorViewModel = viewModel(
        backStackEntry,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChecklistEditorViewModel(repository, checklistId) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLightTheme = usesLightCardPalette()
    val displayedCardColor = uiState.cardColor?.let { cardColorForDisplay(it, isLightTheme) }
    val checklistKey = uiState.checklistId.ifBlank { "new" }
    var newItemText by rememberSaveable(checklistKey) { mutableStateOf("") }
    var showColorPicker by rememberSaveable(checklistKey) { mutableStateOf(false) }
    var reminderError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "CHECKLIST",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = uiState.lastEditedLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        NotificationSettingsState.refreshFromSystem(context)
                        if (!NotificationSettingsState.enabled) {
                            reminderError = "Enable notifications in Settings to use reminders."
                            return@IconButton
                        }
                        val now = System.currentTimeMillis()
                        val existing = uiState.reminderAt?.takeIf { it > now } ?: now
                        val calendar = Calendar.getInstance().apply { timeInMillis = existing }
                        android.app.TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val selected = Calendar.getInstance().apply {
                                    timeInMillis = now
                                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                    if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
                                }.timeInMillis
                                if (!viewModel.canSetReminderAt(selected)) {
                                    reminderError = "Reminder must be within 24 hours of checklist creation or last edit."
                                } else {
                                    viewModel.setReminderAt(selected)
                                    ChecklistReminderScheduler.schedule(
                                        context = context,
                                        checklistId = uiState.checklistId,
                                        title = uiState.title.ifBlank { "Checklist reminder" },
                                        triggerAtMillis = selected
                                    )
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Set reminder",
                            tint = if (uiState.reminderAt != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showColorPicker = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Pick checklist color", tint = displayedCardColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
        bottomBar = {
            AddItemInputRow(
                value = newItemText,
                onValueChange = { newItemText = it },
                onAdd = {
                    viewModel.addItem(newItemText)
                    newItemText = ""
                }
            )
        }
    ) { innerPadding ->
        reminderError?.let { message ->
        AlertDialog(
            onDismissRequest = { reminderError = null },
            title = { Text("Reminder") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { reminderError = null }) { Text("OK") } }
        )
    }

    if (showColorPicker) {
            ColorPickerDialogChecklist(
                selectedColor = displayedCardColor,
                onColorSelected = { selected ->
                    viewModel.setCardColor(selected?.let { cardColorForStorage(it, isLightTheme) })
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            BasicTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 10.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (uiState.title.isBlank()) {
                        Text(
                            text = "Checklist title",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            )

            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                color = if (uiState.progress >= 0.999f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Text(
                text = uiState.completionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    ChecklistRow(
                        item = item,
                        onCheckedChange = { viewModel.onItemCheckedChanged(item.id, it) },
                        onTextChange = { viewModel.onItemTextChanged(item.id, it) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChecklistRow(
    item: ChecklistEditorItemUi,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            if (target == SwipeToDismissBoxValue.EndToStart || target == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = { onCheckedChange(it) })
            BasicTextField(
                value = item.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                textStyle = MaterialTheme.typography.bodyLarge.merge(
                    TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                    )
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (item.text.isBlank()) {
                        Text(
                            text = "List item",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun ColorPickerDialogChecklist(
    selectedColor: Long?,
    onColorSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val isLightTheme = usesLightCardPalette()
    val palette = if (isLightTheme) LightCardPalette else DarkCardPalette
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select checklist color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = { onColorSelected(null) }) { Text("Default") }
                LazyVerticalGrid(columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(120.dp)) {
                    gridItems(palette) { colorLong ->
                        Box(modifier = Modifier.size(if (selectedColor == colorLong) 30.dp else 26.dp).background(Color(colorLong), RoundedCornerShape(100)).clickable { onColorSelected(colorLong) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun AddItemInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "Add new item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onAdd) {
            Text("Add", color = MaterialTheme.colorScheme.primary)
        }
    }
}
