package com.autoclicker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoclicker.data.ClickType
import com.autoclicker.viewmodel.ProfileEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    profileId: Long,
    onBack: () -> Unit,
    vm: ProfileEditViewModel = viewModel()
) {
    LaunchedEffect(profileId) {
        if (profileId != -1L) vm.load(profileId)
    }

    val profile by vm.profile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileId == -1L) "New Clicker" else "Edit Clicker") },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.save()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = profile.name,
                onValueChange = { vm.update { copy(name = it) } },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel("Click Type")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ClickType.entries.forEach { type ->
                    FilterChip(
                        selected = profile.clickType == type,
                        onClick = { vm.update { copy(clickType = type) } },
                        label = {
                            Text(if (type == ClickType.SINGLE_TAP) "Single Tap" else "Press & Hold")
                        }
                    )
                }
            }

            if (profile.clickType == ClickType.PRESS_AND_HOLD) {
                NumberField(
                    label = "Hold Duration (ms)",
                    value = profile.holdDurationMs,
                    onValue = { vm.update { copy(holdDurationMs = it) } }
                )
            }

            SectionLabel("Timing")
            NumberField(
                label = "Interval between clicks (ms, min 50)",
                value = profile.intervalMs,
                onValue = { vm.update { copy(intervalMs = it.coerceAtLeast(50)) } }
            )
            NumberField(
                label = "Random jitter on interval (±ms)",
                value = profile.jitterIntervalMs,
                onValue = { vm.update { copy(jitterIntervalMs = it) } }
            )
            NumberField(
                label = "Start delay (ms)",
                value = profile.startDelayMs,
                onValue = { vm.update { copy(startDelayMs = it) } }
            )

            SectionLabel("Click Count")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Infinite", modifier = Modifier.weight(1f))
                Switch(
                    checked = profile.isInfinite,
                    onCheckedChange = { vm.update { copy(isInfinite = it) } }
                )
            }
            if (!profile.isInfinite) {
                NumberField(
                    label = "Number of clicks",
                    value = profile.clickCount.toLong(),
                    onValue = { vm.update { copy(clickCount = it.toInt().coerceAtLeast(1)) } }
                )
            }

            SectionLabel("Position & Jitter")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = "X",
                    value = profile.positionX.toLong(),
                    onValue = { vm.update { copy(positionX = it.toInt()) } },
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = "Y",
                    value = profile.positionY.toLong(),
                    onValue = { vm.update { copy(positionY = it.toInt()) } },
                    modifier = Modifier.weight(1f)
                )
            }
            NumberField(
                label = "Position jitter (±px)",
                value = profile.jitterPositionPx.toLong(),
                onValue = { vm.update { copy(jitterPositionPx = it.toInt()) } }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { vm.save(); onBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun NumberField(
    label: String,
    value: Long,
    onValue: (Long) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValue(it.toLongOrNull() ?: value) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true
    )
}
