package com.example.motes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.example.motes.ui.theme.Accent

@Composable
fun SpeedDialFab(
    onNewNote: () -> Unit,
    onNewChecklist: () -> Unit,
    onNewDrawing: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val mainFabRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "speed_dial_main_rotation"
    )
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.6f
    val menuContainerColor = if (isLightTheme) MaterialTheme.colorScheme.surface else Color(0xFF4A6270)
    val menuContentColor = if (isLightTheme) MaterialTheme.colorScheme.onSurface else Color.White
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
                    .clickable { expanded = false }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExtendedFloatingActionButton(
                        text = { Text("New Note") },
                        icon = { Icon(Icons.Default.Note, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onNewNote()
                        },
                        expanded = true,
                        containerColor = menuContainerColor,
                        contentColor = menuContentColor,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
                    ExtendedFloatingActionButton(
                        text = { Text("New Checklist") },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onNewChecklist()
                        },
                        expanded = true,
                        containerColor = menuContainerColor,
                        contentColor = menuContentColor,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
                    ExtendedFloatingActionButton(
                        text = { Text("New Drawing") },
                        icon = { Icon(Icons.Default.Brush, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onNewDrawing()
                        },
                        expanded = true,
                        containerColor = menuContainerColor,
                        contentColor = menuContentColor,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )

                }
            }

            Spacer(modifier = Modifier.height(if (expanded) 12.dp else 0.dp))

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = Accent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "+",
                    modifier = Modifier.graphicsLayer { rotationZ = mainFabRotation }.scale(1f)
                )
            }
        }
    }
}
