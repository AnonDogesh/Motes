package com.example.motes.ui.editor_checklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChecklistEditorScreen(
    onBack: () -> Boolean,
    viewModel: ChecklistEditorViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Checklist Editor")
        Text("ID: ${viewModel.checklistId ?: "new"}")
        Button(onClick = { onBack() }) {
            Text("Back")
        }
    }
}
