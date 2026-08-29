package com.autoclicker.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoclicker.data.ClickerProfile
import com.autoclicker.viewmodel.ProfileListViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    val flashEnabled by vm.flashEnabled

    val localProfiles = remember { mutableStateListOf<ClickerProfile>() }
    var isDraggingAny by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        localProfiles.add(to.index, localProfiles.removeAt(from.index))
    }

    // Sync DB → local list whenever profiles change, but not mid-drag
    LaunchedEffect(profiles) {
        if (!isDraggingAny) {
            localProfiles.clear()
            localProfiles.addAll(profiles)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AutoClicker") },
                actions = {
                    IconButton(onClick = { vm.toggleFlash() }) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (flashEnabled) "Disable click flash" else "Enable click flash",
                            tint = if (flashEnabled) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
        if (localProfiles.isEmpty()) {
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
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(localProfiles, key = { it.id }) { profile ->
                    ReorderableItem(reorderState, key = profile.id) { isDragging ->
                        ProfileCard(
                            profile = profile,
                            isDragging = isDragging,
                            reorderModifier = Modifier.longPressDraggableHandle(
                                onDragStarted = { isDraggingAny = true },
                                onDragStopped = {
                                    isDraggingAny = false
                                    vm.reorderProfiles(localProfiles.toList())
                                }
                            ),
                            onEdit = { onEditProfile(profile.id) },
                            onDelete = { vm.delete(profile) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ClickerProfile,
    isDragging: Boolean,
    reorderModifier: Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val elevation by animateDpAsState(if (isDragging) 6.dp else 0.dp, label = "drag-elevation")
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, shape = CardDefaults.outlinedShape)
            .then(reorderModifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${profile.intervalMs} ms · " +
                        if (profile.isInfinite) "∞" else "${profile.clickCount}×",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
