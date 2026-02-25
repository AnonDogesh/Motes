package com.example.motes.ui.editor_note

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.motes.ui.theme.Accent
import com.example.motes.ui.theme.SurfaceMedium
import com.example.motes.ui.theme.SurfaceHigh

@Composable
fun NoteEditorScreen(
    onBack: () -> Boolean,
    viewModel: NoteEditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val noteKey = uiState.noteId ?: "new"

    var titleText by rememberSaveable(noteKey) { mutableStateOf(uiState.initialTitle) }
    var bodyText by rememberSaveable(noteKey) { mutableStateOf(uiState.initialBody) }

    LaunchedEffect(titleText, bodyText) {
        viewModel.onDraftChanged(titleText, bodyText)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = uiState.sectionLabel,
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
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Text("⇪", style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(onClick = { }) {
                        Text("⋯", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        bottomBar = {
            NoteFormattingToolbar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            BasicTextField(
                value = titleText,
                onValueChange = { titleText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(Accent),
                decorationBox = { innerTextField ->
                    if (titleText.isBlank()) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

            BasicTextField(
                value = bodyText,
                onValueChange = { bodyText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(620.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(Accent),
                decorationBox = { innerTextField ->
                    if (bodyText.isBlank()) {
                        Text(
                            text = "Start writing your note...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun NoteFormattingToolbar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceMedium)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 68.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarPillButton("☑")
            ToolbarPillButton("B", emphasized = true)
            ToolbarPillButton("I", italic = true)
            ToolbarPillButton("U")
            ToolbarPillButton("•")
            ToolbarPillButton("Tt")
        }

        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp),
            containerColor = Accent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Text("＋")
        }
    }
}

@Composable
private fun ToolbarPillButton(
    label: String,
    emphasized: Boolean = false,
    italic: Boolean = false
) {
    Box(
        modifier = Modifier
            .background(
                color = SurfaceHigh,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.width(2.dp))
}
