package com.example.motes.ui.editor_note

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.motes.data.AppContainer
import com.example.motes.ui.theme.Accent
import com.example.motes.ui.theme.SurfaceHigh
import com.example.motes.ui.theme.SurfaceMedium
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    val context = LocalContext.current
    val repo = AppContainer.noteRepository(context)
    val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
    val viewModel: NoteEditorViewModel = viewModel(
        backStackEntry,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return NoteEditorViewModel(repo, noteId) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val persisted = persistImageToAppStorage(context, uri)
            viewModel.addImage(persisted)
        }
    }

    val noteBodyStyle = MaterialTheme.typography.bodyLarge.merge(
        TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = if (uiState.isBoldEnabled) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (uiState.isItalicEnabled) FontStyle.Italic else FontStyle.Normal,
            textDecoration = if (uiState.isUnderlineEnabled) TextDecoration.Underline else TextDecoration.None
        )
    )

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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NoteFormattingToolbar(
                isBoldEnabled = uiState.isBoldEnabled,
                isItalicEnabled = uiState.isItalicEnabled,
                isUnderlineEnabled = uiState.isUnderlineEnabled,
                onToggleBold = viewModel::toggleBold,
                onToggleItalic = viewModel::toggleItalic,
                onToggleUnderline = viewModel::toggleUnderline,
                onAddImage = { imagePicker.launch("image/*") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(Accent),
                decorationBox = { innerTextField ->
                    if (uiState.title.isBlank()) {
                        Text("Title", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    innerTextField()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

            if (uiState.imageUris.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.imageUris.forEach { imageUri ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceMedium, RoundedCornerShape(16.dp))
                                .padding(8.dp)
                        ) {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                factory = { ctx ->
                                    ImageView(ctx).apply {
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                        adjustViewBounds = true
                                    }
                                },
                                update = { imageView ->
                                    imageView.setImageURI(Uri.parse(imageUri))
                                }
                            )
                            IconButton(
                                onClick = { viewModel.removeImage(imageUri) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove image")
                            }
                        }
                    }
                }
            }

            BasicTextField(
                value = uiState.body,
                onValueChange = viewModel::onBodyChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(620.dp),
                textStyle = noteBodyStyle,
                cursorBrush = SolidColor(Accent),
                decorationBox = { innerTextField ->
                    if (uiState.body.isBlank()) {
                        Text("Start writing your note...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f))
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun NoteFormattingToolbar(
    isBoldEnabled: Boolean,
    isItalicEnabled: Boolean,
    isUnderlineEnabled: Boolean,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onAddImage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceMedium)
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormatButton(label = "B", isSelected = isBoldEnabled, onClick = onToggleBold)
        FormatButton(label = "I", italic = true, isSelected = isItalicEnabled, onClick = onToggleItalic)
        FormatButton(label = "U", underline = true, isSelected = isUnderlineEnabled, onClick = onToggleUnderline)
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(
            onClick = onAddImage,
            containerColor = Accent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(42.dp),
            shape = CircleShape
        ) {
            Text("+")
        }
    }
}

@Composable
private fun FormatButton(
    label: String,
    italic: Boolean = false,
    underline: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else SurfaceHigh,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

private fun persistImageToAppStorage(context: android.content.Context, sourceUri: Uri): String {
    val imagesDir = File(context.filesDir, "note_images").apply { mkdirs() }
    val targetFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")

    context.contentResolver.openInputStream(sourceUri).use { input ->
        requireNotNull(input) { "Unable to read selected image." }
        FileOutputStream(targetFile).use { output ->
            input.copyTo(output)
        }
    }

    return Uri.fromFile(targetFile).toString()
}
