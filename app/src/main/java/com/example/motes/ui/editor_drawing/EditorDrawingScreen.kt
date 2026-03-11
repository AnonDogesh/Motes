package com.example.motes.ui.editor_drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.motes.data.AppContainer
import com.example.motes.ui.theme.Accent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingEditorScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    val context = LocalContext.current
    val repository = AppContainer.drawingRepository(context)
    val drawingId = backStackEntry.arguments?.getString("noteId").orEmpty()
    val viewModel: DrawingEditorViewModel = viewModel(
        backStackEntry,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DrawingEditorViewModel(repository, drawingId) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStrokePoints = remember { mutableStateListOf<DrawPoint>() }
    var showColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = viewModel::setTitle,
                            textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(Accent),
                            decorationBox = { inner ->
                                if (uiState.title.isBlank()) {
                                    Text("Drawing", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                inner()
                            }
                        )
                        Text(uiState.lastEditedLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showColorPicker = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Pick drawing color", tint = uiState.cardColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = viewModel::undo) { Text("Undo") }
                    TextButton(onClick = viewModel::clear) { Text("Clear") }
                }
            )
        },
        bottomBar = {
            DrawingBottomControls(
                selectedColor = uiState.selectedColor,
                selectedTool = uiState.selectedTool,
                strokeWidth = uiState.strokeWidth,
                onColorSelect = viewModel::setColor,
                onToolSelect = viewModel::setTool,
                onWidthChange = viewModel::setStrokeWidth
            )
        }
    ) { innerPadding ->
        if (showColorPicker) {
            ColorPickerDialogDrawing(
                selectedColor = uiState.cardColor,
                onColorSelected = { viewModel.setCardColor(it); showColorPicker = false },
                onDismiss = { showColorPicker = false }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                .pointerInput(uiState.selectedColor, uiState.strokeWidth, uiState.selectedTool) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStrokePoints.clear()
                            currentStrokePoints.add(DrawPoint(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            currentStrokePoints.add(DrawPoint(change.position.x, change.position.y))
                        },
                        onDragEnd = {
                            viewModel.addStroke(currentStrokePoints.toList())
                            currentStrokePoints.clear()
                        },
                        onDragCancel = {
                            currentStrokePoints.clear()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                uiState.strokes.forEach { stroke ->
                    stroke.points.zipWithNext { start, end ->
                        drawLine(
                            color = if (stroke.isEraser) MaterialTheme.colorScheme.surfaceVariant else Color(stroke.color),
                            start = Offset(start.x, start.y),
                            end = Offset(end.x, end.y),
                            strokeWidth = stroke.width,
                            cap = StrokeCap.Round
                        )
                    }
                }

                currentStrokePoints.zipWithNext { start, end ->
                    drawLine(
                        color = if (uiState.selectedTool == DrawingTool.Eraser) MaterialTheme.colorScheme.surfaceVariant else Color(uiState.selectedColor),
                        start = Offset(start.x, start.y),
                        end = Offset(end.x, end.y),
                        strokeWidth = uiState.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                drawRect(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}


@Composable
private fun ColorPickerDialogDrawing(
    selectedColor: Long?,
    onColorSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = listOf(0xFF6B5E2EL, 0xFF7A4A2BL, 0xFF6A3B3BL, 0xFF5A3F6EL,0xFF3F4F74L, 0xFF2F5D78L, 0xFF2F6F6DL, 0xFF3E6B3EL,0xFF5E6A2EL, 0xFF6B6B2EL, 0xFF5C4A3BL, 0xFF4E5B63L)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select drawing color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = { onColorSelected(null) }) { Text("Default") }
                LazyVerticalGrid(columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(120.dp)) {
                    items(palette) { colorLong ->
                        Box(modifier = Modifier.size(if (selectedColor == colorLong) 30.dp else 26.dp).background(Color(colorLong), CircleShape).clickable { onColorSelected(colorLong) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun DrawingBottomControls(
    selectedColor: Long,
    selectedTool: DrawingTool,
    strokeWidth: Float,
    onColorSelect: (Long) -> Unit,
    onToolSelect: (DrawingTool) -> Unit,
    onWidthChange: (Float) -> Unit
) {
    val palette = listOf(0xFFEAEFEFL, 0xFFFF9B51L, 0xFF8ED1F0L, 0xFFA7F3D0L, 0xFFFCA5A5L)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onToolSelect(DrawingTool.Brush) }) { Text(if (selectedTool == DrawingTool.Brush) "Brush ✓" else "Brush") }
            TextButton(onClick = { onToolSelect(DrawingTool.Eraser) }) { Text(if (selectedTool == DrawingTool.Eraser) "Eraser ✓" else "Eraser") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            palette.forEach { colorLong ->
                val color = Color(colorLong)
                Box(
                    modifier = Modifier
                        .size(if (selectedColor == colorLong) 32.dp else 28.dp)
                        .background(
                            color = if (selectedColor == colorLong) Color.White.copy(alpha = 0.18f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(3.dp)
                        .background(color, CircleShape)
                        .clickable { onColorSelect(colorLong) }
                )
            }
        }

        Column {
            Text("Stroke: ${strokeWidth.toInt()}px", style = MaterialTheme.typography.bodyMedium)
            Slider(value = strokeWidth, onValueChange = onWidthChange, valueRange = 2f..28f)
        }
    }
}
