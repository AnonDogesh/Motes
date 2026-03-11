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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.unit.dp
import com.example.motes.ui.home.HomeFilter
import com.example.motes.ui.theme.Accent

@Composable
fun FilterDialFab(
    activeFilter: HomeFilter,
    onNotes: () -> Unit,
    onChecklists: () -> Unit,
    onDrawings: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val mainFabScale by animateFloatAsState(
        targetValue = if (expanded) 1.07f else 1f,
        animationSpec = spring(dampingRatio = 0.85f),
        label = "filter_dial_main_scale"
    )
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
                    .clickable { expanded = false }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExtendedFloatingActionButton(
                        text = { Text(if (activeFilter == HomeFilter.NOTE) "Notes ✓" else "Notes") },
                        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onNotes()
                        },
                        expanded = true,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
                    ExtendedFloatingActionButton(
                        text = { Text(if (activeFilter == HomeFilter.CHECKLIST) "Checklists ✓" else "Checklists") },
                        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onChecklists()
                        },
                        expanded = true,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
                    ExtendedFloatingActionButton(
                        text = { Text(if (activeFilter == HomeFilter.DRAWING) "Drawings ✓" else "Drawings") },
                        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onDrawings()
                        },
                        expanded = true,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
                    ExtendedFloatingActionButton(
                        text = { Text("Archive") },
                        icon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                        onClick = {
                            expanded = false
                            onArchive()
                        },
                        expanded = true,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(18.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = Accent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.scale(mainFabScale),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = if (expanded) "Close filter menu" else "Open filter menu"
                )
            }
        }
    }
}
