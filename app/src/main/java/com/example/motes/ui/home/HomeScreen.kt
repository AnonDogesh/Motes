package com.example.motes.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenArchive: () -> Unit,
    onOpenNoteEditor: (String?) -> Unit,
    onOpenChecklistEditor: (String?) -> Unit,
    onOpenDrawingEditor: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Home")
        Button(onClick = onOpenArchive) { Text("Open Archive") }
        Button(onClick = { onOpenNoteEditor(null) }) { Text("Open Note Editor (new)") }
        Button(onClick = { onOpenChecklistEditor("42") }) { Text("Open Checklist Editor (id=42)") }
        Button(onClick = { onOpenDrawingEditor("abc") }) { Text("Open Drawing Editor (id=abc)") }
    }
}
