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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.createSavedStateHandle
import androidx.navigation.NavController
import com.example.motes.data.AppContainer
import com.example.motes.ui.theme.Accent
import com.example.motes.ui.theme.SurfaceHigh
import com.example.motes.ui.theme.SurfaceMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingEditorScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val repository = AppContainer.drawingRepository(context)
    val viewModel: DrawingEditorViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return DrawingEditorViewModel(repository, extras.createSavedStateHandle()) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStrokePoints = remember { mutableStateListOf<DrawPoint>() }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .background(SurfaceMedium, RoundedCornerShape(20.dp))
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
                            color = if (stroke.isEraser) SurfaceMedium else Color(stroke.color),
                            start = Offset(start.x, start.y),
                            end = Offset(end.x, end.y),
                            strokeWidth = stroke.width,
                            cap = StrokeCap.Round
                        )
                    }
                }

                currentStrokePoints.zipWithNext { start, end ->
                    drawLine(
                        color = if (uiState.selectedTool == DrawingTool.Eraser) SurfaceMedium else Color(uiState.selectedColor),
                        start = Offset(start.x, start.y),
                        end = Offset(end.x, end.y),
                        strokeWidth = uiState.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                drawRect(
                    color = SurfaceHigh.copy(alpha = 0.2f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
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
            .background(SurfaceMedium)
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
