package com.autoclicker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoclicker.data.ClickerProfile
import com.autoclicker.viewmodel.ProfileListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileListScreen(
    onEditProfile: (Long) -> Unit,
    onStartOverlay: () -> Unit,
    isOverlayRunning: Boolean,
    darkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    vm: ProfileListViewModel = viewModel()
) {
    val profiles by vm.profiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AutoClicker") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (darkTheme) "Switch to light mode" else "Switch to dark mode"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(onClick = onStartOverlay) {
                    Icon(
                        if (isOverlayRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isOverlayRunning) "Stop" else "Start"
                    )
                }
                FloatingActionButton(onClick = { vm.addProfile() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add clicker")
                }
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No clicker profiles yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to create one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        onEdit = { onEditProfile(profile.id) },
                        onDelete = { vm.delete(profile) },
                        onMoveUp = { vm.moveUp(profile) },
                        onMoveDown = { vm.moveDown(profile) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ClickerProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${profile.intervalMs} ms interval · " +
                        (if (profile.isInfinite) "∞ clicks" else "${profile.clickCount} clicks"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onMoveUp) { Icon(Icons.Default.KeyboardArrowUp, null) }
            IconButton(onClick = onMoveDown) { Icon(Icons.Default.KeyboardArrowDown, null) }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}
