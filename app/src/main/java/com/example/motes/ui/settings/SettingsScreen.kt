package com.example.motes.ui.settings

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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motes.navigation.AppRoute

private val ScreenBackground = Color(0xFF061019)
private val CardBackground = Color(0xFF2A3645)
private val Accent = Color(0xFFFFA55C)
private val MutedText = Color(0xFF9EB0C4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var selectedTheme by remember { mutableStateOf("Dark") }
    var compactLayout by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("ACCOUNT")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFE2C89A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF846B45))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Alex Morgan", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedText)
                }
            }

            SectionTitle("APPEARANCE")
            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MutedText)
                        Spacer(Modifier.width(8.dp))
                        Text("Theme", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ThemeOption("Light", selectedTheme == "Light") { selectedTheme = "Light" }
                        ThemeOption("Dark", selectedTheme == "Dark") { selectedTheme = "Dark" }
                        ThemeOption("System", selectedTheme == "System") { selectedTheme = "System" }
                    }

                    HorizontalDivider(color = Color(0xFF344556))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ViewModule, contentDescription = null, tint = MutedText)
                        Spacer(Modifier.width(8.dp))
                        Text("Note Layout", color = Color.White, style = MaterialTheme.typography.titleMedium)
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
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MutedText)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Push Notifications", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Daily summaries and reminders", color = MutedText, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
            }

            SectionTitle("SYNC & DATA")
            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SimpleActionRow(icon = Icons.Default.Download, title = "Export Notes", onClick = { })
                    HorizontalDivider(color = Color(0xFF344556))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF7A7A))
                        Spacer(Modifier.width(10.dp))
                        Text("Clear Cache", color = Color(0xFFFF7A7A), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("124 MB", color = MutedText)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Motes v2.4.0 (Build 392)", color = Color(0xFF5D7084), modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Made with ❤️ in India", color = Color(0xFF5D7084), modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Accent,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
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
        Icon(icon, contentDescription = null, tint = MutedText)
        Spacer(Modifier.width(10.dp))
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedText)
    }
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF162335) else Color(0xFF435163)
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, Accent) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(if (selected) Accent else Color(0xFFA9B7C6), CircleShape)
            )
            Text(label, color = if (selected) Accent else MutedText)
        }
    }
}

@Composable
private fun LayoutPill(isCompact: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0xFF1A2736), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (!isCompact) Accent else Color.Transparent, RoundedCornerShape(8.dp))
                .clickable { if (isCompact) onToggle() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ViewModule, contentDescription = null, tint = if (!isCompact) Color.Black else MutedText) }

        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (isCompact) Accent else Color.Transparent, RoundedCornerShape(8.dp))
                .clickable { if (!isCompact) onToggle() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ViewModule, contentDescription = null, tint = if (isCompact) Color.Black else MutedText) }
    }
}
